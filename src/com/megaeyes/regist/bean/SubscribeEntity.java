package com.megaeyes.regist.bean;

import com.mega.jdom.Document;
import com.mega.jdom.Element;

public class SubscribeEntity {
	private String SN;
	private String cmdType="Catalog";
	private String deviceID;
	
	public SubscribeEntity(String deviceID,String SN){
		this.deviceID = deviceID;
		this.SN = SN;
	}
	
	public Document getQueryDoc() {
		Element root = new Element("Query");
		Element cmdTypeEl = new Element("CmdType");
		cmdTypeEl.setText(this.getCmdType());
		root.addContent(cmdTypeEl);

		Element SNEl = new Element("SN");
		SNEl.setText(this.getSN());
		root.addContent(SNEl);

		Element DeviceIDEl = new Element("DeviceID");
		DeviceIDEl.setText(this.getDeviceID());
		root.addContent(DeviceIDEl);

		Document doc = new Document(root);
		return doc;
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
}
