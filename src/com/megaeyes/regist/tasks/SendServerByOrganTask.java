package com.megaeyes.regist.tasks;

import java.util.List;
import java.util.concurrent.Callable;

import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.ServerStatus;
import com.megaeyes.regist.utils.sendResource.GBUtils;
import com.megaeyes.regist.utils.sendResource.SendParam;
import com.megaeyes.regist.webservice.RegistUtil;

public class SendServerByOrganTask implements Callable<String> {
	private GbPlatform platform;
	private GBUtils gbUtils;
	private RequestEntity entity;
	private SendParam sendParam;
	private String organStdId;
	private RegistUtil registUtil;
	List<ServerStatus> serverStatusList;

	@Override
	public String call() throws Exception {
		sendServersByOrgan(sendParam, entity, platform, organStdId,
				serverStatusList);
		return "success";
	}

	public void sendServersByOrgan(SendParam sendParam, RequestEntity entity,
			GbPlatform platform, String organStdId,
			List<ServerStatus> serverStatusList) {
//		sendParam.getEntity().setDeviceID(organStdId);
		if (serverStatusList.size() > 0) {
			gbUtils.sendMsg(platform.getSipServer(), gbUtils.getServerContents(
					serverStatusList, entity, organStdId), entity
					.getFromDeviceID());
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

	public String getOrganStdId() {
		return organStdId;
	}

	public void setOrganStdId(String organStdId) {
		this.organStdId = organStdId;
	}

	public RegistUtil getRegistUtil() {
		return registUtil;
	}

	public void setRegistUtil(RegistUtil registUtil) {
		this.registUtil = registUtil;
	}

	public List<ServerStatus> getServerStatusList() {
		return serverStatusList;
	}

	public void setServerStatusList(List<ServerStatus> serverStatusList) {
		this.serverStatusList = serverStatusList;
	}
}
