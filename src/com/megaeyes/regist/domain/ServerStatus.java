package com.megaeyes.regist.domain;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.megaeyes.utils.Ar;

@Entity
public class ServerStatus implements NotifyStatus{
	private Integer id;
	private DeviceServer server; // 关联的device_server
	private String status; // 状态 add,update,delete
	private boolean baseNotify;// 基本信息变更通知（增，删，改）
	private Timestamp changeTime; // 基本信息更新时间
	private int nanosecond; // 纳秒
	private String online="OFF"; // 设备是否在线
	private boolean onlineNotify; // 上下级是否已经进行了消息通知
	private Timestamp onlineChangeTime; // 状态更新时间
	private int onlineNanosecond; // 纳秒
	private GbPlatform platform;
	
	private Integer serverId;
	private String name;
	private String location;

	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isBaseNotify() {
		return baseNotify;
	}

	public void setBaseNotify(boolean baseNotify) {
		this.baseNotify = baseNotify;
	}

	public Timestamp getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(Timestamp changeTime) {
		this.changeTime = changeTime;
	}

	public int getNanosecond() {
		return nanosecond;
	}

	public void setNanosecond(int nanosecond) {
		this.nanosecond = nanosecond;
	}

	public boolean isOnlineNotify() {
		return onlineNotify;
	}

	public void setOnlineNotify(boolean onlineNotify) {
		this.onlineNotify = onlineNotify;
	}


	public int getOnlineNanosecond() {
		return onlineNanosecond;
	}

	public void setOnlineNanosecond(int onlineNanosecond) {
		this.onlineNanosecond = onlineNanosecond;
	}

	@ManyToOne
	public GbPlatform getPlatform() {
		return platform;
	}

	public void setPlatform(GbPlatform platform) {
		this.platform = platform;
	}

	public String getOnline() {
		return online;
	}

	public void setOnline(String online) {
		this.online = online;
	}

	public Timestamp getOnlineChangeTime() {
		return onlineChangeTime;
	}

	public void setOnlineChangeTime(Timestamp onlineChangeTime) {
		this.onlineChangeTime = onlineChangeTime;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ServerStatus)) {
			return false;
		}
		ServerStatus other = (ServerStatus) obj;
		if (this.getId().equals(other.getId())) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	public Integer getServerId() {
		return serverId;
	}

	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	

	@Transient
	public DeviceServer getServer() {
		if(this.server == null){
			this.server = Ar.of(DeviceServer.class).get(this.getServerId());
		}
		return server;
	}

	public void setServer(DeviceServer server) {
		this.server = server;
	}
}
