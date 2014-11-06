package com.megaeyes.regist.bean;

import com.megaeyes.regist.domain.OrganStatus;
import com.megaeyes.regist.domain.SubscribeEvent;

public class SubscribeOrganStatusBean {
	private SubscribeEvent subscribeEvent;
	private OrganStatus organStatus;

	public SubscribeEvent getSubscribeEvent() {
		return subscribeEvent;
	}

	public void setSubscribeEvent(SubscribeEvent subscribeEvent) {
		this.subscribeEvent = subscribeEvent;
	}

	public OrganStatus getOrganStatus() {
		return organStatus;
	}

	public void setOrganStatus(OrganStatus organStatus) {
		this.organStatus = organStatus;
	}

}
