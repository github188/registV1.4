package com.megaeyes.regist.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.megaeyes.regist.other.ResourceType;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"platformCmsId","resourceId","resourceType"}))
public class Share {
	private Integer id;
	private String platformCmsId; // 接受资源的平台
	private Integer resourceId; // 可能是机构id,可能是设备id,取决于resourceType
	// 111表示有实时,历史,云台,第一位表示实时,历史,云台,1表示有权限,0表示没有权限
	private String item;
	private ResourceType resourceType;
	private String resourcePath; // 资源所属位置
	private String resourceCmsId; // 资源所属平台编号

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPlatformCmsId() {
		return platformCmsId;
	}

	public void setPlatformCmsId(String platformCmsId) {
		this.platformCmsId = platformCmsId;
	}

	public Integer getResourceId() {
		return resourceId;
	}

	public void setResourceId(Integer resourceId) {
		this.resourceId = resourceId;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public String getResourceCmsId() {
		return resourceCmsId;
	}

	public void setResourceCmsId(String resourceCmsId) {
		this.resourceCmsId = resourceCmsId;
	}

}
