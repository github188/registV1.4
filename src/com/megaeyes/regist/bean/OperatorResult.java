package com.megaeyes.regist.bean;

public class OperatorResult {
	private String retTime;
	private String deviceId; //设备ID
	private String result;//操作结果
	private String method;//操作方法
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}

	public String getRetTime() {
		return retTime;
	}
	public void setRetTime(String retTime) {
		this.retTime = retTime;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	
	
}
