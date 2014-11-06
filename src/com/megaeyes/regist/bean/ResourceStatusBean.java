package com.megaeyes.regist.bean;

import com.megaeyes.regist.domain.GbPlatform;

public class ResourceStatusBean {
	private int statusId;
	private String resourceId;
	private String resourceType;
	private GbPlatform platform;

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public GbPlatform getPlatform() {
		return platform;
	}

	public void setPlatform(GbPlatform platform) {
		this.platform = platform;
	}
}
