package com.megaeyes.regist.bean;

import com.megaeyes.regist.domain.ResourceStatus;

public class RegistParam {
	private ResourceStatus beginResourceStatus;
	private ResourceStatus endResourceStatus;
	private String cmsId;

	public ResourceStatus getBeginResourceStatus() {
		return beginResourceStatus;
	}

	public void setBeginResourceStatus(ResourceStatus beginResourceStatus) {
		this.beginResourceStatus = beginResourceStatus;
	}

	public ResourceStatus getEndResourceStatus() {
		return endResourceStatus;
	}

	public void setEndResourceStatus(ResourceStatus endResourceStatus) {
		this.endResourceStatus = endResourceStatus;
	}

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}
}
