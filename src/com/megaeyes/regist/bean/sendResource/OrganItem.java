package com.megaeyes.regist.bean.sendResource;

public class OrganItem {
	private String deviceID;
	private String name;
	private IEntity parent;
	private String parentID="";
	private String businessGroupID;

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IEntity getParent() {
		return parent;
	}

	public void setParent(IEntity parent) {
		this.parent = parent;
	}

	public String getParentID() {
		return parentID;
	}

	public void setParentID(String parentID) {
		this.parentID = parentID;
	}

	public String getBusinessGroupID() {
		return businessGroupID;
	}

	public void setBusinessGroupID(String businessGroupID) {
		this.businessGroupID = businessGroupID;
	}
}
