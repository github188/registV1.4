package com.megaeyes.regist.bean;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;

public class OnlineStatus implements Comparable<OnlineStatus>{
	private String accessIp;
	private Set<String> deviceIds = new HashSet<String>();
	private Date startTime;
	private boolean online;

	public String getAccessIp() {
		return accessIp;
	}

	public void setAccessIp(String accessIp) {
		this.accessIp = accessIp;
	}

	public Set<String> getDeviceIds() {
		return deviceIds;
	}

	public void setDeviceIds(Set<String> deviceIds) {
		this.deviceIds = deviceIds;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	public String getStatus(String id){
		if(deviceIds.contains(id) && this.isOnline()){
			return "ON";
		}else{
			return "OFF";
		}
	}
	
	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof OnlineStatus)){
			return false;
		}
		OnlineStatus other = (OnlineStatus) obj;
		if(other.getAccessIp() == this.getAccessIp()){
			return true;
		}else{
			return false;
		}
	}
	@Override
	public int hashCode() {
		return this.getAccessIp().hashCode();
	}
	
	@Override
	public int compareTo(OnlineStatus o) {
		CRC32 crc32 = new CRC32();
		crc32.update(this.getAccessIp().getBytes());
		CRC32 other = new CRC32();
		other.update(o.getAccessIp().getBytes());
		return (int)other.getValue() - (int)crc32.getValue();
	}
}
