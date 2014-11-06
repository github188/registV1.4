package com.megaeyes.regist.bean;

import java.io.Serializable;

public class PlatformBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 273716626212384737L;
	private String id;
	private String name;
	private String serviceUrl;
	private String password;
	private String parentId;
	private String eventServerIp;
	private Integer eventServerPort;
	private boolean share = true;
	private boolean owner;
	private String gbCmsId; //对应于gbPlatform表中的childCmsId

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public boolean isShare() {
		return share;
	}

	public void setShare(boolean share) {
		this.share = share;
	}
	
	public String getEventServerIp() {
		return eventServerIp;
	}

	public void setEventServerIp(String eventServerIp) {
		this.eventServerIp = eventServerIp;
	}

	public Integer getEventServerPort() {
		return eventServerPort;
	}

	public void setEventServerPort(Integer eventServerPort) {
		this.eventServerPort = eventServerPort;
	}

	public boolean isOwner() {
		return owner;
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}
	
	public String getGbCmsId() {
		return gbCmsId;
	}

	public void setGbCmsId(String gbCmsId) {
		this.gbCmsId = gbCmsId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PlatformBean) {
			PlatformBean other = (PlatformBean) obj;
			if(this.getId().equals(other.getId())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	public boolean compareTo(PlatformBean platform){
		if (((this.getName() != null && !this.getName().equals(platform.getName())) 
				||this.getName()==null && platform.getName()!=null)
				|| ((this.getServiceUrl()!=null && !this.getServiceUrl().equals(platform.getServiceUrl()))
						||this.getServiceUrl()==null && platform.getServiceUrl()!=null)
				|| ((this.getEventServerIp()!=null && !this.getEventServerIp().equals(platform.getEventServerIp()))
						|| this.getEventServerIp()==null && platform.getEventServerIp()!=null)
				|| ((this.getEventServerPort()!=null && !this.getEventServerPort().equals(platform.getEventServerPort()))
						|| this.getEventServerPort()==null && platform.getEventServerPort()!=null)){
			return true;
		}
			return false;
	}

}
