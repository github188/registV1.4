package com.megaeyes.regist.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.megaeyes.regist.bean.IPlatform;
import com.megaeyes.regist.other.Status;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"cmsId"}))
public class Platform implements IGroup,IPlatform{
	private Integer id;
	private String cmsId;
	private String name;
	private String serviceUrl;
	private String password;
	private Status status;
	private List<Platform> children = new ArrayList<Platform>();
	private Platform parent;
	private String parentCmsId;
	private String eventServerIp;
	private Integer eventServerPort;
	private boolean owner;
	private String gbPlatformCmsId;
	private boolean sync;
	private Integer Istatus;

	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@OneToMany(mappedBy = "parent")
	public List<Platform> getChildren() {
		return children;
	}

	public void setChildren(List<Platform> children) {
		this.children = children;
	}

	@ManyToOne
	@JoinColumn(name = "parent_id")
	public Platform getParent() {
		return parent;
	}

	public void setParent(Platform parent) {
		this.parent = parent;
	}

	public String getEventServerIp() {
		return eventServerIp;
	}

	public void setEventServerIp(String eventServerIp) {
		this.eventServerIp = eventServerIp;
	}

	public Integer getEventServerPort() {
		return eventServerPort;
	}

	public void setEventServerPort(Integer eventServerPort) {
		this.eventServerPort = eventServerPort;
	}

	public String getParentCmsId() {
		return parentCmsId;
	}

	public void setParentCmsId(String parentCmsId) {
		this.parentCmsId = parentCmsId;
	}

	public boolean isOwner() {
		return owner;
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}

	public String getGbPlatformCmsId() {
		return gbPlatformCmsId;
	}

	public void setGbPlatformCmsId(String gbPlatformCmsId) {
		this.gbPlatformCmsId = gbPlatformCmsId;
	}

	public boolean isSync() {
		return sync;
	}

	public void setSync(boolean sync) {
		this.sync = sync;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Platform) {
			Platform other = (Platform) obj;
			if (this.getCmsId().equals(other.getCmsId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.getCmsId().hashCode();
	}
	
	@Transient
	@Override
	public Class<? extends IGroup> getMyClass() {
		return this.getClass();
	}
	
	@Transient
	@Override
	public String getStdId() {
		return getGbPlatformCmsId();
	}
	
	@Override
	public void setStdId(String stdId) {
		
	}
	
	@Transient
	public Integer getIstatus() {
		return Istatus;
	}
	public void setIstatus(Integer istatus) {
		Istatus = istatus;
	}
}
