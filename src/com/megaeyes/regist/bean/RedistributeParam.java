package com.megaeyes.regist.bean;

public class RedistributeParam {
	private String cmsId;
	private String VICIds;
	private String IPVICIds;
	private String AICIds;
	private String ORGANIds;
	private String organId;
	private String name;
	private String first = "true";
	private String resourceType="VIC";
	private String resourceCmsId;
	private String targetCmsId;
	private String targetOrganId;

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	

	public String getOrganId() {
		return organId;
	}

	public void setOrganId(String organId) {
		this.organId = organId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceCmsId() {
		return resourceCmsId;
	}

	public void setResourceCmsId(String resourceCmsId) {
		this.resourceCmsId = resourceCmsId;
	}

	public String getVICIds() {
		return VICIds;
	}

	public void setVICIds(String vICIds) {
		VICIds = vICIds;
	}

	public String getIPVICIds() {
		return IPVICIds;
	}

	public void setIPVICIds(String iPVICIds) {
		IPVICIds = iPVICIds;
	}

	public String getAICIds() {
		return AICIds;
	}

	public void setAICIds(String aICIds) {
		AICIds = aICIds;
	}

	public String getORGANIds() {
		return ORGANIds;
	}

	public void setORGANIds(String oRGANIds) {
		ORGANIds = oRGANIds;
	}

	public String getTargetCmsId() {
		return targetCmsId;
	}

	public void setTargetCmsId(String targetCmsId) {
		this.targetCmsId = targetCmsId;
	}

	public String getTargetOrganId() {
		return targetOrganId;
	}

	public void setTargetOrganId(String targetOrganId) {
		this.targetOrganId = targetOrganId;
	}
}
