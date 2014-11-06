package com.megaeyes.regist.dao;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.mega.jdom.Document;
import com.mega.jdom.Element;
import com.mega.jdom.output.Format;
import com.mega.jdom.output.XMLOutputter;
import com.megaeyes.regist.bean.IDevice;
import com.megaeyes.regist.bean.IDeviceServer;
import com.megaeyes.regist.bean.IOrgan;
import com.megaeyes.regist.bean.IPlatform;
import com.megaeyes.regist.bean.RedistributeParam;
import com.megaeyes.regist.bean.RegistParam;
import com.megaeyes.regist.bean.Require;
import com.megaeyes.regist.bean.sendResource.Item;
import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.domain.Authorization;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.DeviceStatus;
import com.megaeyes.regist.domain.GbOrgan;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.OrganServer;
import com.megaeyes.regist.domain.OrganStatus;
import com.megaeyes.regist.domain.OuterDeviceAlarm;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.domain.PlatformOrgan;
import com.megaeyes.regist.domain.ResourceStatus;
import com.megaeyes.regist.domain.Role;
import com.megaeyes.regist.domain.ServerStatus;
import com.megaeyes.regist.domain.Share;
import com.megaeyes.regist.domain.TaskStatus;
import com.megaeyes.regist.domain.User;
import com.megaeyes.regist.enump.DeviceIdType;
import com.megaeyes.regist.enump.DircetionType;
import com.megaeyes.regist.enump.PTZType;
import com.megaeyes.regist.enump.PositionType;
import com.megaeyes.regist.enump.RoomType;
import com.megaeyes.regist.enump.SupplyLightType;
import com.megaeyes.regist.enump.UseType;
import com.megaeyes.regist.other.DeviceType;
import com.megaeyes.regist.other.GrantedType;
import com.megaeyes.regist.other.RegistCallable;
import com.megaeyes.regist.other.ResourceType;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.GbPlatformType;
import com.megaeyes.regist.utils.HttpclientUtils;
import com.megaeyes.regist.utils.Page;
import com.megaeyes.regist.utils.sendResource.ConfigUtil;
import com.megaeyes.regist.webservice.PlatformUtils;
import com.megaeyes.regist.webservice.RegistUtil;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

/**
 * 
 * @author molc1
 */
@Component("registDao")
public class RegisterDao {

	@Autowired
	private JdbcTemplate jdbc;

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

    @Autowired
	private TransactionTemplate txTemplate;


	@Autowired
	private RegistUtil registUtil;

	private ExecutorService executorService = Executors.newFixedThreadPool(10);
	private CompletionService<String> taskCompletionService = new ExecutorCompletionService<String>(
			executorService);

	public void createDevice(Device device) {
		Device dbDevice = new Device();
		dbDevice = setDeviceProfile(dbDevice, device);
		Ar.save(dbDevice);
	}

	private Device setDeviceProfile(Device dbDevice, Device device) {
		String path = device.getPath();
		dbDevice.setCmsId(device.getCmsId());
		dbDevice.setDeviceId(device.getDeviceId());
		dbDevice.setLocation(device.getLocation());
		dbDevice.setName(device.getName());
		dbDevice.setNaming(device.getNaming());
		dbDevice.setOuterPlatforms(device.getOuterPlatforms());
		dbDevice.setOwnerId(path.substring(path.lastIndexOf("/") + 1));
		dbDevice.setPath(path);
		dbDevice.setPermission(device.getPermission());
		dbDevice.setStatus(device.getStatus());
		dbDevice.setSupportScheme(device.isSupportScheme());
		dbDevice.setType(device.getType());
		return dbDevice;
	}

	public List<Device> getDevicesByOrgan(int currentPage, String organId,
			String type, String name, String cmsId) {
		DetachedCriteria criteria = getDevicesByOrganSQL(organId, type, name,
				cmsId);
		List<Device> devices = Ar.find(criteria, (currentPage - 1) * 100, 100);
		return devices;
	}

	public int getDevicesByOrganCount(String organId, String type, String name,
			String cmsId) {
		DetachedCriteria criteria = getDevicesByOrganSQL(organId, type, name,
				cmsId);
		criteria.setProjection(Projections.count("id"));
		return Ar.one(criteria);
	}


	private DetachedCriteria getDevicesByOrganSQL(String organId, String type,
			String name, String cmsId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Device.class);
		criteria.add(Restrictions.eq("type", type));
		criteria.add(Restrictions.eq("cmsId", cmsId));
		criteria.add(Restrictions.ne("status", Status.delete.name()));
		criteria.add(Restrictions.like("path", organId, MatchMode.END));

		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name, MatchMode.START));
		}
		return criteria;
	}


	public DetachedCriteria getUserCrit(Class<?> clazz, String id) {
		DetachedCriteria criteria = DetachedCriteria.forClass(clazz);
		DetachedCriteria crit = criteria.createCriteria("users");
		crit.add(Restrictions.idEq(id));
		return criteria;
	}

	public List<Platform> getPlatformChildren(Integer parentId) {
		return Ar.of(Platform.class).find("parent.id", parentId);
	}


	public void deleteDevice(Device device) {
		if (device != null) {
			Device dbDevice = Ar.of(Device.class).get(device.getId());
			Ar.delete(dbDevice);
		}
	}

	public void resultDeal(Device device) {
		Device temp = Ar.of(Device.class).get(device.getId());
		if (device.getStatus().equals(Status.delete.name())) {
			Ar.delete(temp);
		} else {
			temp.setStatus(Status.normal.name());
			Ar.update(temp);
		}
	}

	public List<Device> getRegistDevices(String cmsId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Device.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		criteria.add(Restrictions.eq("sync", false));
		criteria.addOrder(Order.asc("changeTime"));
		criteria.addOrder(Order.asc("nanosecond"));
		List<Device> deviceList = Ar.find(criteria, 0, getPageSize());
		return deviceList;
	}

	public List<DeviceServer> getRegistDeviceServers(String cmsId) {
		DetachedCriteria criteria = DetachedCriteria
				.forClass(DeviceServer.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		criteria.add(Restrictions.eq("sync", false));
		criteria.addOrder(Order.asc("changeTime"));
		criteria.addOrder(Order.asc("nanosecond"));
		List<DeviceServer> servers = Ar.find(criteria, 0, getPageSize());
		return servers;
	}

	public List<Organ> getRegistOrgans(String cmsId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Organ.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		criteria.add(Restrictions.eq("sync", false));
		criteria.addOrder(Order.asc("changeTime"));
		criteria.addOrder(Order.asc("nanosecond"));
		List<Organ> organs = Ar.find(criteria, 0, getPageSize());
		return organs;
	}

	/**
	 * @param registRunable
	 * @param tableName
	 */
	public void changeStatus(final RegistParam param, final String tableName) {
		txTemplate.execute(new TransactionCallback<String>() {
			@Override
			public String doInTransaction(TransactionStatus status) {
				jdbc.execute("set foreign_key_checks=0");
				updateStatus(param, tableName);
				deleteStatus(param, tableName);
				jdbc.execute("set foreign_key_checks=1");
				return "success";
			}
		});
	}
	
	public void changeDeviceStatus(final RegistParam param){
		txTemplate.execute(new TransactionCallback<String>() {
			@Override
			public String doInTransaction(TransactionStatus status) {
				jdbc.execute("set foreign_key_checks=0");
				updateStatus(param, "device");
				clearByDevice(param.getCmsId());
				deleteStatus(param, "device");
				jdbc.execute("set foreign_key_checks=1");
				return "success";
			}
		});
	}
	
	private void clearByDevice(String cmsId){
		jdbc.update("delete au from authorization au inner join device d on(au.resource_id=d.id and au.resource_type=0 and d.cms_id=? and d.status='delete')",cmsId);
		jdbc.update("delete gbd from gb_device gbd inner join device d on(gbd.device_id=d.id and d.status='delete' and d.cms_id=?)",cmsId);
		jdbc.update("delete ds from device_status ds inner join device d on(ds.device_id=d.id and d.status='delete' and d.cms_id=?)",cmsId);

	}
	
	public void changeDeviceStatus(final String cmsId,
			final Timestamp changeTime, final int nanosecond){
		txTemplate.execute(new TransactionCallback<String>() {
			@Override
			public String doInTransaction(TransactionStatus status) {
				jdbc.execute("set foreign_key_checks=0");
				updateStatus(cmsId, "device", changeTime, nanosecond);
				clearByDevice(cmsId);
				deleteStatus(cmsId, "device", changeTime, nanosecond);
				jdbc.execute("set foreign_key_checks=1");
				return "success";
			}
		});
	}
	
	/**
	 * @param registRunable
	 * @param tableName
	 */
	public void changeOrganStatus(final RegistParam param) {
		txTemplate.execute(new TransactionCallback<String>() {
			@Override
			public String doInTransaction(TransactionStatus status) {
				jdbc.execute("set foreign_key_checks=0");
				updateStatus(param, "organ");
				clearByOrgan(param.getCmsId());
				deleteStatus(param, "organ");
				jdbc.execute("set foreign_key_checks=1");
				return "success";
			}
		});
	}
	
	public void changeOrganStatus(final String cmsId,
			final Timestamp changeTime, final int nanosecond) {
		txTemplate.execute(new TransactionCallback<String>() {
			@Override
			public String doInTransaction(TransactionStatus status) {
				jdbc.execute("set foreign_key_checks=0");
				updateStatus(cmsId, "organ", changeTime, nanosecond);
				clearByOrgan(cmsId);
				deleteStatus(cmsId, "organ", changeTime, nanosecond);
				jdbc.execute("set foreign_key_checks=1");
				return "success";
			}
		});
	}
	
	private void clearByOrgan(String cmsId){
		jdbc.update("delete au from authorization au inner join platform_organ po on(au.resource_id=po.id and au.resource_type=1) join organ o on(po.source_id=o.id and po.source_type='organ' and o.status='delete' and o.cms_id=?)",cmsId);
		jdbc.update("delete po from platform_organ po inner join organ o on(po.source_id=o.id and po.source_type='organ' and o.status='delete' and o.cms_id=?)",cmsId);
		jdbc.update("delete gbo from gb_organ gbo inner join organ o on(gbo.source_id=o.id and gbo.source_type='organ' and o.status='delete' and o.cms_id=?)",cmsId);
		jdbc.update("delete os from organ_status os inner join organ o on(os.organ_id=o.id and o.status='delete' and o.cms_id=?)",cmsId);
	}
	
	

	public void updateStatus(RegistParam param, String tableName) {
		jdbc.update(
				"update "
						+ tableName
						+ " set sync=1 where cms_id=? and status!=? and sync=0 and (change_time>? or  (change_time=? and nanosecond>=?)) and (change_time<? or (change_time=? and nanosecond<=?))",
				param.getCmsId(), Status.delete.name(), param
						.getBeginResourceStatus().getChangeTime(), param
						.getBeginResourceStatus().getChangeTime(), param
						.getBeginResourceStatus().getNanosecond(), param
						.getEndResourceStatus().getChangeTime(), param
						.getEndResourceStatus().getChangeTime(), param
						.getEndResourceStatus().getNanosecond());
	}

	public void updateStatus(String cmsId, String tableName,
			Timestamp changeTime, int nanosecond) {
		jdbc.update(
				"update "
						+ tableName
						+ " set sync=1,status='normal' where cms_id=? and sync=0 and status!=? and (change_time<? or (change_time=? and nanosecond<?))",
				cmsId, Status.delete.name(), changeTime, changeTime, nanosecond);
	}

	public void deleteStatus(RegistParam param, String tableName) {
		jdbc.update(
				"delete from "
						+ tableName
						+ " where cms_id=?  and status=? and (change_time>? or  (change_time=? and nanosecond>=?)) and (change_time<? or (change_time=? and nanosecond<=?))",
				param.getCmsId(), Status.delete.name(), param
						.getBeginResourceStatus().getChangeTime(), param
						.getBeginResourceStatus().getChangeTime(), param
						.getBeginResourceStatus().getNanosecond(), param
						.getEndResourceStatus().getChangeTime(), param
						.getEndResourceStatus().getChangeTime(), param
						.getEndResourceStatus().getNanosecond());
	}

	public void deleteStatus(String cmsId, String tableName,
			Timestamp changeTime, int nanosecond) {
		jdbc.update(
				"delete from "
						+ tableName
						+ " where cms_id=? and status=? and (change_time<? or (change_time=? and nanosecond<?))",
				cmsId, Status.delete.name(), changeTime, changeTime, nanosecond);
	}

	public void updateStatus(String tableName, Integer resourceId) {
		jdbc.update(
				"update "
						+ tableName
						+ " set sync=1,status='normal' where sync=0 and status!=? and id=?",
				Status.delete.name(), resourceId);
	}

	public void deleteStatus(String tableName, Integer resourceId) {
		Ar.exesql("delete from " + tableName + " where status=? and id=?",
				Status.delete.name(), resourceId);
	}

	private static int pageSize = 0;

	public int getPageSize() {
		if (pageSize == 0) {
			String pageSizeStr = ConfigUtil.SEND_TO_REGIST_PAGESIZE;
			if (StringUtils.isNumeric(pageSizeStr)) {
				pageSize = Integer.valueOf(pageSizeStr);
			} else {
				pageSize = 1000;
			}
		}
		return pageSize;
	}

	public List<User> getRoleUser(Role r) {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		DetachedCriteria crit = criteria.createCriteria("roles");
		crit.add(Restrictions.idEq(r.getId()));
		List<User> users = Ar.find(criteria);
		return users;
	}




	public Map<String, String> getOtherMap(String ids) {
		if (ids == null) {
			return null;
		}
		Map<String, String> idsMap = new HashMap<String, String>();
		String[] idsArr = ids.split("__");
		for (String id : idsArr) {
			if (id.length() > 0) {
				idsMap.put(id, id);
			}
		}
		return idsMap;
	}

	public Map<String, String> getVicMap(String vicIds) {
		if (vicIds == null) {
			return null;
		}
		Map<String, String> vicIdsMap = new HashMap<String, String>();
		String[] vicIdsArr = vicIds.split("__");
		for (String vicId : vicIdsArr) {
			if (vicId.length() > 0) {
				vicIdsMap.put(vicId.substring(0, vicId.lastIndexOf("_")),
						vicId.substring(vicId.lastIndexOf("_") + 1));
			}
		}
		return vicIdsMap;
	}

	public void getAllChildren(Set<String> cmsIds, Platform platform) {
		List<Platform> children = platform.getChildren();
		for (Platform child : children) {
			cmsIds.add(child.getCmsId());
			if (child.getChildren().size() > 0) {
				getAllChildren(cmsIds, child);
			}
		}
	}

	public void getOuterPlatforms(List<Platform> platforms, String cmsId,
			String name) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Share.class);
		criteria.add(Restrictions.eq("resourceCmsId", cmsId));
		criteria.setProjection(Projections.distinct(Projections
				.property("platformCmsId")));
		List<String> cmsIds = Ar.find(criteria);
		if (cmsIds.size() > 0) {
			DetachedCriteria crit = DetachedCriteria.forClass(Platform.class);
			crit.add(Restrictions.in("id", cmsIds));
			if (StringUtils.isNotBlank(name)) {
				crit.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
			}
			List<Platform> tempPlatforms = Ar.find(crit);
			platforms.addAll(tempPlatforms);
		}
	}

	public Map<String, String> getIdsMap(Require r) {
		if (r.getResourceType().equals(ResourceType.IPVIC.name())) {
			return getVicMap(r.getIpvicIds());
		} else if (r.getResourceType().equals(ResourceType.VIC.name())) {
			return getVicMap(r.getVicIds());
		} else if (r.getResourceType().equals(ResourceType.AIC.name())) {
			return getOtherMap(r.getAicIds());
		} else if (r.getResourceType().equals(ResourceType.ORGAN.name())) {
			return getOtherMap(r.getOrganIds());
		} else {
			Map<String, String> map = new HashMap<String, String>();
			return map;
		}
	}

	public Map<String, String> getIdsMapForRedistribution(RedistributeParam r) {
		if (r.getResourceType().equals(ResourceType.IPVIC.name())) {
			return getOtherMap(r.getIPVICIds());
		} else if (r.getResourceType().equals(ResourceType.VIC.name())) {
			return getOtherMap(r.getVICIds());
		} else if (r.getResourceType().equals(ResourceType.AIC.name())) {
			return getOtherMap(r.getAICIds());
		} else if (r.getResourceType().equals(ResourceType.ORGAN.name())) {
			return getOtherMap(r.getORGANIds());
		} else {
			Map<String, String> map = new HashMap<String, String>();
			return map;
		}
	}

	public void updatePermission(String[] resourceIds, String grantedId,
			String grantedType, ResourceType resourceType) {
		for (String resourceId : resourceIds) {
			if (resourceId.length() == 0) {
				return;
			}
			if (resourceType.equals(ResourceType.VIC)
					|| resourceType.equals(ResourceType.IPVIC)) {
				String item = resourceId
						.substring(resourceId.lastIndexOf("_") + 1);
				resourceId = resourceId.substring(0,
						resourceId.lastIndexOf("_"));
				if (item.equals("00000")) {
					deleteAuthorization(grantedId, grantedType, resourceType,
							resourceId);
				} else {
					updateAuthorization(grantedId, grantedType, resourceType,
							resourceId, item);
				}
			} else {
				deleteAuthorization(grantedId, grantedType, resourceType,
						resourceId);
			}

		}
	}

	public void updateShare(String[] resourceIds, String platformCmsId,
			ResourceType resourceType) {
		for (String resourceId : resourceIds) {
			if (resourceId.length() == 0) {
				return;
			}
			if (resourceType.equals(ResourceType.VIC)
					|| resourceType.equals(ResourceType.IPVIC)) {
				String item = resourceId
						.substring(resourceId.lastIndexOf("_") + 1);
				resourceId = resourceId.substring(0,
						resourceId.lastIndexOf("_"));
				if (item.equals("00000")) {
					deleteShare(platformCmsId, resourceType, resourceId);
				} else {
					updateShare(platformCmsId, resourceType, resourceId, item);
				}
			} else {
				deleteShare(platformCmsId, resourceType, resourceId);
			}

		}
	}

	private void deleteAuthorization(String grantedId, String grantedType,
			ResourceType resourceType, String resourceId) {
		Ar.exesql("delete from authorization where resource_id=? and "
				+ "resource_type=? and granted_id=? and granted_type=?",
				resourceId, resourceType.ordinal(), grantedId, GrantedType
						.valueOf(grantedType).ordinal());
	}

	private void updateAuthorization(String grantedId, String grantedType,
			ResourceType resourceType, String resourceId, String item) {
		Ar.exesql("update authorization set item=? where resource_id=? and "
				+ "resource_type=? and granted_id=? and granted_type=?", item,
				resourceId, resourceType.ordinal(), grantedId, GrantedType
						.valueOf(grantedType).ordinal());
	}

	private void deleteShare(String platformCmsId, ResourceType resourceType,
			String resourceId) {
		Ar.exesql("delete from share where resource_id=? and "
				+ "resource_type=? and platform_cms_id=?", resourceId,
				resourceType.ordinal(), platformCmsId);
	}

	private void updateShare(String platformCmsId, ResourceType resourceType,
			String resourceId, String item) {
		Ar.exesql("update share set item=? where resource_id=? and "
				+ "resource_type=? and platform_cms_id=?", item, resourceId,
				resourceType.ordinal(), platformCmsId);
	}


	public void accessSelf(Map<String, String> map, String resource) {
		HttpGet accessSelf = HttpclientUtils.accessSelf(map, resource);
		HttpClient httpclient = new DefaultHttpClient();
		try {
			httpclient.execute(accessSelf);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void accessSelfVisPost(Map<String, String> map, String resource) {
		HttpPost accessSelf = HttpclientUtils.accessSelfViaPost(map, resource);
		HttpClient httpclient = new DefaultHttpClient();
		try {
			httpclient.execute(accessSelf);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<String, Platform> getPlatformMap(List<Platform> platforms) {
		Map<String, Platform> map = new HashMap<String, Platform>();
		Map<String, Platform> childrenMap = new HashMap<String, Platform>();
		for (Platform platform : platforms) {
			if (platform.getParentCmsId() == null) {
				map.put(platform.getCmsId(), platform);
			} else {
				childrenMap.put(platform.getCmsId(), platform);
			}
		}
		for (Platform platform : childrenMap.values()) {
			if (childrenMap.get(platform.getParentCmsId()) != null) {
				childrenMap.get(platform.getParentCmsId()).getChildren()
						.add(platform);
			} else if (map.get(platform.getParentCmsId()) != null) {
				map.get(platform.getParentCmsId()).getChildren().add(platform);
			} else {
				map.put(platform.getCmsId(), platform);
			}
		}
		return map;
	}


	public Map<String, OuterDeviceAlarm> getSchemeSetting(String cmsId) {
		Map<String, OuterDeviceAlarm> map = new HashMap<String, OuterDeviceAlarm>();
		List<OuterDeviceAlarm> outerDeviceAlarms = Ar
				.of(OuterDeviceAlarm.class).find("cmsId", cmsId);
		for (OuterDeviceAlarm temp : outerDeviceAlarms) {
			map.put(temp.getDeviceNaming() + "_" + temp.getDeviceType(), temp);
		}
		return map;
	}

	
	int deviceSize = 10000;

	// This method writes a DOM document to a file
	private static void writeXmlFile(Document doc, String filename) {
		OutputStream os = null;
		try {
			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat()
					.setEncoding("GBK"));
			os = new BufferedOutputStream(new FileOutputStream(new File(
					filename)));
			out.output(doc, os);
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {

				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void createStaticFileByVm(Invocation inv) {
		String cmsId = inv.getRequest().getParameter("cmsId");
		String naming = inv.getRequest().getParameter("naming");
		Organ organ = (Organ) inv.getModel("platform");
		List<Organ> topOrgans = (List<Organ>) inv.getModel("topOrgans");
		Map<Organ, List<Device>> organDevices = (Map<Organ, List<Device>>) inv
				.getModel("organDevicesMap");
		Map<String, OuterDeviceAlarm> schemeMap = (Map<String, OuterDeviceAlarm>) inv
				.getModel("schemeSetting");
		createStaticFileByVm(cmsId, naming, organ, topOrgans, organDevices,
				schemeMap);
	}

	public void createStaticFileByVm(String cmsId, String naming, Organ organ,
			List<Organ> topOrgans, Map<Organ, List<Device>> organDevices,
			Map<String, OuterDeviceAlarm> schemeMap) {
		if (naming == null) {
			return;
		}
		try {
			System.out
					.println("###########################################begin");
			Document doc = new Document();
			Element root = new Element("organ");
			root.setAttribute("id", organ.getOrganId());
			root.setAttribute("name", organ.getName());
			root.setAttribute("isShare", "true");
			root.setAttribute("isPlatform", "true");
			doc.setRootElement(root);

			for (Organ topOrgan : topOrgans) {
				if (organDevices.containsKey(topOrgan)) {
					generalTreeNode(topOrgan, root, doc, organDevices,
							schemeMap);
				}
			}
			String id = naming.substring(0, 31) + "_"
					+ naming.substring(naming.length() - 6);
			writeXmlFile(doc, ConfigUtil.STATIC_PATH + "/" + id + "_" + cmsId
					+ ".xml");
			System.out
					.println("###########################################end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createStaticFileByEntity(HttpEntity entity, Invocation inv) {
		OutputStream out = null;
		try {
			String cmsId = inv.getRequest().getParameter("cmsId");
			String naming = inv.getRequest().getParameter("naming");
			String id = naming.substring(0, 31) + "_"
					+ naming.substring(naming.length() - 6);
			File file = new File(ConfigUtil.STATIC_PATH + "/" + id + "_"
					+ cmsId + ".xml");
			out = new BufferedOutputStream(new FileOutputStream(file));
			entity.writeTo(out);
			out.flush();
			out.close();
			respStaticFile(inv, file);
		} catch (Exception e) {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void generalTreeNode(Organ organ, Element parent, Document doc,
			Map<Organ, List<Device>> organDevices,
			Map<String, OuterDeviceAlarm> schemeMap) {
		List<Device> devices = organDevices.get(organ);
		Element organEL = new Element("organ");
		organEL.setAttribute("id", organ.getOrganId());
		organEL.setAttribute("name", organ.getName());
		organEL.setAttribute("isShare", "true");
		organEL.setAttribute("isPlatform", "false");
		parent.addContent(organEL);
		if (devices != null) {
			for (Device device : devices) {
				OuterDeviceAlarm outerDeviceAlarm = schemeMap.get(device
						.getNaming() + "_VideoInputChannel");
				if (device.getType().equals("VIC")) {
					organEL.addContent(createCameraEl(doc, device,
							outerDeviceAlarm, "general_camera_vic"));
				} else if (device.getType().equals("IPVIC")) {
					organEL.addContent(createCameraEl(doc, device,
							outerDeviceAlarm, "ip_camera_vic"));
				} else if (device.getType().equals("AIC")) {
					Element AICEL = new Element("alarm_input_channel");
					AICEL.setAttribute("id", device.getDeviceId());
					AICEL.setAttribute("name", device.getName());
					AICEL.setAttribute("installLocation", "");
					AICEL.setAttribute("note", "");
					AICEL.setAttribute("naming", device.getNaming());
					if (outerDeviceAlarm != null) {
						AICEL.setAttribute("Associateid",
								outerDeviceAlarm.getAlarmDeviceId());
						AICEL.setAttribute("AlarmSchemeid",
								outerDeviceAlarm.getSchemeId());
					} else {
						AICEL.setAttribute("Associateid", "");
						AICEL.setAttribute("AlarmSchemeid", "");
					}
					AICEL.setAttribute("scheme", "" + device.isSupportScheme());
					AICEL.setAttribute("isShare", "true");
					AICEL.setAttribute("typeId", "");
					AICEL.setAttribute("typeName", "");
					organEL.addContent(AICEL);
				}
			}
		}
		if (organ.getChildren().size() > 0) {
			for (Organ child : organ.getChildren()) {
				if (organDevices.containsKey(child)) {
					generalTreeNode(child, organEL, doc, organDevices,
							schemeMap);
				}
			}
		}
	}

	private Element createCameraEl(Document doc, Device device,
			OuterDeviceAlarm outerDeviceAlarm, String subType) {
		Element VICEL = new Element("video_input_channel");
		VICEL.setAttribute("id", device.getDeviceId());
		VICEL.setAttribute("subType", subType);
		VICEL.setAttribute("name", device.getName());
		VICEL.setAttribute("naming", device.getNaming());
		if (outerDeviceAlarm != null) {
			VICEL.setAttribute("Associateid",
					outerDeviceAlarm.getAlarmDeviceId());
			VICEL.setAttribute("AlarmSchemeid", outerDeviceAlarm.getSchemeId());
		} else {
			VICEL.setAttribute("Associateid", "");
			VICEL.setAttribute("AlarmSchemeid", "");
		}
		VICEL.setAttribute("scheme", "" + device.isSupportScheme());
		VICEL.setAttribute("isShare", "true");
		if (device.getPermission().substring(2, 3).equals("1")) {
			VICEL.setAttribute("hasPan", "true");
		} else {
			VICEL.setAttribute("hasPan", "false");
		}
		VICEL.setAttribute("x", device.getLongitude());
		VICEL.setAttribute("y", device.getLatitude());
		VICEL.setAttribute("z", device.getGpsZ());
		return VICEL;
	}

	public void getUserByCmsId(Invocation inv, Require r, Page page) {
		String platformCmsId = PlatformUtils.getCmsId(inv);
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		criteria.add(Restrictions.eq("cmsId", platformCmsId));
		if (StringUtils.isNotBlank(r.getOwnerOrganId())
				&& !r.getOwnerOrganId().equals("all")) {
			criteria.add(Restrictions.eq("organId", r.getOwnerOrganId()));
			inv.addModel("ownerOrgan",
					Ar.of(Organ.class).get(r.getOwnerOrganId()));
		}
		if (StringUtils.isNotBlank(r.getName())) {
			criteria.add(Restrictions.like("logonName", r.getName(),
					MatchMode.ANYWHERE));
		}
		page.setRecordCount(criteria);
		List<User> users = Ar.find(criteria, page.getFirstResult(),
				page.getPageSize());
		inv.addModel("grantedList", users);
	}

	public void getGrantedRole(Invocation inv, Require r, Page page) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Role.class);
		if (StringUtils.isNotBlank(r.getName())) {
			criteria.add(Restrictions.like("name", r.getName(),
					MatchMode.ANYWHERE));
		}
		criteria.add(Restrictions.eq("cmsId", PlatformUtils.getCmsId(inv)));
		page.setRecordCount(criteria);
		List<Role> roles = Ar.find(criteria, page.getFirstResult(),
				page.getPageSize());
		inv.addModel("grantedList", roles);

	}

	public void getSelectedUserByCmsId(Invocation inv, Require r, Page page,
			Set<String> ids) {
		if (ids.size() == 0) {
			return;
		}
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		criteria.add(Restrictions.eq("cmsId", PlatformUtils.getCmsId(inv)));
		criteria.add(Restrictions.in("id", ids));
		if (StringUtils.isNotBlank(r.getOwnerOrganId())
				&& !r.getOwnerOrganId().equals("all")) {
			criteria.add(Restrictions.eq("organId", r.getOwnerOrganId()));
			inv.addModel("ownerOrgan",
					Ar.of(Organ.class).get(r.getOwnerOrganId()));
		}
		if (StringUtils.isNotBlank(r.getName())) {
			criteria.add(Restrictions.like("logonName", r.getName(),
					MatchMode.ANYWHERE));
		}
		page.setRecordCount(criteria);
		List<User> users = Ar.find(criteria, page.getFirstResult(),
				page.getPageSize());
		inv.addModel("grantedList", users);
	}

	public void getSelectedRoleByCmsId(Invocation inv, String platformCmsId,
			String name, Page page, Set<String> ids) {
		if (ids.size() == 0) {
			return;
		}
		DetachedCriteria criteria = DetachedCriteria.forClass(Role.class);
		criteria.add(Restrictions.in("id", ids));
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name));
		}
		criteria.equals(Restrictions.eq("cmsId", platformCmsId));
		page.setRecordCount(criteria);
		List<Role> roles = Ar.find(criteria, page.getFirstResult(),
				page.getPageSize());
		inv.addModel("grantedList", roles);

	}

	/**
	 * 获取所有不是自己上级的平台,除国标平台
	 * 
	 * @param inv
	 * @param cmsId
	 */
	public void getPlatforms(Invocation inv, String cmsId, String name) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Platform.class);
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		}
		criteria.add(Restrictions.not(Restrictions.in("id",
				getAllParents(cmsId))));

		List<String> cmsIds = Ar.of(GbPlatform.class).find(
				"select cmsId from GbPlatform where type=?",
				GbPlatformType.CHILD);
		if (cmsIds.size() > 0) {
			criteria.add(Restrictions.not(Restrictions.in("id", cmsIds)));
		}

		List<Platform> platforms = Ar.find(criteria);
		inv.addModel("platforms", platforms);
	}

	public List<String> getAllParents(String cmsId) {
		List<String> ids = new ArrayList<String>();
		ids.add(cmsId);
		Platform platform = Ar.of(Platform.class).one("cmsId",cmsId);
		Platform parent = platform.getParent();
		while (parent != null) {
			ids.add(parent.getCmsId());
			parent = parent.getParent();
		}
		return ids;
	}

	public List<Platform> getSharePlatforms(Invocation inv, String obtainCmsId) {
		// 获取本注册服务器的共享给obtainCmsId的所有平台
		DetachedCriteria criteria = DetachedCriteria.forClass(Share.class);
		criteria.add(Restrictions.eq("platformCmsId", obtainCmsId));
		criteria.setProjection(Projections.distinct(Projections
				.property("resourceCmsId")));
		List<String> cmsIds = Ar.find(criteria);
		if (cmsIds.size() > 0) {
			DetachedCriteria crit = DetachedCriteria.forClass(Platform.class);
			crit.add(Restrictions.in("id", cmsIds));
			List<Platform> platforms = Ar.find(crit);
			return platforms;

		}
		return new ArrayList<Platform>();
	}

	public List<Platform> getEventServerMsg(List<Platform> platforms,
			String naming, String type) {
		String cmsId = naming.substring(naming.lastIndexOf(":") + 1);
		List<String> ids = Ar.of(Device.class).find(
				"select id from Device where cmsId=? and type=? and naming=?",
				cmsId, type, naming);
		// 上级平台
		if (ids.size() > 0) {
			List<String> cmsIds = getAllParents(cmsId);
			cmsIds.remove(cmsId);

			DetachedCriteria criteria = DetachedCriteria.forClass(Share.class);
			criteria.add(Restrictions.eq("resourceCmsId", cmsId));
			criteria.add(Restrictions.eq("resourceType",
					ResourceType.valueOf(type)));
			criteria.add(Restrictions.in("resourceId", ids));
			criteria.setProjection(Projections.distinct(Projections
					.property("platformCmsId")));
			List<String> tempCmsIds = Ar.find(criteria);
			cmsIds.addAll(tempCmsIds);
			if (cmsIds.size() > 0) {
				DetachedCriteria crit = DetachedCriteria
						.forClass(Platform.class);
				crit.add(Restrictions.in("id", cmsIds));
				List<Platform> list = Ar.find(crit);
				platforms.addAll(list);
			}
		}
		return platforms;
	}

	public void deviceRegistResult(String cmsId) {
		// 设备注册返回结果处理
		Ar.exesql(
				"update device set status=? where cms_id=? and (status=? or status=?)",
				Status.normal.name(), cmsId, Status.add.name(),
				Status.update.name());

		Ar.exesql("delete from device where cms_id=? and status=?", cmsId,
				Status.delete.name());
	}

	public void delShareAuthorizationByDevices(List<Device> devices) {
		for (Device device : devices) {
			delShareAuthorizationByDevice(device);
		}
	}

	public void delShareAuthorizationByDevice(Device device) {
		Ar.exesql("delete from authorization where resource_cms_id=? "
				+ "and resource_id=? and resource_type=?", device.getCmsId(),
				device.getId(), ResourceType.valueOf(device.getType())
						.ordinal());
		Ar.exesql("delete from share where resource_cms_id=? "
				+ "and resource_id=? and (resource_type=? or resource_type=?)",
				device.getCmsId(), device.getId(),
				ResourceType.valueOf(device.getType()).ordinal(),
				ResourceType.DEVICE.ordinal());
	}

	public List<Device> getDelDevicesByCmsId(String cmsId) {
		List<Device> devices = Ar.of(Device.class).find(
				"from Device where cms_id=? and status=?", cmsId,
				Status.delete.name());
		return devices;
	}


	public String getOuterPlatformByCmsId(String platformCmsId, String cmsId) {
		List<String> ids = Ar
				.of(Share.class)
				.find("select id from Share where platformCmsId=? and resourceCmsId=?",
						platformCmsId, cmsId);
		if (ids.size() > 0) {
			return "has";
		} else {
			return "hasnot";
		}
	}


	/**
	 * permission位数分别代表的是实时,历史,云台,预置位,抓拍, 只有当云台为0是预置位也相应的为0所有,其它情况全部都为1
	 * 
	 * @param permission
	 * @return
	 */
	public String getDevicePermission(String permission) {
		if (permission == null) {
			return "11111";
		}
		if (permission.length() == 5) {
			return permission;
		}
		if (permission.equals("111")) {
			return "11111";
		} else {
			return "11001";
		}
	}

	public Platform createPlatform(IPlatform platform, Platform parent) {
		Platform dbPlatform = setPlatform(platform, parent);
		Ar.save(dbPlatform);
		return dbPlatform;
	}

	public Platform setPlatform(IPlatform platform, Platform parent) {
		Platform dbPlatform = new Platform();
		dbPlatform.setCmsId(platform.getCmsId());
		dbPlatform.setName(platform.getName());
		dbPlatform.setPassword(platform.getPassword());
		dbPlatform.setServiceUrl(platform.getServiceUrl());
		dbPlatform.setParent(parent);
		dbPlatform.setStatus(Status.add);
		dbPlatform.setEventServerIp(platform.getEventServerIp());
		dbPlatform.setEventServerPort(platform.getEventServerPort());
		dbPlatform.setParentCmsId(parent != null ? parent.getCmsId() : null);
		dbPlatform.setSync(false);
		return dbPlatform;
	}


	public void deleteDeviceByServer(String cmsId, String serverId) {
		List<Device> devices = Ar.of(Device.class).find(
				"from Device where cmsId=? and naming like ?", cmsId,
				"%:" + serverId + ":");
		delShareAuthorizationByDevices(devices);
		Ar.exesql("delete from device where cmsId=? and naming like ?", cmsId,
				"%:" + serverId + ":");
	}

	public void respStaticFile(Invocation inv, File file) {
		PrintWriter out = null;
		InputStreamReader in = null;
		try {
			out = inv.getResponse().getWriter();
			in = new InputStreamReader(new FileInputStream(file));
			char[] buf = new char[1024];
			int off = 0;
			int len = 0;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, off, len);
			}
			out.flush();
			out.close();
			in.close();
		} catch (Exception e) {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void visRegistResult(String cmsId) {
		// 设备注册返回结果处理
		Ar.exesql(
				"update device_server set status=? where cms_id=? and (status=? or status=?)",
				Status.normal.name(), cmsId, Status.add.name(),
				Status.update.name());

		Ar.exesql("delete from device_server where cms_id=? and status=?",
				cmsId, Status.delete.name());
	}

	public String getDeviceInfoByUrl(String url, String cmsId, Device device) {
		url = url.replace("http://", "").replaceFirst("/platform_grade", "")
				.replaceFirst("/share", "");
		Map<String, String> map = new HashMap<String, String>();
		map.put("cmsId", cmsId);
		map.put("resourceType", DeviceType.valueOf(device.getType()).getType());
		map.put("resourceId", device.getDeviceId());
		map.put("naming", device.getNaming());
		map.put("sessionId", "outer_user_session");
		HttpGet platformGet = HttpclientUtils.accessPlatformGrade(map,
				"/resource_detail_info.xml", url + "/web_xml_interface");
		try {
			HttpEntity entity = HttpclientUtils.getResponse(map, platformGet)
					.getEntity();
			if (entity != null) {
				String deviceInfo = EntityUtils.toString(entity);
				if (parserDeviceInfo(deviceInfo)) {
					return deviceInfo;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return "";
	}

	private boolean parserDeviceInfo(String deviceInfo) {
		if (deviceInfo.indexOf("<failed code=\"700\"") != -1) {
			return false;
		}
		return true;
	}

	public String getDeviceInfoByNaming(Invocation inv, String naming) {
		Device device = Ar.of(Device.class).one("naming", naming);
		inv.addModel("device", device);
		if (device.getServerId() != null) {
			DeviceServer server = Ar.of(DeviceServer.class).get(
					device.getServerId());
			inv.addModel("server", server);
		}
		return PlatformUtils.getGBPath("device-detail");
	}

	public String getStdId(String cmsId, String type, String entity) {
		StringBuilder id = new StringBuilder();
		// id.append(cmsId).append("0001");// 平台编号+行业编号
		id.append(DeviceIdType.getCmsId(cmsId.substring(0,6))).append("0000");// 测试中说明0000是正常的
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

	public String getCmsIdByGBConfig(String deviceId) {
		StringBuilder str = new StringBuilder("");
		if (ConfigUtil.GB_CMSID_EXPRESSION != null) {
			String[] indexes = ConfigUtil.GB_CMSID_EXPRESSION.split(",");
			for (String index : indexes) {
				String[] temp = index.split("-");
				if (temp.length == 2) {
					str.append(deviceId.substring(
							Integer.parseInt(temp[0]) - 1,
							Integer.parseInt(temp[1])));
				}
				if (temp.length == 1) {
					str.append(deviceId.substring(
							Integer.parseInt(temp[0]) - 1,
							Integer.parseInt(temp[0])));
				}
			}
			return str.toString();
		}

		str.append(deviceId.substring(0, 8)).append(deviceId.substring(13, 14));
		return str.toString();
	}

	public List<Organ> getAllChildrenOrgan(String parentPath, String cmsId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Organ.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		criteria.add(Restrictions.like("path", parentPath, MatchMode.START));
		criteria.add(Restrictions.ne("status", Status.delete.name()));
		List<Organ> organs = Ar.find(criteria);
		return organs;
	}

	public List<Device> getDeviceNumByOrgans(List<Organ> organs, String cmsId) {
		List<String> ownerIds = new ArrayList<String>();
		for (Organ organ : organs) {
			ownerIds.add(organ.getOrganId());
		}
		DetachedCriteria criteria = DetachedCriteria.forClass(Device.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		criteria.add(Restrictions.in("ownerId", ownerIds));
		criteria.add(Restrictions.ne("status", Status.delete.name()));
		return Ar.find(criteria);
	}

	public List<String> getOrganResourceIds(String deviceResourceId,
			String cmsId, ResourceType resourceType) {
		ResourceStatus resource = Ar.of(resourceType.getResourceClass()).get(
				deviceResourceId);
		String[] organIds = resource.getPath().split("/");
		List<String> organIdList = new ArrayList<String>();
		for (String organId : organIds) {
			if (organId.length() > 0) {
				organIdList.add(organId + "_" + cmsId);
			}
		}
		return organIdList;
	}

	public Share getOrganShare(String platformId, String resourceId,
			String resourceCmsId, ResourceType resourceType) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Share.class);
		criteria.add(Restrictions.eq("platformCmsId", platformId));
		criteria.add(Restrictions.in("resourceId",
				getOrganResourceIds(resourceId, resourceCmsId, resourceType)));
		criteria.add(Restrictions.eq("resourceType", ResourceType.ORGAN));
		criteria.add(Restrictions.eq("resourceCmsId", resourceCmsId));
		Share share = Ar.one(criteria);
		return share;
	}

	public String getPath(String deviceResourceId, ResourceType resourceType) {
		ResourceStatus resource = Ar.of(resourceType.getResourceClass()).get(
				deviceResourceId);
		return resource.getPath();
	}

	public DetachedCriteria getServerByIds(List<String> serverIds) {
		DetachedCriteria crit = DetachedCriteria.forClass(DeviceServer.class);
		crit.add(Restrictions.isNotNull("stdId"));
		crit.add(Restrictions.ne("status", Status.delete.name()));
		crit.add(Restrictions.in("id", serverIds));
		return crit;
	}

	public boolean organIdsContain(String[] organIds, String platformId,
			String resourceId, String resourceCmsId, ResourceType resourceType) {
		String resourcePath = getPath(resourceId, resourceType);
		for (String organId : organIds) {
			if (organId.length() > 0) {
				String organPath = getPath(organId, ResourceType.ORGAN);
				if (resourcePath.indexOf(organPath) == 0) {
					if (resourceType == ResourceType.DEVICE) {
						return true;
					} else if (resourceType == ResourceType.ORGAN
							&& !organPath.equals(resourcePath)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public List<Organ> getOrgansByName(String cmsId, String name) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Organ.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		criteria.add(Restrictions.ne("status", Status.delete.name()));
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		} else {
			criteria.add(Restrictions.isNull("parent"));
		}
		List<Organ> organs = Ar.find(criteria, 0, 1000);
		return organs;
	}

	public void deleteOrgans(Collection<Organ> organs) {
		Set<Integer> ids = new HashSet<Integer>();
		for (Organ organ : organs) {
			if (!ids.contains(organ.getId())) {
				deleteOrganOnly(organ, ids);
			}
		}
	}

	private void deleteOrganOnly(Organ organ, Set<Integer> ids) {
		ids.add(organ.getId());
		List<Organ> children = organ.getChildren();
		for (Organ child : children) {
			deleteOrganOnly(child, ids);
		}
		Ar.delete(organ);
		Ar.exesql("delete from authorization where resource_cms_id=? "
				+ "and resource_id=? and resource_type=?", organ.getCmsId(),
				organ.getId(), ResourceType.valueOf(organ.getType()).ordinal());
		Ar.exesql("delete from share where resource_cms_id=? "
				+ "and resource_id=? and resource_type=?", organ.getCmsId(),
				organ.getId(), ResourceType.valueOf(organ.getType()).ordinal());
	}

	public void deleteAuthorizationShareByOrgan(Organ organ) {
		Ar.exesql("delete from authorization where resource_cms_id=? "
				+ "and resource_id=? and resource_type=?", organ.getCmsId(),
				organ.getId(), ResourceType.valueOf(organ.getType()).ordinal());
		Ar.exesql("delete from share where resource_cms_id=? "
				+ "and resource_id=? and resource_type=?", organ.getCmsId(),
				organ.getId(), ResourceType.valueOf(organ.getType()).ordinal());
	}

	/**
	 * 重新设置organ的path
	 * 
	 * @return
	 */
	public String resetOrgan() {
		List<Organ> organs = Ar
				.of(Organ.class)
				.find("from Organ where parent.id is null and parentOrganId is not null");
		StringBuilder path = new StringBuilder();
		for (Organ organ : organs) {
			organ.setPath(getPath(organ, path));
			Ar.update(organ);
			path.setLength(0);
		}
		Ar.flush();
		return "@success";
	}

	public String getPath(Organ organ, StringBuilder path) {
		path.insert(0, organ.getOrganId());
		path.insert(0, "/");
		if (organ.getParentOrganId() != null) {
			if (organ.getParent() == null) {
				Organ parent = Ar.of(Organ.class).get(organ.getParentOrganId());
				organ.setParent(parent);
			}
			if (organ.getParent() != null) {
				getPath(organ.getParent(), path);
			}
		}
		return path.toString();
	}

	public void test(List<Organ> organs) {
		for (Organ organ : organs) {
			List<Organ> children = organ.getChildren();
			for (Organ child : children) {
				System.out.println(child.getName());
			}
		}
	}

	public void setDeviceByServer(Device device, String serverId) {
		DeviceServer server = Ar.of(DeviceServer.class).get(serverId);
		if (server != null) {
			server.setChildrenStatus(true);
			Ar.update(server);
			Organ organ = Ar.of(Organ.class).get(server.getOrganId());
			if (organ != null) {
				device.setOrgan(organ);
				device.setOwnerId(organ.getOrganId());
				device.setPath(organ.getPath());
			}
		}
	}

	public boolean compare(Object source, Object target) {
		if (source == null && target == null) {
			return true;
		}
		if (source == null && target != null) {
			return false;
		}
		if (source.equals(target)) {
			return true;
		}
		return false;
	}

	public DetachedCriteria getServerStatusByIds(List<String> serverIds,
			int gbPlatformId) {
		DetachedCriteria criteria = DetachedCriteria
				.forClass(ServerStatus.class);
		criteria.add(Restrictions.eq("platform.id", gbPlatformId));
		criteria.add(Restrictions.ne("status", Status.normal));
		criteria.add(Restrictions.in("serverId", serverIds));
		return criteria;
	}

	public void saveTaskStatus(RequestEntity entity, int objectId,
			String objectType) {
		TaskStatus ts = new TaskStatus();
		ts.setChangeTime(entity.getChangeTime());
		ts.setNanosecond(entity.getNanosecond());
		ts.setObjectId(objectId);
		ts.setObjectType(objectType);
		ts.setPlatformId(entity.getGbPlatform().getId());
		ts.setSn(entity.getSN());
		Ar.save(ts);
	}

	int size = 5000;

	public void initDevice(final String cmsId, final GbPlatform platform) {
		String sql ="insert into device_status (base_notify,change_time,nanosecond,online,online_change_time,online_nanosecond,online_notify,status,device_id,platform_id,name,path,location) select false,change_time,nanosecond,online,change_time,nanosecond,false,3,id,?,name,path,location from device where cms_id=?";
		Ar.exesql(sql, platform.getId(),cmsId);
	}

	public void flushAndClear() {
		Ar.flush();
		Ar.clear();
	}

	public void scanDeviceStatusByGbPlatformForPush(GbPlatform platform,
			Timestamp changeTime, int nanosecond) {
		DeviceStatus deviceStatus = Ar
				.of(DeviceStatus.class)
				.one("from DeviceStatus where platform.id=? and ( (baseNotify is false and (changeTime<? or (changeTime=? and nanosecond<?)))  or (onlineNotify is false and (onlineChangeTime<? or (onlineChangeTime=? and onlineNanosecond<?))))",
						platform.getId(), changeTime, changeTime, nanosecond,
						changeTime, changeTime, nanosecond);
		ServerStatus serverStatus = Ar
				.of(ServerStatus.class)
				.one("from ServerStatus where platform.id=? and ( (baseNotify is false and (changeTime<? or (changeTime=? and nanosecond<?)))  or (onlineNotify is false and (onlineChangeTime<? or (onlineChangeTime=? and onlineNanosecond<?))))",
						platform.getId(), changeTime, changeTime, nanosecond,
						changeTime, changeTime, nanosecond);
		OrganStatus organStatus = Ar
				.of(OrganStatus.class)
				.one("from OrganStatus where platform.id=? and baseNotify is false and (changeTime<? or (changeTime=? and nanosecond<?))",
						platform.getId(), changeTime, changeTime, nanosecond);

		if (deviceStatus != null) {
			Ar.exesql(
					"update device_status set online_notify=true where platform_id=? and status!=2 and online_notify=false and online_change_time<? or (online_change_time=? and online_nanosecond<=?)",
					platform.getId(), changeTime, changeTime, nanosecond);
			Ar.exesql(
					"update device_status set base_notify=true where platform_id=? and status!=2 and base_notify=false and change_time<? or (change_time=? and nanosecond<=?)",
					platform.getId(), changeTime, changeTime, nanosecond);
			Ar.exesql(
					"delete from device_status where platform_id=? and status=2  and base_notify=false and change_time<? or (change_time=? and nanosecond<=?)",
					platform.getId(), changeTime, changeTime, nanosecond);
		}

		if (serverStatus != null) {
			Ar.exesql(
					"update server_status set online_notify=true where platform_id=? and status!=2 and online_notify=false and online_change_time<? or (online_change_time=? and online_nanosecond<=?)",
					platform.getId(), changeTime, changeTime, nanosecond);
			Ar.exesql(
					"update server_status set base_notify=true where platform_id=? and status!=2 and base_notify=false and change_time<? or (change_time=? and nanosecond<=?)",
					platform.getId(), changeTime, changeTime, nanosecond);
			Ar.exesql(
					"delete from server_status where platform_id=? and status=2 and base_notify=false and change_time<? or (change_time=? and nanosecond<=?)",
					platform.getId(), changeTime, changeTime, nanosecond);
		}

		if (organStatus != null) {
			Ar.exesql(
					"update organ_status set base_notify=true where platform_id=? and status!=2 and base_notify=false and change_time<? or (change_time=? and nanosecond<=?)",
					platform.getId(), changeTime, changeTime, nanosecond);
			Ar.exesql(
					"delete from organ_status where  platform_id=? and status=2 and base_notify=false and change_time<? or (change_time=? and nanosecond<=?)",
					platform.getId(), changeTime, changeTime, nanosecond);
		}
	}


	/**
	 * 摄像头设备，注意naming的生成，主要是根据国标设备ID来获得cmsId，然后获得对应的Sip服务器地址
	 * 
	 * @param item
	 * @param platform
	 * @param parentMap
	 * @return
	 */
	public Device getDevice(Item item, GbPlatform gbPlatform) {
		Device device = new Device();
		String deviceId = item.getDeviceID();
		device.setDeviceId(DeviceIdType.getMegaId(deviceId));
		device.setCmsId(gbPlatform.getCmsId());
		device.setLocation(item.getAddress());
		device.setName(item.getName());
		device.setStdId(deviceId);// 设置标准ID
		if (StringUtils.isBlank(item.getStatus())) {
			device.setOnline("OFF");
		} else {
			device.setOnline(item.getStatus());
		}
		
		String[] parentIds = item.getParentID().split("/");
		for(String parentId : parentIds){
			if(DeviceIdType.isVirtrueOrgan(parentId)){
				device.setOwnerId(DeviceIdType.getMegaId(parentId));
			}
		}
		
		if(StringUtils.isBlank(device.getOwnerId())){
			for(String parentId : parentIds){
				if(DeviceIdType.isBusinessOrgan(parentId)){
					device.setOwnerId(DeviceIdType.getMegaId(parentId));
				}
			}		
		}
		
		
		if(StringUtils.isBlank(device.getOwnerId())){
			String parentId = item.getCivilCode();
			if (DeviceIdType.isOrgan(parentId) || DeviceIdType.isCmsId(parentId)) {
				String ownerId = null;
				if (DeviceIdType.isCmsId(parentId)) {
					ownerId = DeviceIdType.getMegaId(gbPlatform.getCmsId());
					createVirtualOrgan(gbPlatform);
				} else {
					ownerId = DeviceIdType.getMegaId(DeviceIdType.fullOrganId(parentId));
	
				}
				device.setOwnerId(ownerId);
			}
		}
		
		for(String parentId : parentIds){
			if(DeviceIdType.isServer(parentId)){
				device.setNaming(device.getDeviceId() + ":"
						+ DeviceIdType.getMegaId(parentId) + ":"
						+ getSipServerIP(gbPlatform.getSipServer()) + ":"
						+ registUtil.getNamingCmsId(gbPlatform.getCmsId()));
				device.setServerId(DeviceIdType.getMegaId(parentId));
			}
		}	

		device.setPermission("11111");
		device.setStatus(Status.normal.name());
		device.setSupportScheme(false);
		device.setType(DeviceIdType.getType(deviceId).name());
		device.setLatitude(item.getLatitude());
		device.setLongitude(item.getLongitude());
		

		device.setStdId(deviceId);

		device.setSync(false);
		device.setStatus(Status.add.name());
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		device.setChangeTime(timestamp);
		device.setNanosecond(timestamp.getNanos() / 1000);
		return device;
	}

	private Organ createVirtualOrgan(GbPlatform platform) {
		Organ organ = Ar.of(Organ.class).one("organId",
				DeviceIdType.getMegaId(platform.getCmsId()));
		if (organ == null) {
			organ = new Organ();
			organ.setOrganId(DeviceIdType.getMegaId(platform.getCmsId()));
			organ.setCmsId(platform.getCmsId());
			organ.setName(platform.getName() + "_organ");
			organ.setPath("/" + organ.getOrganId());
			organ.setStatus(Status.add.name());
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			organ.setChangeTime(timestamp);
			organ.setSync(false);
			organ.setNanosecond(timestamp.getNanos() / 1000);
			organ.setStdId(registUtil.getManufacturerUtils(
					platform.getManufacturer()).getOrganStdId(organ));
			Ar.save(organ);
			DetachedCriteria criteria = DetachedCriteria
					.forClass(GbPlatform.class);
			criteria.add(Restrictions.eq("type", GbPlatformType.PARENT));
			registUtil.getRS(this).parentRegistOrgan(
					platform.getCmsId());
		}
		return organ;
	}

	public DeviceServer createVirtualDeviceServer(GbPlatform gbPlatform) {
		DeviceServer ds = new DeviceServer();
		ds.setCmsId(gbPlatform.getCmsId());
		ds.setServerId(DeviceIdType.Mega.sample());
		ds.setStdId(getStdId(gbPlatform.getCmsId(), "VIS", "device_server"));
		ds.setIP("127.0.0.1");
		ds.setLocation("Virtual Server tianlei");
		ds.setManufacturer("virtual");
		ds.setModel("virtual");
		ds.setName("virtual");
		ds.setType("VIS");
		ds.setStatus(Status.normal.name());
		ds.setChildrenStatus(true);
		ds.setOnline("ON");
		ds.setInnerDevice(false);

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		ds.setChangeTime(timestamp);
		ds.setNanosecond(timestamp.getNanos() / 1000);
		ds.setSync(false);
		ds.setStatus(Status.add.name());
		ds.setNaming(DeviceIdType.Mega.sample()+":"+getSipServerIP(gbPlatform.getSipServer())+":"+registUtil.getNamingCmsId(gbPlatform.getCmsId()));
		Ar.save(ds);
		return ds;
	}

	public static String getSipServerIP(String sipServer) {
		sipServer = sipServer.replace("http://", "");
		sipServer = sipServer.split("/")[0].split(":")[0];
		return sipServer;
	}

	public DeviceServer getDeviceServer(Item item, GbPlatform platform) {
		DeviceServer server = new DeviceServer();
		server.setCmsId(platform.getCmsId());
		server.setManufacturer(item.getManufacturer());
		server.setStdId(item.getDeviceID());
		server.setModel(item.getModel());
		server.setStatus(Status.normal.name());
		server.setServerId(DeviceIdType.getMegaId(item.getDeviceID()));
		server.setIP(item.getIPAddress());
		server.setLocation(item.getAddress());
		server.setName(item.getName());
		server.setType(DeviceIdType.getType(item.getDeviceID()).name());
		server.setStreamSupport("false");

		server.setNaming(DeviceIdType.getMegaId(item.getDeviceID()) + ":"
				+ getSipServerIP(platform.getSipServer()) + ":"
				+ DeviceIdType.getCmsId(platform.getCmsId()));

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		server.setChangeTime(timestamp);
		server.setNanosecond(timestamp.getNanos() / 1000);
		server.setSync(false);
		server.setStatus(Status.add.name());
		if (StringUtils.isBlank(item.getStatus())) {
			server.setOnline("OFF");
		} else {
			server.setOnline(item.getStatus());
		}

		return server;
	}

	public void createOrganServer(String organId, String serverId, String cmsId) {
		OrganServer organServer = Ar.of(OrganServer.class).one(
				"organId,serverId", organId, serverId);
		if (organServer == null) {
			organServer = new OrganServer();
			organServer.setCmsId(cmsId);
			organServer.setOrganId(organId);
			organServer.setServerId(serverId);
			Organ organ = Ar.of(Organ.class).get(organId);
			organServer.setPath(organ.getPath());
			Ar.save(organServer);
		}
	}
    public static int i;
	// 生成机构基本信息
	public Organ getOrgan(Item item, GbPlatform gbPlatform) {
		Organ organ = new Organ();
		organ.setOrganId(DeviceIdType.getMegaId(DeviceIdType.fullOrganId(item.getDeviceID())));
		organ.setCmsId(gbPlatform.getCmsId());
		organ.setName(item.getName());
		organ.setStdId(DeviceIdType.fullOrganId(item.getDeviceID()));
		
		if(DeviceIdType.isVirtrueOrgan(organ.getStdId())){
			String[] parentIds = item.getParentID().split("/");
			for(String parentId : parentIds){
				if(DeviceIdType.isVirtrueOrgan(parentId)){
					organ.setParentOrganId(DeviceIdType.getMegaId(parentId));
				}
			}
			if(StringUtils.isBlank(organ.getParentOrganId())){
				if(StringUtils.isNotBlank(item.getBusinessGroupID())){
					organ.setParentOrganId(DeviceIdType.getMegaId(item.getBusinessGroupID()));
				}
			}
		}else if(DeviceIdType.isBusinessOrgan(organ.getStdId())){
			organ.setParentOrganId(DeviceIdType.getMegaId(DeviceIdType.getBusinessOrganParentId(item.getDeviceID())));
		}else{
			if (item.getDeviceID().length()==2) {
				organ.setPath("/" + organ.getOrganId());
			} else {
				organ.setParentOrganId(DeviceIdType.getParentOrganId(item.getDeviceID(), organ.getCmsId()));
			}
		}
		if(organ.getOrganId().equals(organ.getParentOrganId())){
			organ.setParentOrganId(null);
			organ.setParent(null);
			organ.setPath("/" + organ.getOrganId());
		}
		organ.setStatus(Status.add.name());
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		organ.setChangeTime(timestamp);
		organ.setSync(false);
		organ.setNanosecond(timestamp.getNanos() / 1000);
		return organ;
	}
	

	public void deviceRegist(String cmsId) {
		try {
			List<Device> deviceList = getRegistDevices(cmsId);
			if (deviceList.size() > 0) {

				ResourceStatus endResourceStatus = deviceList.get(deviceList
						.size() - 1);
				ResourceStatus beginResourceStatus = deviceList.get(0);
				RegistCallable task = new RegistCallable(PlatformUtils.getWsdlUrl());
				task.setClazz(new Class[] { String.class, List.class });
				task.setMethodStr("deviceRegist");
				task.setObjects(new Object[] { cmsId, deviceList });
				taskCompletionService.submit(task);
				try {
					Future<String> result = taskCompletionService.take();
					RegistParam param = new RegistParam();
					param.setBeginResourceStatus(beginResourceStatus);
					param.setEndResourceStatus(endResourceStatus);
					param.setCmsId(cmsId);
					
					if("success".equals(result.get())){
						showRegistResult("device regist success!");
						// 设备注册返回结果处理
						changeDeviceStatus(param);
						deviceRegist(param.getCmsId());
					}else{
						showRegistResult("device regist failure!");
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void showRegistResult(String result){
		System.out.println("-----------------------------------------------------");
		System.out.println(result);
		System.out.println("-----------------------------------------------------");
	}

	public void platformRegist() {
		try {
			List<IPlatform> list = Ar.of(Platform.class).find(
					"from Platform where status!=?  and owner is false",
					Status.normal);
			IPlatform owner = Ar.of(Platform.class).one(
					"from Platform where owner is true");
			if (list.size() == 0 && owner.isSync()) {
				return;
			}

			RegistCallable task = new RegistCallable(PlatformUtils.getWsdlUrl());
			task.setClazz(new Class[] { IPlatform.class, List.class });
			task.setMethodStr("parentPlatformRegist");
			task.setObjects(new Object[] { owner, list });
			taskCompletionService.submit(task);
			try {
				Future<String> result = taskCompletionService.take();
				setPlatformRegistResult(result.get(), list,
						owner);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void organRegist(String cmsId) {
		try {
			List<Organ> organList = getRegistOrgans(cmsId);
			if (organList.size() > 0) {
				ResourceStatus endResourceStatus = organList.get(organList
						.size() - 1);
				ResourceStatus beginResourceStatus = organList.get(0);
				RegistCallable task = new RegistCallable(PlatformUtils.getWsdlUrl());
				task.setClazz(new Class[] { String.class, List.class });
				task.setMethodStr("organRegist");
				task.setObjects(new Object[] { cmsId, organList });
				taskCompletionService.submit(task);
				try {
					Future<String> result = taskCompletionService.take();
					RegistParam param = new RegistParam();
					param.setBeginResourceStatus(beginResourceStatus);
					param.setEndResourceStatus(endResourceStatus);
					param.setCmsId(cmsId);
					if("success".equals(result.get())){
						showRegistResult("organ regist success!");
						changeOrganStatus(param);
						organRegist(param.getCmsId());
					}else{
						showRegistResult("organ regist failure!");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void visRegist(String cmsId) {
		try {
			List<DeviceServer> serverList = getRegistDeviceServers(cmsId);
			if (serverList.size() > 0) {

				ResourceStatus endResourceStatus = serverList.get(serverList
						.size() - 1);
				ResourceStatus beginResourceStatus = serverList.get(0);
				RegistCallable task = new RegistCallable(PlatformUtils.getWsdlUrl());
				task.setClazz(new Class[] { String.class, List.class });
				task.setMethodStr("visRegist");
				task.setObjects(new Object[] { cmsId, serverList });
				taskCompletionService.submit(task);
				try {
					Future<String> result = taskCompletionService.take();
					RegistParam param = new RegistParam();
					param.setBeginResourceStatus(beginResourceStatus);
					param.setEndResourceStatus(endResourceStatus);
					param.setCmsId(cmsId);
					if("success".equals(result.get())){
						showRegistResult("vis regist success!");
						changeStatus(param, "device_server");
						visRegist(param.getCmsId());
					}else{
						showRegistResult("vis regist failure!");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setPlatformRegistResult(String result,
			List<IPlatform> platforms, IPlatform ownerBean) {
		if (result.equals("success")) {
			System.out
					.println("-----------------------------------------------------");
			System.out
					.println("result=" + result + " , method=platform regist");
			System.out
					.println("-----------------------------------------------------");
			// 平台注册返回结果
			platforms.add(ownerBean);
			for (IPlatform platform : platforms) {
				Platform dbPlatform = Ar.of(Platform.class).get(
						platform.getId());
				dbPlatform.setStatus(Status.normal);
				dbPlatform.setSync(true);
				Ar.update(dbPlatform);
			}
		}
	}

	public Map<String, OuterDeviceAlarm> getSchemesMap(String deviceNamings,
			String types, String schemeIds, String ids) {
		Map<String, OuterDeviceAlarm> map = new HashMap<String, OuterDeviceAlarm>();
		String[] deviceNamingsArr = deviceNamings.split("__");
		String[] typesArr = types.split("__");
		String[] schemeIdsArr = schemeIds.split("__");
		String[] idsArr = ids.split("__");
		for (int i = 0; i < schemeIdsArr.length; i++) {
			if (schemeIdsArr[i].length() > 0) {
				OuterDeviceAlarm deviceAlarm = new OuterDeviceAlarm();
				deviceAlarm.setDeviceNaming(deviceNamingsArr[i]);
				deviceAlarm.setDeviceType(typesArr[i]);
				deviceAlarm.setSchemeId(schemeIdsArr[i]);
				deviceAlarm.setId(idsArr[i]);
				map.put(deviceAlarm.getDeviceNaming() + "_"
						+ deviceAlarm.getDeviceType(), deviceAlarm);
			}
		}
		return map;
	}

	public void deleteAuthorizations(List<Authorization> authorizations) {
		for (Authorization au : authorizations) {
			Ar.delete(au);
		}
	}

	public void clearInvaild() {
		jdbc.execute("delete  from authorization  where authorization.resource_type='organ' and not exists(select * from platform_organ o where authorization.resource_id=o.id)");
		jdbc.execute("delete  from authorization  where authorization.resource_type='device' and not exists(select * from device d where authorization.resource_id = d.id)");
		jdbc.execute("delete  from share  where share.resource_type='organ' and  not exists(select * from organ d where share.resource_cms_id=d.cms_id and share.resource_id = d.id)");
		jdbc.execute("delete  from share  where share.resource_type='device' and not exists(select * from device d where share.resource_cms_id=d.cms_id and share.resource_id = d.id)");
	}

	public List<String> getResourceCmsIdsByAuthorization(User user) {
		String sql = " select distinct(au.resource_cms_id) from authorization au inner join (select role_id as id,1 as type from r_user_role rur where user_id=? union select ? as id,0 as type) as granted on(au.granted_id=granted.id and au.granted_type=granted.type)";
		List<String> cmsIds = jdbc.queryForList(sql,
				new Object[] { user.getId(), user.getId() }, String.class);
		return cmsIds;
	}

	public void deleteOrganServerByServerId(Integer id) {
		Ar.exesql("delete from organ_server where server_id=?", id);
	}
	
	public List<Device> getDevicesByNaming(String naming){
		List<Device> devices = Ar.of(Device.class).find("naming",naming);
		return devices;
	}
	
	public String resetPlatformOrganPath(){
		Map<Integer, String> organPathMap = new HashMap<Integer, String>();
		List<PlatformOrgan> organs = Ar.of(PlatformOrgan.class).find();
		StringBuilder path = new StringBuilder();
		for (PlatformOrgan organ : organs) {
			organPathMap.put(organ.getId(), getPlatformOrganPath(organ, path));
			path.setLength(0);
		}
		List<Object[]> params = new ArrayList<Object[]>();
		int i = 1;
		for (Integer id : organPathMap.keySet()) {
			params.add(new Object[] { organPathMap.get(id), id, });
			if (i++ % 200 == 0) {
				jdbc.batchUpdate("update platform_organ set path=? where id=? ",
						params);
				params.clear();
			}
		}
		jdbc.batchUpdate("update platform_organ set path=? where id=? ", params);
		return "@success";
	}
	
	public String resetGbOrganPath() {
		Map<Integer, String> organPathMap = new HashMap<Integer, String>();
		List<GbOrgan> organs = Ar.of(GbOrgan.class).find();
		StringBuilder path = new StringBuilder();
		for (GbOrgan organ : organs) {
			organPathMap.put(organ.getId(), getPath(organ, path));
			path.setLength(0);
		}
		List<Object[]> params = new ArrayList<Object[]>();
		int i = 1;
		for (Integer id : organPathMap.keySet()) {
			params.add(new Object[] { organPathMap.get(id), id, });
			if (i++ % 200 == 0) {
				jdbc.batchUpdate("update gb_organ set path=? where id=? ",
						params);
				params.clear();
			}
		}
		jdbc.batchUpdate("update gb_organ set path=? where id=? ", params);
		return "@success";
	}

	private String getPath(GbOrgan gbOrgan, StringBuilder path) {
		path.insert(0, gbOrgan.getId());
		path.insert(0, "/");
		if (gbOrgan.getParent() != null) {
			getPath(gbOrgan.getParent(), path);
		}
		return path.toString();
	}
	
	private String getPlatformOrganPath(PlatformOrgan organ, StringBuilder path) {
		path.insert(0, organ.getId());
		path.insert(0, "/");
		if (organ.getParent() != null) {
			getPlatformOrganPath(organ.getParent(), path);
		}
		return path.toString();
	}
	
	public Object[] getInsertDeviceSQLParams(IDevice dbDevice){
		return new Object[] { 
				dbDevice.isAllocated(), 
				dbDevice.getCmsId(),
				dbDevice.getDeviceId(), 
				dbDevice.getLocation(),
				dbDevice.getName(), 
				dbDevice.getNaming(),
				dbDevice.getOwnerId(), 
				dbDevice.getPath(),
				dbDevice.getPermission(), 
				dbDevice.getStatus(),
				dbDevice.isSupportScheme(),
				dbDevice.getType(),
				dbDevice.getLongitude(), 
				dbDevice.getLatitude(),
				dbDevice.getServerId(),
				dbDevice.getStdId(),
				dbDevice.isSync(), 
				dbDevice.getChangeTime(),
				dbDevice.getNanosecond(),
				dbDevice.getPositionType().ordinal(),
				dbDevice.getPtzType().ordinal(),
				dbDevice.getSupplyLightType().ordinal(),
				dbDevice.getUseType().ordinal(),
				dbDevice.getDircetionType().ordinal(),
				dbDevice.getRoomType().ordinal(),
				dbDevice.getOnline(),
				dbDevice.getRecordCount(), 
				dbDevice.getGpsZ()
				,
				dbDevice.getOwnerId(),
				dbDevice.isAllocated(),
				dbDevice.getLocation(),
				dbDevice.getName(),
				dbDevice.getNaming(),
				dbDevice.getStatus(),
				dbDevice.getLongitude(), 
				dbDevice.getLatitude(),
				dbDevice.getServerId(),
				dbDevice.isSync(), 
				dbDevice.getChangeTime(),
				dbDevice.getNanosecond(),
				dbDevice.getPositionType().ordinal(),
				dbDevice.getPtzType().ordinal(),
				dbDevice.getSupplyLightType().ordinal(),
				dbDevice.getUseType().ordinal(),
				dbDevice.getDircetionType().ordinal(),
				dbDevice.getRoomType().ordinal(),
				dbDevice.getOnline(),
				dbDevice.getRecordCount(), 
				dbDevice.getGpsZ() };
	}
	
	public Object[] getInsertOrganParams(IOrgan organ){
		return new Object[] { 
				organ.getBlock(),
				organ.getChangeTime(),
				organ.getCmsId(),
				organ.getName(),
				organ.getNanosecond(),
				organ.getOrganId(),
				organ.getParentOrganId(),
				organ.getParentOrganName(),
				organ.getPath(),
				organ.getStatus(),
				organ.getStdId(),
				organ.isSync(),
				organ.getType(),
				organ.getBlock(),
				organ.getChangeTime(),
				organ.getName(),
				organ.getNanosecond(),
				organ.getParentOrganId(),
				organ.getParentOrganName(),
				organ.getStatus(),
				organ.isSync()};
	}
	
	public Object[] getInsertServerParams(IDeviceServer server,boolean innerDevice){
		return new Object[] { 
				server.getIP(),
				server.getChangeTime(),
				server.isChildrenStatus(),
				server.getCmsId(), 
				innerDevice,
				server.getLocation(),
				server.getManufacturer(),
				server.getModel(),
				server.getName(),
				server.getNaming(),
				server.getNanosecond(),
				server.getOnline(),
				server.getServerId(),
				server.getStatus(),
				server.getStdId(),
				server.getStreamSupport(),
				server.isSync(),
				server.getType() 
				,
				server.getChangeTime(),
				server.getLocation(),
				server.getManufacturer(),
				server.getModel(),
				server.getName(),
				server.getNaming(),
				server.getNanosecond(),
				server.getOnline(),
				server.getStatus(),
				server.getStdId(),
				server.isSync()};
	}
	
	public Device setDeviceProfile(Device dbDevice, IDevice device) {
		String path = device.getPath();
		dbDevice.setDeviceId(device.getDeviceId());
		dbDevice.setLocation(device.getLocation());
		dbDevice.setName(device.getName());
		dbDevice.setNaming(device.getNaming());
		dbDevice.setOuterPlatforms(device.getOuterPlatforms());
		if (StringUtils.isBlank(device.getOwnerId())) {
			dbDevice.setOwnerId(path.substring(path.lastIndexOf("/") + 1));
		} else {
			dbDevice.setOwnerId(device.getOwnerId());
		}
		dbDevice.setPermission(this.getDevicePermission(device
				.getPermission()));
		dbDevice.setStatus(device.getStatus());
		dbDevice.setSupportScheme(device.isSupportScheme());
		dbDevice.setType(device.getType());
		dbDevice.setChangeTime(device.getChangeTime());
		dbDevice.setNanosecond(device.getNanosecond());
		dbDevice.setSync(device.isSync());
		if (StringUtils.isBlank(device.getOnline())) {
			dbDevice.setOnline("OFF");
		} else {
			dbDevice.setOnline(device.getOnline());
		}
		dbDevice.setStdId(device.getStdId());
		if (StringUtils.isBlank(device.getServerId())) {
			dbDevice.setServerId(dbDevice.getNaming().split(":")[1]);
		} else {
			dbDevice.setServerId(device.getServerId());
		}

		dbDevice.setPtzType(PTZType.getInstance(device.getIptzType()));
		dbDevice.setDircetionType(DircetionType.getInstance(device
				.getIdircetionType()));
		dbDevice.setRoomType(RoomType.getInstance(device.getIroomType()));
		dbDevice.setUseType(UseType.getInstance(device.getIuseType()));
		dbDevice.setSupplyLightType(SupplyLightType.getInstance(device
				.getIsupplyLightType()));
		dbDevice.setPositionType(PositionType.getInstance(device
				.getIpositionType()));

		dbDevice.setLongitude(device.getLongitude());
		dbDevice.setLatitude(device.getLatitude());
		dbDevice.setGpsZ(device.getGpsZ());

		return dbDevice;
	}
}
