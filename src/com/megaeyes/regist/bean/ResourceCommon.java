package com.megaeyes.regist.bean;

import com.megaeyes.regist.other.ResourceType;

public interface ResourceCommon {
	public Integer getId();
	public void setId(Integer id);
	public String getItem();
	public void setItem(String item) ;
	public String getResourceId();
	public void setResourceId(String resourceId);
	public ResourceType getResourceType() ;
	public void setResourceType(ResourceType resourceType) ;
	public String getResourcePath();
	public void setResourcePath(String resourcePath) ;
	public String getResourceCmsId();
	public void setResourceCmsId(String resourceCmsId);

}
