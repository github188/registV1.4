package com.megaeyes.regist.bean;

import java.io.Serializable;

public class RoleBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6581352587496976932L;
	private String id;
	private String name;
	private String cmsId;
	private String note;
	
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCmsId() {
		return cmsId;
	}
	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}
}
