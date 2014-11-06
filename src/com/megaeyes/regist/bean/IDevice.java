package com.megaeyes.regist.bean;

import java.sql.Timestamp;

import com.megaeyes.regist.enump.DircetionType;
import com.megaeyes.regist.enump.PTZType;
import com.megaeyes.regist.enump.PositionType;
import com.megaeyes.regist.enump.RoomType;
import com.megaeyes.regist.enump.SupplyLightType;
import com.megaeyes.regist.enump.UseType;

public interface IDevice {
	public String getStatus();

	public void setStatus(String status);

	public boolean isSync();

	public void setSync(boolean sync);

	public Timestamp getChangeTime();

	public void setChangeTime(Timestamp changeTime);

	public int getNanosecond();

	public void setNanosecond(int nanosecond);

	public String getDeviceId();

	public void setDeviceId(String deviceId);

	public String getName();

	public void setName(String name);

	public String getNaming();

	public void setNaming(String naming);

	public String getLocation();

	public void setLocation(String location);

	public String getPath();

	public void setPath(String path);

	public String getType();

	public void setType(String type);

	public String getCmsId();

	public void setCmsId(String cmsId);

	public String getOwnerId();

	public void setOwnerId(String ownerId);

	public String getPermission();

	public void setPermission(String permission);

	public String getOuterPlatforms();

	public void setOuterPlatforms(String outerPlatforms);

	public long getRecordCount();

	public void setRecordCount(long recordCount);

	public boolean isAllocated();

	public void setAllocated(boolean allocated);

	public boolean isSupportScheme();

	public void setSupportScheme(boolean supportScheme);

	public String getLongitude();

	public void setLongitude(String longitude);

	public String getLatitude();

	public void setLatitude(String latitude);

	public String getStdId();

	public void setStdId(String stdId);

	public String getServerId();

	public void setServerId(String serverId);

	public String getDispatcherPlatformId();

	public void setDispatcherPlatformId(String dispatcherPlatformId);

	public String getAlarmStatu();

	public void setAlarmStatu(String alarmStatu);

	public String getAlarmReset();

	public void setAlarmReset(String alarmReset);

	public PTZType getPtzType();

	public void setPtzType(PTZType ptzType);

	public PositionType getPositionType();

	public void setPositionType(PositionType positionType);

	public RoomType getRoomType();

	public void setRoomType(RoomType roomType);

	public UseType getUseType();

	public void setUseType(UseType useType);

	public SupplyLightType getSupplyLightType();

	public void setSupplyLightType(SupplyLightType supplyLightType);

	public DircetionType getDircetionType();

	public void setDircetionType(DircetionType dircetionType);
	
	public String getOnline();

	public void setOnline(String online);

	public int getIptzType();

	public void setIptzType(int iptzType);

	public int getIpositionType();

	public void setIpositionType(int ipositionType);

	public int getIroomType();

	public void setIroomType(int iroomType);

	public int getIuseType();

	public void setIuseType(int iuseType);

	public int getIsupplyLightType();

	public void setIsupplyLightType(int isupplyLightType);

	public int getIdircetionType();

	public void setIdircetionType(int iDircetionType);
	
	public String getGpsZ();

	public void setGpsZ(String gpsZ);
}
