package com.megaeyes.regist.domain;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.megaeyes.regist.enump.SubscribeStatus;

//作为上级的订阅记录
@Entity
public class Subscribe {
	private Integer id;
	private String toPlatformId;// 接受订阅的平台id
	private String fromPlatformId;// 发起订阅的平台id,可能是国标上级的id,也可以是本平台的国标id
	private Timestamp subscribeTime;// 订阅时间
	private String deviceId;// 接受订阅目标id,可能是平台，机构，视频服务器，终端设备id
	private SubscribeStatus status;// 订阅状态，正常，取消
	private Integer period;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getToPlatformId() {
		return toPlatformId;
	}

	public void setToPlatformId(String toPlatformId) {
		this.toPlatformId = toPlatformId;
	}

	public String getFromPlatformId() {
		return fromPlatformId;
	}

	public void setFromPlatformId(String fromPlatformId) {
		this.fromPlatformId = fromPlatformId;
	}

	public Timestamp getSubscribeTime() {
		return subscribeTime;
	}

	public void setSubscribeTime(Timestamp subscribeTime) {
		this.subscribeTime = subscribeTime;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public SubscribeStatus getStatus() {
		return status;
	}

	public void setStatus(SubscribeStatus status) {
		this.status = status;
	}

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

}
