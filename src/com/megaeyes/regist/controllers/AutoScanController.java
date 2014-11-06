package com.megaeyes.regist.controllers;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.dao.AutoScanDao;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.dao.SubscribeEventDao;
import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.Subscribe;
import com.megaeyes.regist.enump.SubscribeStatus;
import com.megaeyes.regist.other.ResourceType;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.GbPlatformType;
import com.megaeyes.regist.utils.sendResource.GBUtils;
import com.megaeyes.regist.webservice.PlatformUtils;
import com.megaeyes.regist.webservice.RegistUtil;
import com.megaeyes.utils.Ar;

@Component("autoScanCtrl")
public class AutoScanController {
	private static final Executor exec = Executors.newCachedThreadPool();
	public static Map<String, String> accessIPMap = new HashMap<String, String>();
	@Autowired
	private RegisterDao registerDao;

	@Autowired
	private RegistUtil registUtil;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private GbOuterDeviceController gbOuterDevice;

	@Autowired
	private GBUtils gbUtils;

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private AutoScanDao autoScanDao;

	@Value("${access_server_ip}")
	private String masterAccessIP;

	@Autowired
	private SubscribeEventDao subscribeEventDao;
	
	@Autowired
	private  InitDBController initDB;
	
	public void pullAllData(){
		try {
			initDB.executeAllPull();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void monitor() {
		subscribeNotify();
	}

	public void subscribeNotify() {
		jdbc.execute("delete from subscribe_event where expire_date<now()");
		jdbc.execute("delete ds from device_status ds where not exists (select * from subscribe_event se where se.id=ds.subscribe_event_id)");
		jdbc.execute("delete os from organ_status os where not exists (select * from subscribe_event se where se.id=os.subscribe_event_id)");
		Timestamp changeTime = new Timestamp(System.currentTimeMillis());
		int nanosecond = changeTime.getNanos() / 1000;
		// 先通知新增的机构，先父机构，后子机构，最后是设备;删除则反过来，先删除设备，子机构，父机构
		subscribeEventDao.notifyAdd(changeTime, nanosecond);
		subscribeEventDao.notifyUpdate(changeTime, nanosecond);
		subscribeEventDao.notifyDelete(changeTime, nanosecond);

		// 设备上下线通知
		subscribeEventDao.notifyDeviceOnline(changeTime, nanosecond);

		// 通知后，更改状态
		Ar.flush();
		subscribeEventDao.updateDeviceStatus(changeTime, nanosecond);
		subscribeEventDao.updateOrganStatus(changeTime, nanosecond);
	}

	public void parentRegistOrgan(String cmsId) {
		List<Organ> organList = Ar.of(Organ.class).find(
				"from Organ where cmsId=? and status=? and sync=? ", cmsId,
				Status.delete.name(), true);
		Set<Integer> ids = new HashSet<Integer>();
		for (Organ organ : organList) {
			if (!ids.contains(organ.getId())) {
				deleteOrgan(organ, ids);
			}
		}
		setOrganParent();
	}

	public void setOrganParent() {
		List<Organ> organs = Ar.of(Organ.class).find(
				"from Organ where status!=?", Status.delete.name());
		for (Organ organ : organs) {
			if (organ.getParentOrganId() != null && organ.getParent() == null) {
				Organ parent = Ar.of(Organ.class).get(organ.getParentOrganId());
				organ.setParent(parent);
				Ar.update(organ);
			}
		}
	}

	private void deleteOrgan(Organ organ, Set<Integer> ids) {
		ids.add(organ.getId());
		List<Organ> children = organ.getChildren();
		for (Organ child : children) {
			deleteOrgan(child, ids);
		}
		Ar.delete(organ);
		Ar.exesql("delete from authorization where resource_cms_id=? "
				+ "and resource_id=? and resource_type=?", organ.getCmsId(),
				organ.getId(), ResourceType.valueOf(organ.getType()).ordinal());
		Ar.exesql("delete from share where resource_cms_id=? "
				+ "and resource_id=? and resource_type=?", organ.getCmsId(),
				organ.getId(), ResourceType.valueOf(organ.getType()).ordinal());
	}

	public void initAccessIp() {
		List<String> cmsIds = Ar.of(GbPlatform.class).find(
				"select cmsId from GbPlatform where type=?",
				GbPlatformType.CHILD);
		DetachedCriteria criteria = DetachedCriteria
				.forClass(DeviceServer.class);
		if (cmsIds.size() > 0) {
			criteria.add(Restrictions.not(Restrictions.in("cmsId", cmsIds)));
		}
		criteria.add(Restrictions.eq("innerDevice", true));
		criteria.add(Restrictions.isNotNull("naming"));
		criteria.setProjection(Projections.property("naming"));
		List<String> namings = Ar.find(criteria);
		for (String naming : namings) {
			if (StringUtils.isNotBlank(naming)) {
				String[] temp = naming.split(":");
				accessIPMap.put(temp[1], temp[2]);
			}
		}
	}

	int pageSize = 5000;
	private Map<Long, AtomicInteger> finishFlushMap = new HashMap<Long, AtomicInteger>();

	public void flushStatus() {
		if (accessIPMap.size() == 0) {
			initAccessIp();
			System.out.println(accessIPMap.size());
		}
		long currentFlush = Long.valueOf((""+System.nanoTime()).substring(5,16));
		finishFlushMap.put(currentFlush, new AtomicInteger(0));
		gbUtils.createAccessServerCallableList(accessIPMap, masterAccessIP,
				currentFlush, this);
	}

	public void resetDeviceStatusByOnline() {
		autoScanDao.resetDeviceStatusByOnline();
		System.out.println("################################### end flush");
	}

	public void updateDeviceStatus(String cmsId, String accessIp,
			Set<String> serverIds) {
		MapSqlParameterSource paramMap = new MapSqlParameterSource();
		paramMap.addValue("cmsId", cmsId);
		paramMap.addValue("namingPart", accessIp + ":" + cmsId);
		paramMap.addValue("serverIds", serverIds);
		if (serverIds.size() > 0) {
			namedParameterJdbcTemplate
					.update("update device set online='ON' where cms_id=:cmsId  and server_id in(:serverIds) and online='OFF' and naming like concat(device_id,':%',:namingPart)",
							paramMap);
			namedParameterJdbcTemplate
					.update("update device set online='OFF' where cms_id=:cmsId  and server_id not in(:serverIds) and online='ON' and naming like concat(device_id,':%',:namingPart) ",
							paramMap);
		} else {
			jdbc.update(
					"update device set online='OFF' where cms_id=?  and online='ON' and naming like ?",
					cmsId, "%" + accessIp + ":" + cmsId);
		}
	}

	public void pushResource() throws InterruptedException {
		List<GbPlatform> gbPlatforms = Ar.of(GbPlatform.class).find(
				"from GbPlatform where type=? and autoPush=? and hasQuery=?",
				GbPlatformType.PARENT, true, true);
		for (GbPlatform gbPlatform : gbPlatforms) {
			Long sn = PlatformUtils.getSN();
			StringBuilder message = new StringBuilder();
			message.append(
					"<?xml version=\"1.0\"?><Query><CmdType>Catalog</CmdType><SN>")
					.append(sn).append("</SN><DeviceID>")
					.append(gbPlatform.getChildCmsId())
					.append("</DeviceID></Query>");
			RequestEntity entity = new RequestEntity();
			entity.init(message.toString(), gbPlatform.getCmsId());
			entity.setGbPlatform(gbPlatform);
			Timestamp changeTime = new Timestamp(System.currentTimeMillis());
			int nanosecond = changeTime.getNanos() / 1000;
			entity.setChangeTime(changeTime);
			entity.setNanosecond(nanosecond);
			registUtil.getSendResourceHelper(gbPlatform).sendResource(entity);
			registerDao.scanDeviceStatusByGbPlatformForPush(gbPlatform,
					changeTime, nanosecond);
			Thread.sleep(10000);
		}
	}

	public String rectify() {
		autoScanDao.rectify();
		return "@success";
	}

	/**
	 * 作为上级，系统启台后初始刷新订阅
	 */
	public void initSubscribe() {
		List<Subscribe> subscribes = Ar.of(Subscribe.class).find("status",
				SubscribeStatus.NORMAL);
		for (Subscribe subscribe : subscribes) {
			gbOuterDevice.sendSubscribe(subscribe.getToPlatformId(),
					subscribe.getPeriod());
		}
	}

	public Map<Long, AtomicInteger> getFinishFlushMap() {
		return finishFlushMap;
	}

	public void setFinishFlushMap(Map<Long, AtomicInteger> finishFlushMap) {
		this.finishFlushMap = finishFlushMap;
	}

	public String test() {
		Set<String> ids = new HashSet<String>();
		ids.add("121212");
		ids.add("121213");
		ids.add("121215");
		MapSqlParameterSource paramMap = new MapSqlParameterSource();
		paramMap.addValue("cmsId", "420300");
		paramMap.addValue("serverIds", ids);
		namedParameterJdbcTemplate
				.update("update device_server set online='OFF' where cms_id=:cmsId and server_id in(:serverIds)",
						paramMap);
		return "@success";
	}
}
