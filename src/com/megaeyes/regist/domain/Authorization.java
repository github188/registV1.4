package com.megaeyes.regist.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"grantedId","grantedType","resourceId","resourceType"}))
public class Authorization{
	private Integer id;
	private Integer grantedId; //可能是用户id,可能是角色id,取决于grantedType
	private Integer resourceId; //可能是机构id,可能是设备id,取决于resourceType
	//111表示有实时,历史,云台,第一位表示实时,历史,云台,1表示有权限,0表示没有权限
	private String item;
	private Integer grantedType;
	private String resourceType;//device,organ
	private String resourcePath; //资源所属位置
	private Integer resourceCmsId; //资源所属平台编号
	private Integer cmsId;//平台编号

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	
	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public Integer getGrantedId() {
		return grantedId;
	}

	public void setGrantedId(Integer grantedId) {
		this.grantedId = grantedId;
	}

	public Integer getResourceId() {
		return resourceId;
	}

	public void setResourceId(Integer resourceId) {
		this.resourceId = resourceId;
	}

	public Integer getGrantedType() {
		return grantedType;
	}

	public void setGrantedType(Integer grantedType) {
		this.grantedType = grantedType;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public Integer getCmsId() {
		return cmsId;
	}

	public Integer getResourceCmsId() {
		return resourceCmsId;
	}

	public void setResourceCmsId(Integer resourceCmsId) {
		this.resourceCmsId = resourceCmsId;
	}

	public void setCmsId(Integer cmsId) {
		this.cmsId = cmsId;
	}
	
}
