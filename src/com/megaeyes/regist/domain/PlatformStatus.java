package com.megaeyes.regist.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"gb_platform_id"}))
public class PlatformStatus {
	private Integer id;
	private GbPlatform gbPlatform;
	private boolean online;
	private String errorCode;
	private String description;
	private Date heartbeatTime; // 心跳时间
	private Integer heartBeatCycle;// 心跳的周期(秒)

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@OneToOne()
	@JoinColumn(name="gb_platform_id")
	public GbPlatform getGbPlatform() {
		return gbPlatform;
	}

	public void setGbPlatform(GbPlatform gbPlatform) {
		this.gbPlatform = gbPlatform;
	}

	

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getHeartbeatTime() {
		return heartbeatTime;
	}

	public void setHeartbeatTime(Date heartbeatTime) {
		this.heartbeatTime = heartbeatTime;
	}

	public Integer getHeartBeatCycle() {
		return heartBeatCycle;
	}

	public void setHeartBeatCycle(Integer heartBeatCycle) {
		this.heartBeatCycle = heartBeatCycle;
	}
}
