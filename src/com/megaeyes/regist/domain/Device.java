package com.megaeyes.regist.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.springframework.beans.BeanUtils;

import com.megaeyes.regist.bean.IDevice;
import com.megaeyes.regist.bean.sendResource.Item;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.enump.DircetionType;
import com.megaeyes.regist.enump.PTZType;
import com.megaeyes.regist.enump.PositionType;
import com.megaeyes.regist.enump.RoomType;
import com.megaeyes.regist.enump.SupplyLightType;
import com.megaeyes.regist.enump.UseType;
import com.megaeyes.regist.webservice.RegistUtil;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"deviceId","ownerId","cmsId"}))
public class Device implements Serializable, Comparable<Device>, ResourceStatus , Cloneable,IDevice,IResource {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer id; // id
	private String status; // 状态 add,update,delete
	private boolean sync = false; // 是否已经同步
	private Timestamp changeTime; // 更新时间
	private int nanosecond; // 纳秒
	
	private String deviceId;
	private String name;
	private String naming;
	private String location;
	private String path;
	private String type;
	private String cmsId;
	private String ownerId; // 最终所分配的机构,来自path的最后部分
	private String permission; // 设备自身有的功能项,实时,历史,云台,预置位,抓拍
	private String outerPlatforms;
	private long recordCount;
	private boolean allocated;
	private boolean supportScheme;
	private String longitude = "0";// 经度
	private String latitude = "0";// 纬度
	
	private String gpsZ="0";
	

	private String stdId; // 国标Id
							// 6位cmsId+00+01行业+131VIC或132IPVIC或134AIC设备类型+0网络类型+000001设备序号
	private String serverId; // 视频服务器的Id
	private String dispatcherPlatformId; // 所属派出所
	
	//当前二级设备的状态  2012-04-23 zyq
	private String alarmStatu;//报警状态，ONDUTY,OFFDUTY,ALARM
	private String alarmReset; //报警复位标准 true false
	
	
	private PTZType ptzType;
	private PositionType positionType;
	private RoomType roomType;
	private UseType useType;
	private SupplyLightType supplyLightType;
	private DircetionType dircetionType;
	
	private String online;
	
	private int IptzType;
	private int IpositionType;
	private int IroomType;
	private int IuseType;
	private int IsupplyLightType;
	private int IdircetionType;
	private Set<DeviceStatus> deviceStatus = new HashSet<DeviceStatus>();
	
	private String organStdId;
	private DeviceServer deviceServer;
	
	private IGroup gbOrgan;
	
	private Platform platform;
	private Organ organ;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isSync() {
		return sync;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(Timestamp changeTime) {
		this.changeTime = changeTime;
	}

	public int getNanosecond() {
		return nanosecond;
	}

	public void setNanosecond(int nanosecond) {
		this.nanosecond = nanosecond;
	}

	

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNaming() {
		return naming;
	}

	public void setNaming(String naming) {
		this.naming = naming;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getType() {
		return type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public long getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(long recordCount) {
		this.recordCount = recordCount;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getOuterPlatforms() {
		return outerPlatforms;
	}

	public void setOuterPlatforms(String outerPlatforms) {
		this.outerPlatforms = outerPlatforms;
	}

	public boolean isAllocated() {
		return allocated;
	}

	public void setAllocated(boolean allocated) {
		this.allocated = allocated;
	}

	public boolean isSupportScheme() {
		return supportScheme;
	}

	public void setSupportScheme(boolean supportScheme) {
		this.supportScheme = supportScheme;
	}



	@Override
	public int compareTo(Device o) {
		if (this.type.compareTo(o.getType()) == 0) {
			return this.name.compareTo(o.getName());
		} else {
			return o.getType().compareTo(this.type);
		}
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getStdId() {
		return stdId;
	}

	public void setStdId(String stdId) {
		this.stdId = stdId;
	}

	public String getDispatcherPlatformId() {
		return dispatcherPlatformId;
	}

	public void setDispatcherPlatformId(String dispatcherPlatformId) {
		this.dispatcherPlatformId = dispatcherPlatformId;
	}
	
	public String getAlarmStatu() {
		return alarmStatu;
	}

	public void setAlarmStatu(String alarmStatu) {
		this.alarmStatu = alarmStatu;
	}

	public String getAlarmReset() {
		return alarmReset;
	}

	public void setAlarmReset(String alarmReset) {
		this.alarmReset = alarmReset;
	}
	
	public Device clone() {
		Device device = new Device();
		BeanUtils.copyProperties(this, device);
		return device;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Device) {
			Device other = (Device) obj;
			if(this.getDeviceId().equals(other.getDeviceId()) && this.getOwnerId().equals(other.getOwnerId())&& this.getCmsId().equals(other.getCmsId())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (this.getDeviceId()+this.getOwnerId()+this.getCmsId()).hashCode();
	}
	
	
	
	

	public PTZType getPtzType() {
		if(ptzType == null){
			this.ptzType = PTZType.getInstance(this.getIptzType());
		}
		return this.ptzType;
	}

	public void setPtzType(PTZType ptzType) {
		this.ptzType = ptzType;
	}

	public PositionType getPositionType() {
		if(this.positionType == null){
			this.positionType = PositionType.getInstance(this.IpositionType);
		}
		return positionType;
	}

	public void setPositionType(PositionType positionType) {
		this.positionType = positionType;
	}

	public RoomType getRoomType() {
		if(this.roomType == null){
			this.roomType = RoomType.getInstance(this.IroomType);
		}
		return roomType;
	}

	public void setRoomType(RoomType roomType) {
		this.roomType = roomType;
	}

	public UseType getUseType() {
		if(this.useType == null){
			this.useType = UseType.getInstance(this.IuseType);
		}
		return useType;
	}

	public void setUseType(UseType useType) {
		this.useType = useType;
	}

	public SupplyLightType getSupplyLightType() {
		if(this.supplyLightType == null){
			this.supplyLightType = SupplyLightType.getInstance(this.IsupplyLightType);
		}
		return supplyLightType;
	}

	public void setSupplyLightType(SupplyLightType supplyLightType) {
		this.supplyLightType = supplyLightType;
	}

	public DircetionType getDircetionType() {
		if(this.dircetionType == null){
			this.dircetionType = DircetionType.getInstance(this.IdircetionType);
		}
		return dircetionType;
	}

	public void setDircetionType(DircetionType dircetionType) {
		this.dircetionType = dircetionType;
	}
	
	
	
	@Transient
	public int getIptzType() {
		return IptzType;
	}

	public void setIptzType(int iptzType) {
		IptzType = iptzType;
	}

	@Transient
	public int getIpositionType() {
		return IpositionType;
	}

	public void setIpositionType(int ipositionType) {
		IpositionType = ipositionType;
	}

	@Transient
	public int getIroomType() {
		return IroomType;
	}

	public void setIroomType(int iroomType) {
		IroomType = iroomType;
	}

	@Transient
	public int getIuseType() {
		return IuseType;
	}

	public void setIuseType(int iuseType) {
		IuseType = iuseType;
	}

	@Transient
	public int getIsupplyLightType() {
		return IsupplyLightType;
	}

	
	public void setIsupplyLightType(int isupplyLightType) {
		IsupplyLightType = isupplyLightType;
	}

	@Transient
	public int getIdircetionType() {
		return IdircetionType;
	}

	public void setIdircetionType(int idircetionType) {
		IdircetionType = idircetionType;
	}


	@Transient
	public Set<DeviceStatus> getDeviceStatus() {
		return deviceStatus;
	}

	
	public void setDeviceStatus(Set<DeviceStatus> deviceStatus) {
		this.deviceStatus = deviceStatus;
	}

	public String getOnline() {
		return online;
	}

	public void setOnline(String online) {
		this.online = online;
	}
	
	@Transient
	@Override
	public ResourceStatus getResource(Item item, GbPlatform platform,RegisterDao registerDao) {
		return registerDao.getDevice(item, platform);
	}
	
	@Transient
	@Override
	public ResourceStatus getInstance() {
		return new Device();
	}
	
	@Transient
	@Override
	public void updateProperties(Item item, GbPlatform platform,
			RegisterDao registerDao, ResourceStatus rs) {
		Device device = (Device) rs;
		device.setLocation(item.getAddress());
		device.setName(item.getName());
		device.setLatitude(item.getLatitude());
		device.setLongitude(item.getLongitude());
		device.setSync(false);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		device.setChangeTime(timestamp);
		device.setNanosecond(timestamp.getNanos() / 1000);
	}
	
	@Transient
	@Override
	public void parentRegist(RegistUtil registUtil, Integer id) {
		registUtil.getRS().parentRegistByDeviceId(id);
	}

	@Transient
	public String getOrganStdId() {
		return organStdId;
	}

	public void setOrganStdId(String organStdId) {
		this.organStdId = organStdId;
	}

	@Transient
	public DeviceServer getDeviceServer() {
		return deviceServer;
	}

	public void setDeviceServer(DeviceServer deviceServer) {
		this.deviceServer = deviceServer;
	}
	
	@Column(name="gps_z")
	public String getGpsZ() {
		return gpsZ;
	}

	public void setGpsZ(String gpsZ) {
		this.gpsZ = gpsZ;
	}
	
	@Override
	public void setResourceStatus(ResourceStatus rs,
			RegisterDao registerDao) {
		
	}
	
	@Override
	public void resetOrganPath(ResourceStatus rs,RegisterDao registerDao) {
		// TODO Auto-generated method stub
		
	}

	@Transient
	public IGroup getGbOrgan() {
		return gbOrgan;
	}

	public void setGbOrgan(IGroup gbOrgan) {
		this.gbOrgan = gbOrgan;
	}

	@ManyToOne
	public Platform getPlatform() {
		return platform;
	}

	public void setPlatform(Platform platform) {
		this.platform = platform;
	}

	@ManyToOne
	public Organ getOrgan() {
		return organ;
	}

	public void setOrgan(Organ organ) {
		this.organ = organ;
	}
	
}
