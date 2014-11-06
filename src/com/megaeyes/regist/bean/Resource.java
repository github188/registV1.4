package com.megaeyes.regist.bean;

import java.io.Serializable;

public class Resource implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String type;
	private String parentOrgan;
	private String cmsId;
	private String permission;
	private String cmsName;
	private String item;
	private String organPath;

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getParentOrgan() {
		return parentOrgan;
	}

	public void setParentOrgan(String parentOrgan) {
		this.parentOrgan = parentOrgan;
	}

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public String getCmsName() {
		return cmsName;
	}

	public void setCmsName(String cmsName) {
		this.cmsName = cmsName;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getOrganPath() {
		return organPath;
	}

	public void setOrganPath(String organPath) {
		this.organPath = organPath;
	}
}
