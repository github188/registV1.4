package com.megaeyes.regist.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SubscribeEvent implements Comparable<SubscribeEvent> {
	private Integer id;
	private String subscribeId;
	private Date expireDate;
	private String deviceId;
	private Integer platformId;//上级平台id,对应于gb_platform的id
	private String path; // 对应于gb_organ的path
	private Integer organId;// 对应于gb_organ的id

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSubscribeId() {
		return subscribeId;
	}

	public void setSubscribeId(String subscribeId) {
		this.subscribeId = subscribeId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SubscribeEvent) {
			SubscribeEvent other = (SubscribeEvent) obj;
			if (this.getSubscribeId().equals(other.getSubscribeId())
					&& this.getDeviceId().equals(other.getDeviceId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (this.getSubscribeId() + this.getDeviceId()).hashCode();
	}

	@Override
	public int compareTo(SubscribeEvent other) {
		return other.getDeviceId().compareTo(this.getDeviceId());
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getOrganId() {
		return organId;
	}

	public void setOrganId(Integer organId) {
		this.organId = organId;
	}

	public Integer getPlatformId() {
		return platformId;
	}

	public void setPlatformId(Integer platformId) {
		this.platformId = platformId;
	}

}
