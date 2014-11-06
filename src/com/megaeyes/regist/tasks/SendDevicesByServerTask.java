package com.megaeyes.regist.tasks;

import java.util.List;
import java.util.concurrent.Callable;

import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.domain.DeviceStatus;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.utils.sendResource.GBUtils;
import com.megaeyes.regist.utils.sendResource.SendParam;
import com.megaeyes.regist.webservice.RegistUtil;

public class SendDevicesByServerTask implements Callable<String>{
	private GbPlatform platform;
	private GBUtils gbUtils;
	private RequestEntity entity;
	private SendParam sendParam;
	private String serverStdId;
	private RegistUtil registUtil;
	private List<DeviceStatus> deviceStatusList;

	@Override
	public String call() throws Exception {
		sendDevicesByServer(sendParam, entity, platform, serverStdId,deviceStatusList);
		return "success";
	}

	public void sendDevicesByServer(SendParam sendParam, RequestEntity entity,
			GbPlatform platform, String serverStdId,List<DeviceStatus> deviceStatusList) {
		if(deviceStatusList.size()>0){
			gbUtils.sendMsg(
					platform.getSipServer(),
					gbUtils.getDeviceContents(deviceStatusList, entity,serverStdId),
					entity.getFromDeviceID());
		}
	}

	public GbPlatform getPlatform() {
		return platform;
	}

	public void setPlatform(GbPlatform platform) {
		this.platform = platform;
	}

	public GBUtils getGbUtils() {
		return gbUtils;
	}

	public void setGbUtils(GBUtils gbUtils) {
		this.gbUtils = gbUtils;
	}

	public RequestEntity getEntity() {
		return entity;
	}

	public void setEntity(RequestEntity entity) {
		this.entity = entity;
	}

	public SendParam getSendParam() {
		return sendParam;
	}

	public void setSendParam(SendParam sendParam) {
		this.sendParam = sendParam;
	}

	public RegistUtil getRegistUtil() {
		return registUtil;
	}

	public void setRegistUtil(RegistUtil registUtil) {
		this.registUtil = registUtil;
	}

	public String getServerStdId() {
		return serverStdId;
	}

	public void setServerStdId(String serverStdId) {
		this.serverStdId = serverStdId;
	}

	public List<DeviceStatus> getDeviceStatusList() {
		return deviceStatusList;
	}

	public void setDeviceStatusList(List<DeviceStatus> deviceStatusList) {
		this.deviceStatusList = deviceStatusList;
	}
}
