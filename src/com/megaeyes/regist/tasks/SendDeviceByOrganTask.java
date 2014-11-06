package com.megaeyes.regist.tasks;

import java.util.List;
import java.util.concurrent.Callable;

import com.megaeyes.regist.domain.DeviceStatus;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.utils.sendResource.GBUtils;
import com.megaeyes.regist.utils.sendResource.SendParam;
import com.megaeyes.regist.webservice.RegistUtil;

public class SendDeviceByOrganTask implements Callable<String> {
	private SendParam sendParam;
	private GbPlatform platform;
	private RegistUtil registUtil;
	private GBUtils gbUtils;

	@Override
	public String call() throws Exception {
		sendDevicesByOrgan(sendParam, platform);
		return "success";
	}

	private void sendDevicesByOrgan(SendParam sendParam,
			GbPlatform platform) {
		int i = 0;
		while (true) {
			List<DeviceStatus> deviceStatusList = registUtil
					.getSendResourceDao(sendParam.getPlatform())
					.getDevicesByOrganWithId(sendParam, i++);
			if (deviceStatusList.size() == 0) {
				break;
			}
			gbUtils.sendMsg(platform.getSipServer(),
					gbUtils.getDeviceContents(deviceStatusList, sendParam.getEntity()),
					sendParam.getEntity().getFromDeviceID());
		}
	}

	public SendParam getSendParam() {
		return sendParam;
	}

	public void setSendParam(SendParam sendParam) {
		this.sendParam = sendParam;
	}

	public GbPlatform getPlatform() {
		return platform;
	}

	public void setPlatform(GbPlatform platform) {
		this.platform = platform;
	}

	public RegistUtil getRegistUtil() {
		return registUtil;
	}

	public void setRegistUtil(RegistUtil registUtil) {
		this.registUtil = registUtil;
	}

	public GBUtils getGbUtils() {
		return gbUtils;
	}

	public void setGbUtils(GBUtils gbUtils) {
		this.gbUtils = gbUtils;
	}
}
