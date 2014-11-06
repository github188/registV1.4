package com.megaeyes.regist.webservice;

import java.util.List;

import com.megaeyes.regist.bean.Require;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.utils.sendResource.ConfigUtil;
import com.megaeyes.utils.Invocation;

public interface Regist {
	public static String ownerCmsId = ConfigUtil.OWNER_PLATFORM_CMS_ID;
	public void parentPlatform();

	public void parentRegistDevice(String cmsId);

	public void parentRegistOrgan(String cmsId);
	
	public void parentRegistVIS(String cmsId);

	public String shareResource(Require r);

	public String getPlatforms(Invocation inv, String cmsId, String first,
			String name);

	public String getSharePlatforms(Invocation inv, String obtainCmsId,
			String first);

	public String getSharePlatformsByCmsIds(Invocation inv, String shareCmsIds,
			String original);

	public String getDevicesByNaming(Invocation inv, List<Device> devices,
			String deviceNaming, String original);

	public String getEventServerMsg(Invocation inv, String naming, String type);


	public String getOuterPlatformByCmsId(Invocation inv, String platformCmsId,
			String cmsId);

	public String getPlatformUrlByCmsId(Invocation inv, String cmsId);

	public void outerDeviceRegist(String platformId, String accessIP,
			String parentCmsId, String resource);

	public String getDeviceInfoByNaming(Invocation inv,String naming);
	
	public String hasShareResource(String cmsId);

	public void parentRegistByDeviceId(Integer id);
	public void parentRegistByServerId(Integer id);

	public void parentRegistByOrganId(Integer id);
}
