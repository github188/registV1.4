package com.megaeyes.regist.bean;

import com.megaeyes.regist.domain.NotifyStatus;
import com.megaeyes.regist.domain.SubscribeEvent;

public class SubscribeNotifyStatusBean {
	private SubscribeEvent subscribeEvent;
	private NotifyStatus notifyStatus;

	public SubscribeEvent getSubscribeEvent() {
		return subscribeEvent;
	}

	public void setSubscribeEvent(SubscribeEvent subscribeEvent) {
		this.subscribeEvent = subscribeEvent;
	}

	public NotifyStatus getNotifyStatus() {
		return notifyStatus;
	}

	public void setNotifyStatus(NotifyStatus notifyStatus) {
		this.notifyStatus = notifyStatus;
	}
	
	

}
