package com.megaeyes.regist.bean;

import java.sql.Timestamp;

public interface IOrgan {
	public boolean isSync();

	public void setSync(boolean sync);

	public String getStatus();

	public void setStatus(String status);

	public Timestamp getChangeTime();

	public void setChangeTime(Timestamp changeTime);

	public int getNanosecond();

	public void setNanosecond(int nanosecond);

	public String getOrganId();

	public void setOrganId(String organId);

	public String getName();

	public void setName(String name);

	public String getParentOrganId();

	public void setParentOrganId(String parentOrganId);

	public String getParentOrganName();

	public void setParentOrganName(String parentOrganName);

	public String getCmsId();

	public void setCmsId(String cmsId);

	public String getPath();

	public void setPath(String path);

	public String getType();

	public void setType(String type);

	public String getStdId();

	public void setStdId(String stdId);

	public String getBlock();

	public void setBlock(String block);
}
