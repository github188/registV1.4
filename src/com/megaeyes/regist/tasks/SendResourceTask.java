package com.megaeyes.regist.tasks;

import java.util.concurrent.Callable;

import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.utils.sendResource.GBUtils;

public class SendResourceTask implements Callable<String> {
	private GbPlatform platform;
	private GBUtils gbUtils;
	private RequestEntity entity;
	private String content;

	@Override
	public String call() throws Exception {
		sendServer(platform, entity,content);
		return "finish";
	}

	public void sendServer(GbPlatform platform, RequestEntity entity,String Content) {
		gbUtils.sendMsg(platform.getSipServer(),
				content,
				entity.getFromDeviceID());
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
