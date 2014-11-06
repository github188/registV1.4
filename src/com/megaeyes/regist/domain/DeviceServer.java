package com.megaeyes.regist.domain;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.megaeyes.regist.bean.IDeviceServer;
import com.megaeyes.regist.bean.sendResource.Item;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.webservice.RegistUtil;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"serverId","cmsId"}))
public class DeviceServer implements ResourceStatus, IDeviceServer, IResource {
	private Integer id; // id
	private String serverId;
	private String status; // 状态 add,update,delete
	private boolean sync = false; // 是否已经同步
	private Timestamp changeTime; // 更新时间
	private int nanosecond; // 纳秒

	private String name;
	private String type;
	private String IP;
	private String StreamSupport;
	private String location;
	private String cmsId;
	private Integer organId;
	private String manufacturer;
	private String model;
	private String stdId;
	private String naming;
	private boolean childrenStatus;
	private String online;
	private boolean innerDevice = true;

	private Set<ServerStatus> serverStatus = new HashSet<ServerStatus>();
	private String organStdId;

	private VisModel visModel;


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

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public String getStreamSupport() {
		return StreamSupport;
	}

	public void setStreamSupport(String streamSupport) {
		StreamSupport = streamSupport;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public Integer getOrganId() {
		return organId;
	}

	public void setOrganId(Integer organId) {
		this.organId = organId;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getStdId() {
		return stdId;
	}

	public void setStdId(String stdId) {
		this.stdId = stdId;
	}

	public boolean isChildrenStatus() {
		return childrenStatus;
	}

	public void setChildrenStatus(boolean childrenStatus) {
		this.childrenStatus = childrenStatus;
	}

	public String getNaming() {
		return naming;
	}

	public void setNaming(String naming) {
		this.naming = naming;
	}

	@Transient
	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DeviceServer)) {
			return false;
		}
		DeviceServer other = (DeviceServer) obj;
		if (this.getServerId().equals(other.getServerId())) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.getServerId().hashCode();
	}

	@Transient
	public Set<ServerStatus> getServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(Set<ServerStatus> serverStatus) {
		this.serverStatus = serverStatus;
	}

	public String getOnline() {
		return online;
	}

	public void setOnline(String online) {
		this.online = online;
	}

	@Transient
	@Override
	public ResourceStatus getResource(Item item, GbPlatform platform,
			RegisterDao registerDao) {
		return registerDao.getDeviceServer(item, platform);
	}

	@Transient
	@Override
	public ResourceStatus getInstance() {
		return new DeviceServer();
	}

	@Transient
	@Override
	public void updateProperties(Item item, GbPlatform platform,
			RegisterDao registerDao, ResourceStatus rs) {
		DeviceServer deviceServer = (DeviceServer) rs;
		deviceServer.setIP(item.getIPAddress());
		deviceServer.setLocation(item.getAddress());
		deviceServer.setName(item.getName());
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		deviceServer.setChangeTime(timestamp);
		deviceServer.setNanosecond(timestamp.getNanos() / 1000);
		deviceServer.setSync(false);
	}

	@Transient
	public String getOrganStdId() {
		return organStdId;
	}

	public void setOrganStdId(String organStdId) {
		this.organStdId = organStdId;
	}

	@Transient
	@Override
	public void parentRegist(RegistUtil registUtil, Integer id) {
		registUtil.getRS().parentRegistByServerId(id);

	}

	public void setVisModel(VisModel visModel) {
		this.visModel = visModel;
	}

	@Transient
	public VisModel getVisModel() {
		return visModel;
	}

	@Override
	public void setResourceStatus(ResourceStatus rs, RegisterDao registerDao) {
	}

	@Override
	public void resetOrganPath(ResourceStatus rs,RegisterDao registerDao) {
		// TODO Auto-generated method stub

	}

	public boolean isInnerDevice() {
		return innerDevice;
	}

	public void setInnerDevice(boolean innerDevice) {
		this.innerDevice = innerDevice;
	}
	
	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
}
