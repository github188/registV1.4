package com.megaeyes.regist.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.megaeyes.regist.other.Status;
/**
 * 机构与服务器关系表
 * @author molc
 *
 */
@Entity
public class OrganServer {
	private Integer id;
	private String organId;
	private String serverId;
	private String path;
	private String cmsId;
	private Status status;//表示服务器与机构的关系状态便于通知，比如新增了一条记录表示后，只要通知相应的机构表示应机构下新增了一个视频服务器

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOrganId() {
		return organId;
	}

	public void setOrganId(String organId) {
		this.organId = organId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
