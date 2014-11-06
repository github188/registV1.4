package com.megaeyes.regist.bean;

import java.sql.Timestamp;

public interface IDeviceServer {
	public String getStatus();

	public void setStatus(String status);

	public boolean isSync();

	public void setSync(boolean sync);

	public Timestamp getChangeTime();

	public void setChangeTime(Timestamp changeTime);

	public int getNanosecond();

	public void setNanosecond(int nanosecond);

	public String getName();

	public void setName(String name);

	public String getType();

	public void setType(String type);

	public String getIP();

	public void setIP(String iP);

	public String getStreamSupport();

	public void setStreamSupport(String streamSupport);

	public String getLocation();

	public void setLocation(String location);

	public String getCmsId();

	public void setCmsId(String cmsId);

	public Integer getOrganId();

	public void setOrganId(Integer organId);

	public String getManufacturer();

	public void setManufacturer(String manufacturer);

	public String getModel();

	public void setModel(String model);

	public String getStdId();

	public void setStdId(String stdId);

	public String getNaming();

	public void setNaming(String naming);

	public boolean isChildrenStatus();

	public void setChildrenStatus(boolean childrenStatus);

	public String getOnline();

	public void setOnline(String online);
	
	public boolean isInnerDevice();

	public void setInnerDevice(boolean innerDevice);
	
	public String getServerId();
	public void setServerId(String serverId);
}
