package com.megaeyes.regist.webservice;

import java.util.List;

import javax.jws.WebService;

import com.megaeyes.regist.bean.IDevice;
import com.megaeyes.regist.bean.IDeviceServer;
import com.megaeyes.regist.bean.IOrgan;
import com.megaeyes.regist.bean.IPlatform;
import com.megaeyes.regist.bean.UserBean;
import com.megaeyes.regist.domain.OuterDeviceAlarm;

@WebService
public interface RegistCore {
	public String regist(IPlatform platform);
	public String parentPlatformRegist(IPlatform parent,List<IPlatform> list);
	public String deviceRegist(String cmsId,List<IDevice> deviceList);
	public List<IPlatform> getPlatforms(String parentCmsId);
	public String organRegist(String cmsId,List<IOrgan> organList);
	public IPlatform getPlatform(String cmsId);
	public String getOuterDeviceAlarm(List<OuterDeviceAlarm> deviceAlarms,String cmsId);
	public String userRegist(String cmsId,List<UserBean> beans);
	public String visRegist(String cmsId,List<IDeviceServer> servers);
}
