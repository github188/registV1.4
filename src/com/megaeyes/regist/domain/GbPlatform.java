package com.megaeyes.regist.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.megaeyes.regist.utils.GbPlatformType;

/**
 * 当前对于我们内部的分级平台来说platform只表示自已平台以及 子孙平台的信息,不包括上级平台的任何信息.然而实现国标后,国标
 * 平台作为我们的上级也在platform当中存在,platform所示 的信息已经超出了原来的语义,国标数据进行共享,获取时比较乱,现增
 * 加GbPlatform表示国标平台,如果国标平台作为我们的下级则同时存在platform当中
 * 
 * @author molc
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"cmsId"}))
public class GbPlatform {
	private Integer id; //主键
	private String cmsId; // 平台ID
	private String name; // 平台名称
	private String password;// 平台密码
	private String sipServer; // sip服务器
	private String childCmsId; // 对应于我们内部所对接的平台ID
	private String sendType; // 设备推送方式 server-terminal-->ST,terminal-->T,organ-terminal-->OT,organ-server-terminal-->OST
	private Integer pageSize = 1; // 每次发送记录数
	private boolean contain; // 对接是否包括childPlatformId平台的所有子孙平台的信息
	private GbPlatformType type; //上级:PARENT,下级:CHILD
	private String manufacturer; //国标平台厂商类型
	private String standardType;//国标，还是地标GB，DB
	private String shareType="PART";//共享方式PART,ALL
	private boolean autoPush;//是否需要自动推送目录
	private boolean hasQuery;//上级是否已经进行了检索,作为上级平台时有效
	private String protocol;//tcp,udp


	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSipServer() {
		return sipServer;
	}

	public void setSipServer(String sipServer) {
		this.sipServer = sipServer;
	}

	public String getSendType() {
		return sendType;
	}

	public void setSendType(String sendType) {
		this.sendType = sendType;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public boolean isContain() {
		return contain;
	}

	public void setContain(boolean contain) {
		this.contain = contain;
	}

	public String getChildCmsId() {
		return childCmsId;
	}

	public void setChildCmsId(String childCmsId) {
		this.childCmsId = childCmsId;
	}

	public GbPlatformType getType() {
		return type;
	}
	public void setType(GbPlatformType type) {
		this.type = type;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getStandardType() {
		return standardType;
	}

	public void setStandardType(String standardType) {
		this.standardType = standardType;
	}

	public String getShareType() {
		return shareType;
	}

	public void setShareType(String shareType) {
		this.shareType = shareType;
	}

	public boolean isAutoPush() {
		return autoPush;
	}

	public void setAutoPush(boolean autoPush) {
		this.autoPush = autoPush;
	}

	public boolean isHasQuery() {
		return hasQuery;
	}

	public void setHasQuery(boolean hasQuery) {
		this.hasQuery = hasQuery;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
}
