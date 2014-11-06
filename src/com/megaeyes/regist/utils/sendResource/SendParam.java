package com.megaeyes.regist.utils.sendResource;

import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.domain.GbPlatform;

public class SendParam {
	private GbPlatform platform;
	private RequestEntity entity;
	
	public SendParam(GbPlatform platform,RequestEntity entity) {
		this.entity = entity;
		this.platform = platform;
	}

	public GbPlatform getPlatform() {
		return platform;
	}

	public void setPlatform(GbPlatform platform) {
		this.platform = platform;
	}

	public RequestEntity getEntity() {
		return entity;
	}

	public void setEntity(RequestEntity entity) {
		this.entity = entity;
	}

}
