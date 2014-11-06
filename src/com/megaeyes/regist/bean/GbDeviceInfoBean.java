package com.megaeyes.regist.bean;
/**
 * 
 * @author zlj
 * 国标测试的设备查询信息
 */
/*
 * <DeviceID>64010000001110000001</DeviceID> 
	<Result>OK</Result> 
	<DeviceType>DVR</DeviceType> 
	<Manufacturer>Tiandy</Manufacturer> 
	<Model>TC-2808AN-HD</Model> 
	<Firmware>V2.1, build 091111</Firmware>
	 	<MaxCamera>8</MaxCamera>
	 	<MaxAlarm>16</MaxAlarm> 
 */
public class GbDeviceInfoBean {
	private String deviceId;
	private String deviceType;
	private String manufacturer;
	private String model;
	private String fireWare;
	private String maxCamera;
	private String maxAlarm;

	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getFireWare() {
		return fireWare;
	}
	public void setFireWare(String fireWare) {
		this.fireWare = fireWare;
	}
	public String getMaxCamera() {
		return maxCamera;
	}
	public void setMaxCamera(String maxCamera) {
		this.maxCamera = maxCamera;
	}
	public String getMaxAlarm() {
		return maxAlarm;
	}
	public void setMaxAlarm(String maxAlarm) {
		this.maxAlarm = maxAlarm;
	}
	
	
	

}
