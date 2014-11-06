package com.megaeyes.regist.domain;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.megaeyes.utils.Ar;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"deviceId","platform_id"}))
public class DeviceStatus implements NotifyStatus{
	private Integer id; // 主键
	private String status; // 状态 add,update,delete
	private boolean onlineNotify; // 上下级是否已经进行了消息通知
	private boolean baseNotify;// 基本信息变更通知（增，删，改）
	private String online; // 设备是否在线
	private Timestamp changeTime; // 基本信息更新时间
	private int nanosecond; // 纳秒
	private Timestamp onlineChangeTime; // 状态更新时间
	private int onlineNanosecond; // 纳秒
	private GbPlatform platform;
	
	private Integer deviceId;// 相应的设备gbDevice的id
	private String name;//设备当前名称
	private String location;//设备安装位置
	
	private Integer subscribeEventId ;//对应subscribe_event订阅id
	private int baseNotifySign = 0; //成功通知信号
	private int onlineNotifySign =0;//在线成功通知信息
	

	private Integer Istatus;
	private Integer platformId;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	

	public boolean isOnlineNotify() {
		return onlineNotify;
	}

	public void setOnlineNotify(boolean onlineNotify) {
		this.onlineNotify = onlineNotify;
	}

	public boolean isBaseNotify() {
		return baseNotify;
	}

	public void setBaseNotify(boolean baseNotify) {
		this.baseNotify = baseNotify;
	}

	public String getOnline() {
		return online;
	}

	public void setOnline(String online) {
		this.online = online;
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

	public Timestamp getOnlineChangeTime() {
		return onlineChangeTime;
	}

	public void setOnlineChangeTime(Timestamp onlineChangeTime) {
		this.onlineChangeTime = onlineChangeTime;
	}

	public int getOnlineNanosecond() {
		return onlineNanosecond;
	}

	public void setOnlineNanosecond(int onlineNanosecond) {
		this.onlineNanosecond = onlineNanosecond;
	}

	@ManyToOne
	public GbPlatform getPlatform() {
		if(platform == null && platformId!=null){
			return  Ar.of(GbPlatform.class).get(platformId);
		}
		return platform;
	}

	public void setPlatform(GbPlatform platform) {
		this.platform = platform;
	}

	public Integer getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	
	public Integer getSubscribeEventId() {
		return subscribeEventId;
	}

	public void setSubscribeEventId(Integer subscribeEventId) {
		this.subscribeEventId = subscribeEventId;
	}

	public int getBaseNotifySign() {
		return baseNotifySign;
	}

	public void setBaseNotifySign(int baseNotifySign) {
		this.baseNotifySign = baseNotifySign;
	}

	public int getOnlineNotifySign() {
		return onlineNotifySign;
	}

	public void setOnlineNotifySign(int onlineNotifySign) {
		this.onlineNotifySign = onlineNotifySign;
	}

	@Transient
	public Integer getIstatus() {
		return Istatus;
	}

	public void setIstatus(Integer istatus) {
		Istatus = istatus;
	}

	@Transient
	public Integer getPlatformId() {
		return platformId;
	}

	public void setPlatformId(Integer platformId) {
		this.platformId = platformId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
