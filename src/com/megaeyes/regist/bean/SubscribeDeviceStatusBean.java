package com.megaeyes.regist.bean;

import com.megaeyes.regist.domain.DeviceStatus;
import com.megaeyes.regist.domain.SubscribeEvent;

public class SubscribeDeviceStatusBean {
	private SubscribeEvent subscribeEvent;
	private DeviceStatus deviceStatus;

	public SubscribeEvent getSubscribeEvent() {
		return subscribeEvent;
	}

	public void setSubscribeEvent(SubscribeEvent subscribeEvent) {
		this.subscribeEvent = subscribeEvent;
	}

	public DeviceStatus getDeviceStatus() {
		return deviceStatus;
	}

	public void setDeviceStatus(DeviceStatus deviceStatus) {
		this.deviceStatus = deviceStatus;
	}
}
