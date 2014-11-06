package com.megaeyes.regist.bean;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MsgRecord {
	private String cmsId;
	private String queryMsg;
	private Date queryTime;
	private Date lastReveiveTime;
	private int sumNum=0;
	private final AtomicInteger currentSumNum = new AtomicInteger(0);
	private ConcurrentHashMap<String, Object[]> deviceParams = new ConcurrentHashMap<String, Object[]>();
	private boolean finish;

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public String getQueryMsg() {
		return queryMsg;
	}

	public void setQueryMsg(String queryMsg) {
		this.queryMsg = queryMsg;
	}

	public Date getQueryTime() {
		return queryTime;
	}

	public void setQueryTime(Date queryTime) {
		this.queryTime = queryTime;
	}

	public Date getLastReveiveTime() {
		return lastReveiveTime;
	}

	public void setLastReveiveTime(Date lastReveiveTime) {
		this.lastReveiveTime = lastReveiveTime;
	}

	public Integer getCurrentSumNum(Integer devices) {
		return currentSumNum.addAndGet(devices);
	}
	public Integer getCurrentSumNum() {
		return currentSumNum.get();
	}

	public int getSumNum() {
		return sumNum;
	}

	public void setSumNum(int sumNum) {
		this.sumNum = sumNum;
	}

	public ConcurrentHashMap<String, Object[]> getDeviceParams() {
		return deviceParams;
	}

	public void setDeviceParams(ConcurrentHashMap<String, Object[]> deviceParams) {
		this.deviceParams = deviceParams;
	}

	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}
}
