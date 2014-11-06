package com.megaeyes.regist.bean.sendResource;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.mega.jdom.Element;

public class Item {
	private String deviceID;
	private String name;
	private String manufacturer = "";
	private String model = "";
	private String owner = "1";
	private String civilCode;
	private String block="";
	private String address = "";
	private String parental = "1";
	private String parentID="";
	private String safetyWay = "0";
	private String registerWay="1";
	private String certNum;
	private String certifiable;
	private String errCode;
	private String endTime;
	private String secrecy = "0";
	private String IPAddress = "";
	private String port = "4233";
	private String password;
	private String status = "ON";
	private String longitude;
	private String latitude;
	private IEntity parent;
	private String businessGroupID;
	

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		if (StringUtils.isNotBlank(deviceID))
			this.deviceID = deviceID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (StringUtils.isNotBlank(name))
			this.name = name;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		if (StringUtils.isNotBlank(manufacturer))
			this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		if (StringUtils.isNotBlank(model))
			this.model = model;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		if (StringUtils.isNotBlank(owner))
			this.owner = owner;
	}

	public String getCivilCode() {
		return civilCode;
	}

	public void setCivilCode(String civilCode) {
		if (StringUtils.isNotBlank(civilCode))
			this.civilCode = civilCode;
	}

	public String getBlock() {
		return block;
	}

	public void setBlock(String block) {
		if (StringUtils.isNotBlank(block))
			this.block = block;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		if (StringUtils.isNotBlank(address))
			this.address = address;
	}

	public String getParental() {
		return parental;
	}

	public void setParental(String parental) {
		if (StringUtils.isNotBlank(parental))
			this.parental = parental;
	}

	public String getParentID() {
		return parentID;
	}

	public void setParentID(String parentID) {
		if (StringUtils.isNotBlank(parentID))
			this.parentID = parentID;
	}

	public String getSafetyWay() {
		return safetyWay;
	}

	public void setSafetyWay(String safetyWay) {
		if (StringUtils.isNotBlank(safetyWay))
			this.safetyWay = safetyWay;
	}

	public String getRegisterWay() {
		return registerWay;
	}

	public void setRegisterWay(String registerWay) {
		if (StringUtils.isNotBlank(registerWay))
			this.registerWay = registerWay;
	}

	public String getCertNum() {
		return certNum;
	}

	public void setCertNum(String certNum) {
		if (StringUtils.isNotBlank(certNum))
			this.certNum = certNum;
	}

	public String getCertifiable() {
		return certifiable;
	}

	public void setCertifiable(String certifiable) {
		if (StringUtils.isNotBlank(certifiable))
			this.certifiable = certifiable;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		if (StringUtils.isNotBlank(errCode))
			this.errCode = errCode;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		if (StringUtils.isNotBlank(endTime))
			this.endTime = endTime;
	}

	public String getSecrecy() {
		return secrecy;
	}

	public void setSecrecy(String secrecy) {
		if (StringUtils.isNotBlank(secrecy))
			this.secrecy = secrecy;
	}

	public String getIPAddress() {
		return IPAddress;
	}

	public void setIPAddress(String IPAddress) {
		if (StringUtils.isNotBlank(IPAddress))
			this.IPAddress = IPAddress;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		if (StringUtils.isNotBlank(port))
			this.port = port;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if (StringUtils.isNotBlank(password)) {
			this.password = password;
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		if (StringUtils.isNotBlank(status)) {
			this.status = status;
		}
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		if (StringUtils.isNotBlank(longitude)) {
			this.longitude = longitude;
		}
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		if (StringUtils.isNotBlank(latitude)) {
			this.latitude = latitude;
		}
	}

	public IEntity getParent() {
		return parent;
	}

	public void setParent(IEntity parent) {
		this.parent = parent;
	}
	
	public void initItem(Element el){
		Field[] fields = Item.class.getDeclaredFields();
		for(Field field : fields){
			field.setAccessible(true);
			try {
				field.set(this, el.getChildText(WordUtils.capitalize(field.getName())));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getBusinessGroupID() {
		return businessGroupID;
	}

	public void setBusinessGroupID(String businessGroupID) {
		this.businessGroupID = businessGroupID;
	}

	public static void main(String[] args) {
		try {
			System.out.println(Item.class.getDeclaredField("deviceID"));
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
