package com.megaeyes.regist.bean;

import com.megaeyes.regist.other.Status;

public interface IPlatform {
	public Integer getId();

	public void setId(Integer id);

	public String getCmsId();

	public void setCmsId(String cmsId);

	public String getName();

	public void setName(String name);

	public String getServiceUrl();

	public void setServiceUrl(String serviceUrl);

	public String getPassword();

	public void setPassword(String password);

	public Status getStatus();

	public void setStatus(Status status);

	public String getEventServerIp();

	public void setEventServerIp(String eventServerIp);

	public Integer getEventServerPort();

	public void setEventServerPort(Integer eventServerPort);

	public String getParentCmsId();

	public void setParentCmsId(String parentCmsId);

	public boolean isOwner();

	public void setOwner(boolean owner);

	public String getGbPlatformCmsId();

	public void setGbPlatformCmsId(String gbPlatformCmsId);

	public boolean isSync();

	public void setSync(boolean sync);
}
