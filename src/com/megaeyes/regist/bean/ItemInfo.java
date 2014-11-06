package com.megaeyes.regist.bean;

import com.megaeyes.regist.domain.Device;

public class ItemInfo {
	private String subQuery="0";
	
	private String ptzType;
	private String positionType;
	private String roomType;
	private String useType;
	private String supplyLightType;
	private String dircetionType;
	
	public ItemInfo(){
		
	}
	public static ItemInfo getInstance(Device device){
		ItemInfo info = new ItemInfo();
		info.setDircetionType(""+device.getDircetionType().ordinal());
		info.setPtzType(""+device.getPtzType().ordinal());
		info.setPositionType(""+device.getPositionType().ordinal());
		info.setRoomType(""+device.getRoomType().ordinal());
		info.setSupplyLightType(""+device.getSupplyLightType().ordinal());
		info.setUseType(""+device.getUseType().ordinal());
		return info;
	}

	public String getSubQuery() {
		return subQuery;
	}

	public void setSubQuery(String subQuery) {
		this.subQuery = subQuery;
	}

	public String getPtzType() {
		return ptzType;
	}
	public void setPtzType(String ptzType) {
		this.ptzType = ptzType;
	}
	public String getPositionType() {
		return positionType;
	}
	public void setPositionType(String positionType) {
		this.positionType = positionType;
	}
	public String getRoomType() {
		return roomType;
	}
	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}
	public String getUseType() {
		return useType;
	}
	public void setUseType(String useType) {
		this.useType = useType;
	}
	public String getSupplyLightType() {
		return supplyLightType;
	}
	public void setSupplyLightType(String supplyLightType) {
		this.supplyLightType = supplyLightType;
	}
	public String getDircetionType() {
		return dircetionType;
	}
	public void setDircetionType(String dircetionType) {
		this.dircetionType = dircetionType;
	}
}
