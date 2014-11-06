package com.megaeyes.regist.bean;

import java.util.HashMap;
import java.util.Map;

/*
 * <?xml version="1.0"?>
<Response> 
	<CmdType>DeviceStatus</CmdType>
	<SN>248</SN> 
	<DeviceID>34020000001130000001</DeviceID> 
	<Result>OK</Result>
	<Online>ONLINE</Online> 
	<Status>OK</Status> 
	<Encode>ON</Encode> 
	<Record>OFF</Record> 
	<DeviceTime>2010-11-11T19:46:17</DeviceTime> 
	<Alarmstatus Num=2> 
		<Item>
			<DeviceID>34020000001340000001</DeviceID> 
			<DutyStatus>OFFDUTY</DutyStatus>
		 </Item>
		 <Item> 
			<DeviceID>34020000001340000002</DeviceID> 
			<DutyStatus>OFFDUTY</DutyStatus> 
		</Item>
	</Alarmstatus>
</Response>
 */
public class GbDeviceStatusBean {
	private String deviceId;
	private String onLine;
	private String status;
	private String encode;
	private String record;
	private String deviceTime;
	
	//报警相关的状态
	public Map<String,String> alarmMap = new HashMap<String, String>();

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getOnLine() {
		return onLine;
	}

	public void setOnLine(String onLine) {
		this.onLine = onLine;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEncode() {
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	public String getDeviceTime() {
		return deviceTime;
	}

	public void setDeviceTime(String deviceTime) {
		this.deviceTime = deviceTime;
	}

	public Map<String, String> getAlarmMap() {
		return alarmMap;
	}

	public void setAlarmMap(Map<String, String> alarmMap) {
		this.alarmMap = alarmMap;
	}

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
	}
	
	
	
	
}
