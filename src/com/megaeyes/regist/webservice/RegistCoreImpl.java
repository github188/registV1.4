package com.megaeyes.regist.webservice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jws.WebService;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import com.megaeyes.regist.bean.IDevice;
import com.megaeyes.regist.bean.IDeviceServer;
import com.megaeyes.regist.bean.IOrgan;
import com.megaeyes.regist.bean.IPlatform;
import com.megaeyes.regist.bean.MsgRecord;
import com.megaeyes.regist.bean.UserBean;
import com.megaeyes.regist.controllers.AutoScanController;
import com.megaeyes.regist.dao.AutoScanDao;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.OuterDeviceAlarm;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.domain.User;
import com.megaeyes.regist.enump.DeviceIdType;
import com.megaeyes.regist.enump.DircetionType;
import com.megaeyes.regist.enump.PTZType;
import com.megaeyes.regist.enump.PositionType;
import com.megaeyes.regist.enump.RoomType;
import com.megaeyes.regist.enump.SupplyLightType;
import com.megaeyes.regist.enump.UseType;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.sendResource.ConfigUtil;
import com.megaeyes.regist.utils.sendResource.GbDomainMapFactory;
import com.megaeyes.utils.Ar;

/**
 * 平台注册服务,根据平台cmsId组织平台机构树
 * 
 * @author molc1
 * 
 */
@WebService(serviceName = "RegistCoreService", endpointInterface = "com.megaeyes.regist.webservice.RegistCore")
public class RegistCoreImpl implements RegistCore {

	@Autowired
	private RegisterDao registerDao;

	public void setRegisterDao(RegisterDao registerDao) {
		this.registerDao = registerDao;
	}

	@Autowired
	private RegistUtil registUtil;

	public void setRegistUtil(RegistUtil registUtil) {
		this.registUtil = registUtil;
	}

	@Value("${refusePlatformRegist}")
	private boolean refusePlatformRegist;

	@Value("${insertDevicesSQL}")
	private String insertDevicesSQL;

	@Value("${insertServerSQL}")
	private String insertServerSQL;

	@Value("${insertOrganSQL}")
	private String insertOrganSQL;

	@Autowired
	private AutoScanDao autoScanDao;

	@Autowired
	private AutoScanController autoScanCtrl;

	@Autowired
	private JdbcTemplate jdbc;

	public String regist(IPlatform platform) {
		String cmsId = platform.getCmsId();
		Platform temp = Ar.of(Platform.class).one("cmsId", cmsId);
		if (temp == null) {
			String ownerCmsId = ConfigUtil.OWNER_PLATFORM_CMS_ID;
			Platform parent = null;
			if (cmsId.equals(ownerCmsId)) {
				// 如果为主平台注册
				Platform owner = Ar.of(Platform.class).one("cmsId", cmsId);
				if (owner == null) {
					owner = registerDao.setPlatform(platform, null);
					owner.setOwner(true);
					Ar.save(owner);
				} else {
					updateOwner(platform, owner);
				}
			} else {
				parent = PlatformUtils.getPlatformParent();
				registerDao.createPlatform(platform, parent);
			}

		} else {
			temp.setName(platform.getName());
			temp.setStatus(Status.update);
			temp.setServiceUrl(platform.getServiceUrl());
			temp.setEventServerIp(platform.getEventServerIp());
			temp.setEventServerPort(platform.getEventServerPort());
			Ar.update(temp);
		}
		registUtil.getRS(registerDao).parentPlatform();
		return "success";
	}

	// 更改主平台的信息
	private void updateOwner(IPlatform platform, Platform owner) {
		owner.setName(platform.getName());
		owner.setPassword(platform.getPassword());
		owner.setServiceUrl(platform.getServiceUrl());
		owner.setStatus(Status.add);
		owner.setEventServerIp(platform.getEventServerIp());
		owner.setEventServerPort(platform.getEventServerPort());
		Ar.update(owner);
	}

	@Override
	public String parentPlatformRegist(IPlatform parentBean,
			List<IPlatform> list) {
		Platform owner = PlatformUtils.getPlatformParent();
		Platform parent = Ar.of(Platform.class).one("cmsId",
				parentBean.getCmsId());
		if (parent == null) {
			parent = registerDao.createPlatform(parentBean, owner);
			parent.setParentCmsId(owner.getCmsId());
			Ar.update(parent);
		} else {
			parent.setName(parentBean.getName());
			parent.setStatus(Status.update);
			parent.setServiceUrl(parentBean.getServiceUrl());
			parent.setEventServerIp(parentBean.getEventServerIp());
			parent.setEventServerPort(parentBean.getEventServerPort());
			Ar.update(parent);
		}
		List<Platform> platforms = getPlatforms(list);
		setPlatformParent(platforms);
		// 向省平台注册服务器注册信息
		registUtil.getRS(registerDao).parentPlatform();
		return "success";
	}

	private List<Platform> getPlatforms(List<IPlatform> list) {
		List<Platform> platforms = new ArrayList<Platform>();
		for (IPlatform bean : list) {
			String cmsId = bean.getCmsId();
			Platform temp = Ar.of(Platform.class).one("cmsId", cmsId);
			if (temp == null) {
				temp = new Platform();
				temp.setCmsId(cmsId);
				temp.setName(bean.getName());
				temp.setPassword(bean.getPassword());
				temp.setServiceUrl(bean.getServiceUrl());
				temp.setStatus(Status.add);
				temp.setParentCmsId(bean.getParentCmsId());
				temp.setEventServerIp(bean.getEventServerIp());
				temp.setEventServerPort(bean.getEventServerPort());
				Ar.save(temp);
			} else {
				temp.setName(bean.getName());
				temp.setStatus(Status.update);
				temp.setServiceUrl(bean.getServiceUrl());
				temp.setEventServerIp(bean.getEventServerIp());
				temp.setEventServerPort(bean.getEventServerPort());
				Ar.update(temp);
			}
			platforms.add(temp);
		}
		return platforms;
	}

	/**
	 * 设置各子平台的父平台的parent, 因为各平台的父平台可能还没有在上级平台注册, 而子平台先注册,子平台这时找不到相关的Platform
	 * parent 所以要采取全部注册完毕后再调整父子关系的方式
	 * 
	 * @param platforms
	 */
	private void setPlatformParent(List<Platform> platforms) {
		for (Platform platform : platforms) {
			if (platform.getParentCmsId() != null) {
				platform.setParent((Platform) Ar.of(Platform.class).one(
						"cmsId", platform.getParentCmsId()));
				Ar.update(platform);
			}
		}
	}

	public static ConcurrentHashMap<String, MsgRecord> deviceRegistRecordMap = new ConcurrentHashMap<String, MsgRecord>();
	public static ConcurrentHashMap<String, MsgRecord> organRegistRecordMap = new ConcurrentHashMap<String, MsgRecord>();
	public static ConcurrentHashMap<String, MsgRecord> serverRegistRecordMap = new ConcurrentHashMap<String, MsgRecord>();

	void clearExpireRecord() {
		clearExpireDeviceRecord(new DeviceFinish(this));
		clearExpireDeviceRecord(new OrganFinish(this));
		clearExpireDeviceRecord(new VisFinish(this));
	}

	private void clearExpireDeviceRecord(IFinish finish) {
		Set<String> cmsIds = new HashSet<String>();
		ConcurrentHashMap<String, MsgRecord> map = finish.getMap();
		for (String cmsId : map.keySet()) {
			MsgRecord record = map.get(cmsId);
			Date startTime = record.getLastReveiveTime();
			Calendar last = Calendar.getInstance();
			last.setTime(startTime);
			last.add(Calendar.MILLISECOND, 600000);

			Calendar now = Calendar.getInstance();
			if (last.getTimeInMillis() < now.getTimeInMillis()) {
				cmsIds.add(cmsId);
			}
		}
		for (String cmsId : cmsIds) {
			MsgRecord record = map.get(cmsId);
			if (record.getSumNum() == minSize) {
				executeSave(record, finish.getSQL());
				finish.finish(record, cmsId);
			}
			map.remove(cmsId);
		}
	}

	/**
	 * 平台向注册服务器注册平台设备
	 */
	@Override
	public String deviceRegist(String cmsId, List<IDevice> deviceList) {
		if (refusePlatformRegist) {
			return "success";
		}
		MsgRecord record = getMsgRecord(deviceRegistRecordMap, cmsId,
				deviceList.size());
		for (IDevice device : deviceList) {
			Device dbDevice = new Device();
			dbDevice.setCmsId(cmsId);
			registerDao.setDeviceProfile(dbDevice, device);
			record.getDeviceParams().putIfAbsent(
					dbDevice.getDeviceId() + "_" + dbDevice.getOwnerId(),
					registerDao.getInsertDeviceSQLParams(dbDevice));
		}
		batchSave(record, insertDevicesSQL);
		deviceFinish(record, cmsId);
		return "success";
	}

	void visFinish(MsgRecord record, String cmsId) {
		if (record.isFinish()) {
			registUtil.getRS(registerDao).parentRegistVIS(cmsId);
		}
	}

	void deviceFinish(MsgRecord record, String cmsId) {
		if (record.isFinish()) {
			autoScanDao.resetDeviceByCmsId(cmsId);
			autoScanDao.createGbDeviceByCmsId(cmsId);
			autoScanDao.resetDeviceStatus();
			autoScanCtrl.subscribeNotify();
			registUtil.getRS(registerDao).parentRegistDevice(cmsId);
			GbDomainMapFactory.getInstance().initMap();
		}
	}

	void organFinish(MsgRecord record, String cmsId) {
		if (record.isFinish()) {
			autoScanDao.resetOrganByCmsId(cmsId);
			autoScanDao.resetPlatformOrgan(cmsId);
			autoScanDao.createGbOrganByCmsId(cmsId);
			autoScanDao.resetOrganStatus();
			autoScanCtrl.subscribeNotify();
			registUtil.getRS(registerDao).parentRegistOrgan(cmsId);
			GbDomainMapFactory.getInstance().initMap();
		}
	}

	private MsgRecord getMsgRecord(ConcurrentHashMap<String, MsgRecord> map,
			String cmsId, int size) {
		MsgRecord record = null;
		if (map.containsKey(cmsId)) {
			record = map.get(cmsId);
			record.setSumNum(size);
		} else {
			record = new MsgRecord();
			record.setQueryTime(new Date());
			record.setCmsId(cmsId);
			record.setSumNum(size);
			map.put(cmsId, record);
		}
		record.setLastReveiveTime(new Date());
		return record;
	}

	/**
	 * 获取所有下级平台信息
	 * 
	 * @return
	 */
	@Override
	public List<IPlatform> getPlatforms(String parentCmsId) {
		List<IPlatform> list = Ar.of(Platform.class).find(
				"from Platform where id!=? ", parentCmsId);
		List<IPlatform> beans = new ArrayList<IPlatform>();
		for (IPlatform platform : list) {
			beans.add(platform);
		}
		return beans;
	}

	/**
	 * 平台向注册服务器注册企业机构
	 */
	@Override
	public String organRegist(String cmsId, List<IOrgan> organList) {
		if (refusePlatformRegist) {
			return "success";
		}
		MsgRecord record = getMsgRecord(organRegistRecordMap, cmsId,
				organList.size());
		for (IOrgan organ : organList) {
			organ.setCmsId(cmsId);
			record.getDeviceParams().putIfAbsent(organ.getOrganId(),
					registerDao.getInsertOrganParams(organ));
		}
		batchSave(record, insertOrganSQL);
		organFinish(record, cmsId);
		return "success";
	}

	public String userRegist(String cmsId, List<UserBean> beans) {
		Session session = Ar.getSession();
		try {
			for (UserBean bean : beans) {
				bean.setCmsId(cmsId);
				User user = Ar.of(User.class).one("userId",
						bean.getId() + "_" + cmsId);
				if (bean.getStatus().equals(Status.add.name())) {
					if (user == null) {
						createUser(session, bean);
					} else {
						updateUser(session, user, bean);
					}
				} else if (bean.getStatus().equals(Status.update.name())) {
					if (user != null) {
						updateUser(session, user, bean);
					} else {
						createUser(session, bean);
					}
				} else if (bean.getStatus().equals(Status.delete.name())) {
					if (user != null) {
						deleteUserById(user.getId());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
		return "success";
	}

	private void deleteUserById(Integer userId) {
		jdbc.update("delete from r_user_role where user_id=?", userId);
		jdbc.update("delete from user where id=?", userId);
		jdbc.update(
				"delete from authorization where granted_id=? and granted_type=0",
				userId);
	}

	private void updateUser(Session session, User user, UserBean bean) {
		session.update(setUserprofile(user, bean));

	}

	private void createUser(Session session, UserBean bean) {
		User user = new User();
		user.setUserId(bean.getId() + "_" + bean.getCmsId());
		user.setCmsId(bean.getCmsId());
		session.save(setUserprofile(user, bean));
	}

	private User setUserprofile(User user, UserBean bean) {
		user.setLogonName(bean.getLogonName());
		user.setName(bean.getName());
		user.setNaming(bean.getNaming());
		user.setOrganId(bean.getOrganId());
		user.setPassword(bean.getPassword());
		user.setSex(bean.getSex());
		user.setChangeTime(bean.getChangeTime());
		user.setNanosecond(bean.getNanosecond());
		user.setStatus(bean.getStatus());
		user.setSync(bean.isSync());
		return user;
	}

	/**
	 * 根据cmsId获取平台信息
	 * 
	 */
	@Override
	public IPlatform getPlatform(String cmsId) {
		IPlatform platform = Ar.of(Platform.class).get(cmsId);
		return platform;
	}

	@Override
	public String getOuterDeviceAlarm(List<OuterDeviceAlarm> deviceAlarms,
			String cmsId) {
		Ar.exesql("delete from outer_device_alarm where cms_id=?", cmsId);
		for (OuterDeviceAlarm deviceAlarm : deviceAlarms) {
			Ar.save(deviceAlarm);
		}
		return "success";
	}

	

	@SuppressWarnings("unused")
	private String getStdId(String cmsId, String type, String entity) {
		StringBuilder id = new StringBuilder();
		// id.append(cmsId).append("0001");// 平台编号+行业编号
		id.append(cmsId).append("0000");// 测试中说明0000是正常的
		id.append(DeviceIdType.getTypeCode(type)); // 设备类型编号
		id.append("0"); // 网络类型编号
		id.append(getBGId(DeviceIdType.valueOf(type), cmsId, entity));
		return id.toString();
	}

	private ConcurrentMap<DeviceIdType, Integer> gbIdMap = new ConcurrentHashMap<DeviceIdType, Integer>();

	@SuppressWarnings("rawtypes")
	private synchronized String getBGId(DeviceIdType type, String cmsId,
			String entity) {
		if (gbIdMap.containsKey(type)) {
			Integer deviceNO = gbIdMap.get(type) + 1;
			gbIdMap.put(type, deviceNO);
			return "000000".substring(0, 6 - deviceNO.toString().length())
					+ deviceNO;

		} else {
			List list = Ar.sql("select max(substr(std_id,15)) from " + entity
					+ " where type=? and cms_id=?", type.name(), cmsId);
			if (list.get(0) == null) {
				gbIdMap.put(type, 1);
			} else {
				gbIdMap.put(type, Integer.parseInt((String) list.get(0)) + 1); // 已经存在的情况下，需要+1，要不有重复
			}
			return "000000".substring(0, 6 - gbIdMap.get(type).toString()
					.length())
					+ gbIdMap.get(type);
		}
	}

	@Override
	public String visRegist(String cmsId, List<IDeviceServer> serverList) {
		if (refusePlatformRegist) {
			return "success";
		}
		MsgRecord record = getMsgRecord(serverRegistRecordMap, cmsId,
				serverList.size());
		for (IDeviceServer server : serverList) {
			server.setCmsId(cmsId);
			record.getDeviceParams().putIfAbsent(server.getServerId(),
					registerDao.getInsertServerParams(server,true));
		}
		batchSave(record, insertServerSQL);
		visFinish(record, cmsId);
		return "success";
	}

	private int minSize = 1000;
	private int maxSize = 20000;

	private void batchSave(MsgRecord record, String sql) {
		if (record.getSumNum() < minSize
				|| record.getDeviceParams().size() > maxSize) {
			executeSave(record, sql);
		} else {
			ClearExpire.getInstance(this).startClearThread();
		}
	}

	private void executeSave(MsgRecord record, String sql) {
		List<Object[]> params = new ArrayList<Object[]>();
		Set<String> keys = record.getDeviceParams().keySet();
		for (String key : keys) {
			params.add(record.getDeviceParams().remove(key));
			if (params.size() > 100) {
				jdbc.batchUpdate(sql, params);
				params.clear();
			}
		}
		jdbc.batchUpdate(sql, params);
		params.clear();
		record.setFinish(true);
	}

	String getInsertDevicesSQL() {
		return insertDevicesSQL;
	}

	void setInsertDevicesSQL(String insertDevicesSQL) {
		this.insertDevicesSQL = insertDevicesSQL;
	}

	String getInsertServerSQL() {
		return insertServerSQL;
	}

	void setInsertServerSQL(String insertServerSQL) {
		this.insertServerSQL = insertServerSQL;
	}

	String getInsertOrganSQL() {
		return insertOrganSQL;
	}

	void setInsertOrganSQL(String insertOrganSQL) {
		this.insertOrganSQL = insertOrganSQL;
	}
}
