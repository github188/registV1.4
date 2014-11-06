package com.megaeyes.regist.bean;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;

import com.mega.jdom.Document;
import com.mega.jdom.Element;
import com.mega.jdom.input.SAXBuilder;
import com.megaeyes.regist.exception.SubscribeException;

public class Subscribed {
	private String SN;
	private String cmdType;
	private String deviceID;
	private String reuslt;
	
	private Subscribed(){
	}
	public static Subscribed getInstance(String message) {
		Subscribed subscribe = new Subscribed();
		Reader in = null;
		try {
			in = new BufferedReader(new StringReader(message));
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(in);
			Element root = doc.getRootElement();
			subscribe.setCmdType(root.getChildText("CmdType"));
			subscribe.setDeviceID(root.getChildText("DeviceID"));
			subscribe.setSN(root.getChildText("SN"));
			return subscribe;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SubscribeException("message is wrong");
		}
	}
	

	public String getSN() {
		return SN;
	}

	public void setSN(String sN) {
		SN = sN;
	}

	public String getCmdType() {
		return cmdType;
	}

	public void setCmdType(String cmdType) {
		this.cmdType = cmdType;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getReuslt() {
		return reuslt;
	}

	public void setReuslt(String reuslt) {
		this.reuslt = reuslt;
	}
}
