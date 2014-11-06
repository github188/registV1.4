package com.megaeyes.regist.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SimplePlatform implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String cmsId;
	private String cmsName;
	private SimplePlatform parent;
	private List<SimplePlatform> children = new ArrayList<SimplePlatform>();

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public String getCmsName() {
		return cmsName;
	}

	public void setCmsName(String cmsName) {
		this.cmsName = cmsName;
	}

	public SimplePlatform getParent() {
		return parent;
	}

	public void setParent(SimplePlatform parent) {
		this.parent = parent;
	}

	public List<SimplePlatform> getChildren() {
		return children;
	}

	public void setChildren(List<SimplePlatform> children) {
		this.children = children;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SimplePlatform) {
			SimplePlatform other = (SimplePlatform) obj;
			if(this.getCmsId().equals(other.getCmsId())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.getCmsId().hashCode();
	}

}
