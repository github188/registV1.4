package com.megaeyes.regist.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import net.hight.performance.annotation.Param;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.dao.DomainDao;
import com.megaeyes.regist.dao.HomeDao;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.domain.Authorization;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.DeviceStatus;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.OuterDeviceAlarm;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.domain.PlatformOrgan;
import com.megaeyes.regist.domain.Role;
import com.megaeyes.regist.domain.User;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.GbPlatformType;
import com.megaeyes.regist.webservice.RegistUtil;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("home")
public class HomeController {

	@Autowired
	private RegisterDao registerDao;
	@Autowired
	private RegistUtil registUtil;

	@Autowired
	private HomeDao homeDao;

	@Autowired
	private DomainDao domainDao;

	public void setRegistUtil(RegistUtil registUtil) {
		this.registUtil = registUtil;
	}

	public String getDevicesByUser(Invocation inv,
			@Param("naming") String naming, @Param("cmsId") String resourceCmsId) {
		HttpServletResponse resp = inv.getResponse();
		resp.setCharacterEncoding("GBK");
		String id = naming.split(":")[0];
		String userCmsId = naming.substring(naming.lastIndexOf(":") + 1);
		StringBuilder userId = new StringBuilder(id);
		userId.append("_");
		userId.append(userCmsId);

		User user = Ar.of(User.class).one("userId", userId.toString());
		if (user != null) {
			if (StringUtils.isNotBlank(resourceCmsId)) {
				Platform platform = Ar.of(Platform.class).one("cmsId",
						resourceCmsId);
				if (platform == null) {
					inv.addModel("error", "cmsId is wrong:" + resourceCmsId);
					return "device/device-list";
				} else {
					PlatformOrgan platformOrgan = Ar
							.of(PlatformOrgan.class)
							.one("from PlatformOrgan where sourceId=? and sourceType='platform'",
									platform.getId());
					inv.addModel("platformOrgan", platformOrgan);
					inv.addModel("platform", platform);
				}
			}
			List<Map<String, Object>> deviceList = homeDao.getDevicesByUser(
					user.getId(), userCmsId, resourceCmsId);
			List<Map<String, Object>> organList = homeDao.getOrgansByUser(
					user.getId(), userCmsId, resourceCmsId);
			Map<Integer, List<Map<String, Object>>> organGroup = new HashMap<Integer, List<Map<String, Object>>>();
			Map<Integer, List<Map<String, Object>>> deviceGroup = new HashMap<Integer, List<Map<String, Object>>>();
			List<Map<String, Object>> topOrganList = new ArrayList<Map<String, Object>>();
			Set<Integer> organIds = new HashSet<Integer>();
			if (StringUtils.isBlank(resourceCmsId)) {
				for (Map<String, Object> organ : organList) {
					organIds.add((Integer) organ.get("id"));
				}
				for (Map<String, Object> organ : organList) {
					setOrganGroup(organGroup, (Integer) organ.get("parent_id"),
							organ);
					if (!organIds.contains((Integer) organ.get("parent_id"))) {
						topOrganList.add(organ);
					}
				}
				inv.addModel("topOrgans", topOrganList);
			} else {
				for (Map<String, Object> organ : organList) {
					setOrganGroup(organGroup, (Integer) organ.get("parent_id"),
							organ);
				}
			}

			for (Map<String, Object> device : deviceList) {
				if (deviceGroup.containsKey(device.get("organ_id"))) {
					deviceGroup.get(device.get("organ_id")).add(device);
				} else {
					List<Map<String, Object>> devices = new ArrayList<Map<String, Object>>();
					devices.add(device);
					deviceGroup.put((Integer) device.get("organ_id"), devices);
				}
			}
			Map<String, OuterDeviceAlarm> schemeMap = registerDao
					.getSchemeSetting(userCmsId);
			inv.addModel("organGroup", organGroup);
			inv.addModel("deviceGroup", deviceGroup);
			inv.addModel("schemeSetting", schemeMap);
		} else {
			inv.addModel("error", "user is not permission");
		}
		return "device/device-list";
	}

	public void setOrganGroup(
			Map<Integer, List<Map<String, Object>>> organGroup,
			Integer parentId, Map<String, Object> organ) {
		if (organGroup.containsKey(parentId)) {
			organGroup.get(parentId).add(organ);
		} else {
			List<Map<String, Object>> organs = new ArrayList<Map<String, Object>>();
			organs.add(organ);
			organGroup.put(parentId, organs);
		}
	}

	/**
	 * 获取平台机构树
	 * 
	 * @param inv
	 * @param userNaming
	 * @return
	 */
	public String getOuterPlatforms(Invocation inv,
			@Param("userNaming") String userNaming) {
		HttpServletResponse resp = inv.getResponse();
		resp.setCharacterEncoding("GBK");
		String id = userNaming.split(":")[0];
		String userCmsId = userNaming
				.substring(userNaming.lastIndexOf(":") + 1);
		StringBuilder userId = new StringBuilder(id);
		userId.append("_");
		userId.append(userCmsId);
		Platform platform = Ar.of(Platform.class).one("cmsId", userCmsId);
		User user = Ar.of(User.class).one("userId", userId.toString());
		if (user == null) {
			inv.addModel("user", "no.platform");
		} else {
			List<Platform> children = homeDao.getOuterPlatforms(user.getId(),
					userCmsId);
			Map<Integer, Platform> childMap = new HashMap<Integer, Platform>();
			for (Platform child : children) {
				childMap.put(child.getId(), child);
			}

			List<GbPlatform> gbPlatforms = Ar.of(GbPlatform.class).find(
					"type,", GbPlatformType.CHILD);
			Map<String, Boolean> map = new HashMap<String, Boolean>();
			for (GbPlatform gbPlatform : gbPlatforms) {
				map.put(gbPlatform.getCmsId(), true);
			}
			childMap.remove(platform.getId());
			inv.addModel("platform", platform);
			inv.addModel("childMap", childMap);
			inv.addModel("idMap", map);
		}
		return "platform-list";
	}

	public String getOuterCms(Invocation inv,
			@Param("outerCmsIds") String outerCmsIds,
			@Param("original") String original) {
		return null;
	}

	private String getOuterCmsIdsByUser(User user, Set<String> resourceCmsIds) {
		Set<Role> roles = user.getRoles();
		Set<Integer> grantedIds = new HashSet<Integer>();
		grantedIds.add(user.getId());
		for (Role r : roles) {
			// grantedIds.add(r.getId());
		}
		DetachedCriteria criteria = DetachedCriteria
				.forClass(Authorization.class);
		criteria.add(Restrictions.eq("cmsId", user.getCmsId()));
		criteria.add(Restrictions.in("grantedId", grantedIds));
		if (resourceCmsIds.size() > 0) {
			criteria.add(Restrictions.not(Restrictions.in("resourceCmsId",
					resourceCmsIds)));
		}
		criteria.setProjection(Projections.distinct(Projections
				.property("resourceCmsId")));
		List<String> outerCmsIds = Ar.find(criteria);
		StringBuilder temp = new StringBuilder();
		for (String cmsId : outerCmsIds) {
			temp.append(cmsId).append("__");
		}
		return temp.toString();
	}
	public String verifyPermission(Invocation inv,
			@Param("userNaming") String userNaming,
			@Param("deviceNaming") String deviceNaming,
			@Param("operation") String operation) {
		String userId = userNaming.substring(0, 31) + "_"
				+ userNaming.substring(userNaming.length() - 6);
		User user = Ar.of(User.class).one("userId", userId);
		Device device = Ar.of(Device.class).one("from Device where naming=?",
				deviceNaming);
		int authoration = 1;
		if (user != null) {
			Authorization au = domainDao
					.one(Authorization.class,
							"select au.* from authorization au inner join "+HomeDao.userSQL +"  where  au.resource_cms_id=? and ? regexp concat(au.resource_path,'/') or au.resource_path=? limit 1",
							user.getId(),user.getId(),user.getCmsId(),device.getPlatform().getId(),device.getPath(), device.getPath());
			int idx = getIndexOperation(operation);
			if (au != null && idx<5 && StringUtils.isNotBlank(au.getItem())) {
				
				if (au.getItem().substring(idx, idx + 1).equals("1")) {
					authoration = 0;
				}
			}
		}
		inv.addModel("authoration", authoration);
		return "permission-result";
	}

	public String getDevicesByNaming(Invocation inv,
			@Param("deviceNaming") String deviceNaming,
			@Param("original") String original) {
		return registUtil.getRS(registerDao).getDevicesByNaming(inv,
				new ArrayList<Device>(), deviceNaming, original);
	}

	private int getIndexOperation(String operation) {
		if (operation.equals("REAL")) {
			return 0;
		} else if (operation.equals("HISTORY")) {
			return 1;
		} else if (operation.equals("PTZ")) {
			return 2;
		} else if (operation.equals("PTZ_SET") || operation.equals("PTZ_CALL")) {
			return 3;
		} else if (operation.equals("CAPT")) {
			return 4;
		} else {
			return 5;
		}
	}

	public String getEventServerMsg(Invocation inv,
			@Param("naming") String naming, @Param("type") String type) {
		return registUtil.getRS(registerDao).getEventServerMsg(inv, naming,
				type);
	}

	public String dealDeviceScheme(Invocation inv, @Param("id") String id,
			@Param("deviceNaming") String deviceNaming,
			@Param("schemeId") String schemeId,
			@Param("deviceType") String deviceType,
			@Param("dealType") String dealType, @Param("cmsId") String cmsId) {
		if (dealType.equals("delete")) {
			deleteDeviceScheme(cmsId + "_" + id);
		} else if (dealType.equals("add")) {
			addDeviceScheme(cmsId, id, deviceNaming, schemeId, deviceType);
		}
		return "@success";
	}

	private void addDeviceScheme(String cmsId, String id, String deviceNaming,
			String schemeId, String deviceType) {
		OuterDeviceAlarm outerDeviceAlarm = new OuterDeviceAlarm();
		outerDeviceAlarm.setId(cmsId + "_" + id);
		outerDeviceAlarm.setDeviceNaming(deviceNaming);
		outerDeviceAlarm.setDeviceId(deviceNaming.split(":")[0]);
		outerDeviceAlarm.setSchemeId(schemeId);
		outerDeviceAlarm.setDeviceType(deviceType);
		outerDeviceAlarm.setCmsId(cmsId);
		Ar.save(outerDeviceAlarm);
	}

	private void deleteDeviceScheme(String id) {
		OuterDeviceAlarm outerDeviceAlarm = Ar.of(OuterDeviceAlarm.class).get(
				id);
		if (outerDeviceAlarm != null) {
			Ar.delete(outerDeviceAlarm);
		}
	}

	public String getPlatformUrlByCmsId(Invocation inv,
			@Param("cmsId") String cmsId) {
		return "@"
				+ registUtil.getRS(registerDao).getPlatformUrlByCmsId(inv,
						cmsId);
	}

	public String getDeviceInfoByNaming(Invocation inv,
			@Param("naming") String naming) {
		return registUtil.getRS(registerDao).getDeviceInfoByNaming(inv, naming);

	}

	public String forUI(Invocation inv) {
		return "@home";
	}

	// public static void main(String[] args){
	// String userId= "0000000000200000000000000340000_420000";
	// File fileDir = new File("D:/static");
	// File[] files = fileDir.listFiles();
	// for(File file : files){
	// if (file.getName().indexOf(userId, 0)!=-1) {
	// file.delete();
	// }
	// }
	// }

	public String forTest() {
		return "for-test";
	}

	public String testMe(Invocation inv) {
		inv.addModel("name", "sdfsadf");
		return "test-me";
	}

	public String testStatus() {
		// DeviceServer server =
		// Ar.of(DeviceServer.class).one("from DeviceServer where naming is not null");
		// RequestEntity entity = new RequestEntity();
		// String status = entity.getStatus(server);
		// System.out.println(status);
		return "@success";
	}

	public String testConfig(@Param("key") String key) {
		String message = registUtil.getMessage(key);
		return message;
	}

	public String testOpenSession() {
		List<Organ> organs = Ar.of(Organ.class).find();
		Set<Organ> organSet = new HashSet<Organ>();
		organSet.addAll(organs);
		registerDao.deleteOrgans(organSet);
		return "@s";
	}

	public String mysql() {
		Organ organ = Ar.of(Organ.class).get(
				"0000000000200000000000000640005_34010000002000000001");
		if (organ != null) {
			System.out.println(organ.getName());
		}
		return "@s";
	}

	public String testClone() {
		Organ organ = Ar.of(Organ.class).one();
		Organ clone = organ.clone();
		clone.setName(clone.getName() + "_molc");
		Ar.update(clone);
		return "@OK";
	}

	public String testioc() {
		// Map<String,SendResourceDao> map =
		// HPFilter.getContext().getBeansOfType(SendResourceDao.class);
		return "@1";
	}

	public String testHibernate() {
		DeviceStatus status = Ar.of(DeviceStatus.class).one("device.serverId",
				"0000000000200000000000000360220_420100");
		System.out.println(status);
		return "@success";
	}

	public String updateDeviceStatus() {
		List<DeviceStatus> list = Ar.of(DeviceStatus.class).find();
		for (DeviceStatus status : list) {
			status.setStatus(Status.add.name());
		}
		return "@success";
	}

}
