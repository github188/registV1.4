package com.megaeyes.regist.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class OuterDeviceAlarm {
	private String id;
	private String deviceId;
	private String deviceNaming;
	private String deviceType;
	private String schemeId;
	private String cmsId;
	
	@Id
	@Column(length=31)
	public String getId() {
		return this.id;
	}
	
	@Transient
	public String getAlarmDeviceId() {
		return this.id.substring(7);
	}
	@Transient
	public void setAlarmDeviceId(String id) {
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(length=31)
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	@Column(length=100)
	public String getDeviceNaming() {
		return deviceNaming;
	}

	public void setDeviceNaming(String deviceNaming) {
		this.deviceNaming = deviceNaming;
	}

	@Column(length=50)
	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	@Column(length=31)
	public String getSchemeId() {
		return schemeId;
	}

	public void setSchemeId(String schemeId) {
		this.schemeId = schemeId;
	}

	@Column(length=6)
	public String getCmsId() {
		if(cmsId == null){
			System.out.println(deviceNaming);
			cmsId = deviceNaming.split(":")[3];
		}
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}
}
