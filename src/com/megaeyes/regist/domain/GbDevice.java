package com.megaeyes.regist.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"device_id","original_id"}))
public class GbDevice {
	private Integer id;
	private String path;// 起初对应于device的path,但经过重置后，path为重置后实际的path
	private Device device;// 对应的原始机构
	private GbOrgan organ;
	private GbOrgan original;
	

	private Boolean suspend;//映射出来的设备在机构重置界面删除后设置为true,保证内部分级同步时不更新该数据
	
	private Integer deviceId;
	private Integer organId;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}


	@OneToOne
	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	@ManyToOne
	public GbOrgan getOrgan() {
		return organ;
	}

	public void setOrgan(GbOrgan organ) {
		this.organ = organ;
	}
	
	public Boolean getSuspend() {
		return suspend;
	}

	public void setSuspend(Boolean suspend) {
		this.suspend = suspend;
	}

	@Transient
	public Integer getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}

	@Transient
	public Integer getOrganId() {
		return organId;
	}

	public void setOrganId(Integer organId) {
		this.organId = organId;
	}
	
	@ManyToOne
	public GbOrgan getOriginal() {
		return original;
	}

	public void setOriginal(GbOrgan original) {
		this.original = original;
	}

}
