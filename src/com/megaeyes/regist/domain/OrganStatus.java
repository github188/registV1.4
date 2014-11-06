package com.megaeyes.regist.domain;

import java.util.Date;

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
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"organId","platform_id"}))
public class OrganStatus  implements NotifyStatus{
	private Integer id;
	private String status; // 状态 add,update,delete,normal
	private boolean baseNotify;// 基本信息变更通知（增，删，改）
	private Date changeTime; // 基本信息更新时间
	private int nanosecond; // 纳秒
	private GbPlatform platform;
	
	private Integer organId; //gbOrgan的id
	private String name;
	private String block;
	
	private Integer subscribeEventId ;//对应subscribe_event订阅id
	private int baseNotifySign; //成功通知信号
	
	private Integer platformId;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isBaseNotify() {
		return baseNotify;
	}

	public void setBaseNotify(boolean baseNotify) {
		this.baseNotify = baseNotify;
	}

	public Date getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(Date changeTime) {
		this.changeTime = changeTime;
	}

	public int getNanosecond() {
		return nanosecond;
	}

	public void setNanosecond(int nanosecond) {
		this.nanosecond = nanosecond;
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
	
	@Transient
	@Override
	public boolean isOnlineNotify() {
		// TODO Auto-generated method stub
		return false;
	}

	public Integer getOrganId() {
		return organId;
	}

	public void setOrganId(Integer organId) {
		this.organId = organId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBlock() {
		return block;
	}

	public void setBlock(String block) {
		this.block = block;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OrganStatus) {
			OrganStatus other = (OrganStatus) obj;
			if(this.getId().equals(other.getId())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.getId().hashCode();
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
