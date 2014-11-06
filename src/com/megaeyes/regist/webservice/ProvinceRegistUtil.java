package com.megaeyes.regist.webservice;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.megaeyes.regist.bean.Require;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.domain.Share;
import com.megaeyes.regist.other.ResourceType;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.GbPlatformType;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

public class ProvinceRegistUtil implements Regist {
	private RegisterDao registerDao;

	public ProvinceRegistUtil(RegisterDao registerDao) {
		this.registerDao = registerDao;
	}
	
	@Override
	public void parentPlatform() {
		List<Platform> platforms = Ar.of(Platform.class).find(
				"from Platform where status!=?", Status.normal);
		for (Platform platform : platforms) {
			platform.setStatus(Status.normal);
			Ar.update(platform);
		}
	}

	@Override
	public void parentRegistDevice(String cmsId) {
		Timestamp changeTime = new Timestamp(System.currentTimeMillis());
		int nanosecond = changeTime.getNanos() / 1000;
		registerDao.changeDeviceStatus(cmsId, changeTime, nanosecond);
	}


	@Override
	public void parentRegistOrgan(String cmsId) {
		Timestamp changeTime = new Timestamp(System.currentTimeMillis());
		int nanosecond = changeTime.getNanos() / 1000;
		registerDao.changeOrganStatus(cmsId,changeTime,nanosecond);
	}

	@Override
	public String shareResource(Require r) {
		return "success";
	}

	@Override
	public String getPlatforms(Invocation inv, String cmsId, String first,
			String name) {
		registerDao.getPlatforms(inv, cmsId, name);
		if ("true".equals(first)) {
			return PlatformUtils.getSharePath("platform-list");
		} else {
			return PlatformUtils.getSharePath("share-platform-xml");
		}
	}

	@Override
	public String getSharePlatforms(Invocation inv, String obtainCmsId,
			String first) {
		List<Platform> platforms = registerDao.getSharePlatforms(inv,
				obtainCmsId);
		inv.addModel("platforms", platforms);

		if ("true".equals(first)) {
			inv.getRequest().getSession().setAttribute("platforms", platforms);
			return PlatformUtils.getObtainPath("share-platform-xml");
		} else {
			return PlatformUtils.getObtainPath("share-platform-xml");
		}
	}


	@Override
	public String getSharePlatformsByCmsIds(Invocation inv, String cmsIds,
			String original) {
		String[] cmsIdsArr = cmsIds.split("__");
		DetachedCriteria criteria = DetachedCriteria.forClass(Platform.class);
		criteria.add(Restrictions.in("id", cmsIdsArr));
		inv.addModel("platforms", Ar.find(criteria));
		if ("true".equals(original)) {
			return PlatformUtils.getSharePath("share-platform");
		} else {
			return PlatformUtils.getSharePath("share-platform-xml");
		}
	}


	@Override
	public String getOuterPlatformByCmsId(Invocation inv, String platformCmsId,
			String cmsId) {
		return registerDao.getOuterPlatformByCmsId(platformCmsId, cmsId);
	}


	@Override
	public String getDevicesByNaming(Invocation inv, List<Device> devices,
			String deviceNaming, String original) {
		List<Device> list = Ar.of(Device.class).find(
				"from Device where naming=? and (type=? or type=?)",
				deviceNaming, ResourceType.VIC.name(),
				ResourceType.IPVIC.name());
		for (Device device : list) {
			System.out.println("@@@@@@@@@@@@@@@@@@@@@" + device.getId());
		}
		devices.addAll(list);
//		devices.addAll(registerDao.getDevicesByNaming(deviceNaming));
		if (original.equals("false")) {
			inv.addModel("devices", devices);
			return "device-list-xml";
		}
		return "";
	}

	@Override
	public String getEventServerMsg(Invocation inv, String naming, String type) {
		String original = inv.getRequest().getParameter("original");
		List<Platform> platforms = new ArrayList<Platform>();
		platforms = registerDao.getEventServerMsg(platforms, naming, type);
		inv.addModel("platforms", platforms);
		if (original != null) {
			return PlatformUtils.getSharePath("share-platform-xml");
		} else {
			return "event-server";
		}
	}

	@Override
	public String getPlatformUrlByCmsId(Invocation inv, String cmsId) {
		Platform platform = Ar.of(Platform.class).get(cmsId);
		String url = "";
		if (platform != null && platform.getServiceUrl() != null) {
			url = platform.getServiceUrl().replaceFirst("/platform_grade", "");
			url = url.replaceFirst("/share", "");
			return url;
		} else {

		}
		return url;
	}
	
	

	@Override
	public String getDeviceInfoByNaming(Invocation inv, String naming) {
		Device device = Ar.of(Device.class).one("naming", naming);
		Platform platform = Ar.of(Platform.class).get(device.getCmsId());
		String url = "";
		if (platform != null && platform.getServiceUrl() != null) {
			url = platform.getServiceUrl();
			String deviceInfo = registerDao.getDeviceInfoByUrl(url,
					device.getCmsId(), device);
			if (StringUtils.isBlank(deviceInfo)) {
				return registerDao.getDeviceInfoByNaming(inv, naming);
			}
			return "@" + deviceInfo;
		} else if (platform != null && platform.getServiceUrl() == null) {
			// 国标的下级平台设备
			if (StringUtils.isBlank(url)) {
				return registerDao.getDeviceInfoByNaming(inv, naming);
			}
		}
		return "@";
	}

	@Override
	public void outerDeviceRegist(String platformId, String accessIP,
			String parentCmsId, String resource) {
		// TODO Auto-generated method stub

	}

	@Override
	public void parentRegistVIS( String cmsId) {
		Timestamp changeTime = new Timestamp(System.currentTimeMillis());
		int nanosecond = changeTime.getNanos() / 1000;
		registerDao
				.updateStatus(cmsId, "device_server", changeTime, nanosecond);
		registerDao
				.deleteStatus(cmsId, "device_server", changeTime, nanosecond);
	}

	@Override
	public String hasShareResource(String cmsId) {
		Share share = Ar.of(Share.class).one("platformCmsId", cmsId);
		if (share != null) {
			return "yes";
		}
		return "no";
	}

	@Override
	public void parentRegistByDeviceId(Integer id) {
		DetachedCriteria criteria = DetachedCriteria.forClass(GbPlatform.class);
		criteria.add(Restrictions.eq("type", GbPlatformType.PARENT));
		registerDao.updateStatus("device", id);
		registerDao.deleteStatus("device", id);
	}

	@Override
	public void parentRegistByServerId(Integer id) {
		DetachedCriteria criteria = DetachedCriteria.forClass(GbPlatform.class);
		criteria.add(Restrictions.eq("type", GbPlatformType.PARENT));
		registerDao.updateStatus("device_server", id);
		registerDao.deleteStatus("device_server", id);
		registerDao.deleteOrganServerByServerId(id);
	}

	@Override
	public void parentRegistByOrganId(Integer id) {
		DetachedCriteria criteria = DetachedCriteria.forClass(GbPlatform.class);
		criteria.add(Restrictions.eq("type", GbPlatformType.PARENT));
		registerDao.updateStatus("organ", id);
		registerDao.deleteStatus("organ", id);
		Organ organ = Ar.of(Organ.class).get(id);
		Map<String, String> map = new HashMap<String, String>();
		map.put("cmsId", organ.getCmsId());
		registerDao.accessSelf(map, "/organ/topRegistOrgan");
	}
}
