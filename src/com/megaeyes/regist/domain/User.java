package com.megaeyes.regist.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import com.megaeyes.regist.bean.sendResource.Item;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.other.Function;
import com.megaeyes.regist.other.Granted;
import com.megaeyes.regist.other.GrantedType;
import com.megaeyes.regist.webservice.RegistUtil;
import com.megaeyes.utils.Ar;

@Entity
public class User implements Serializable, Granted, ResourceStatus {
	private Integer id; // id
	private String status; // 状态 add,update,delete
	private boolean sync = false; // 是否已经同步
	private Timestamp changeTime; // 更新时间
	private int nanosecond; // 纳秒

	private static final long serialVersionUID = 5948977789052789177L;
	private String cmsId;
	private String organId;
	private String naming;
	private String logonName;
	private String password;
	private String name;
	private String sex;
	private String userId;

	

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

	private Set<Role> roles = new HashSet<Role>();

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public String getNaming() {
		return naming;
	}

	public void setNaming(String naming) {
		this.naming = naming;
	}

	public String getLogonName() {
		return logonName;
	}

	public void setLogonName(String logonName) {
		this.logonName = logonName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToMany(mappedBy = "users")
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getOrganId() {
		return organId;
	}

	public void setOrganId(String organId) {
		this.organId = organId;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			User other = (User) obj;
			if (this.getId().equals(other.getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	@Transient
	@Override
	public String getFunction() {
		return Function.getUsers();
	}

	@Transient
	@Override
	public GrantedType getGrantedType() {
		return GrantedType.USER;
	}

	@Transient
	public Organ getOrgan() {
		if (organ != null && organ.getId().equals(this.organId)) {
			return organ;
		} else {
			organ = Ar.of(Organ.class).one("organId",this.organId);
			return organ;
		}

	}

	public static Organ organ;

	@Transient
	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Transient
	@Override
	public ResourceStatus getResource(Item item, GbPlatform platform,
			RegisterDao registerDao) {
		// TODO Auto-generated method stub
		return null;
	}

	@Transient
	@Override
	public ResourceStatus getInstance() {
		return new User();
	}

	@Override
	public void setOnline(String online) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateProperties(Item item, GbPlatform platform,
			RegisterDao registerDao, ResourceStatus rs) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void parentRegist(RegistUtil registUtil, Integer id) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void setResourceStatus(ResourceStatus rs, RegisterDao registerDao) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void resetOrganPath(ResourceStatus rs,RegisterDao registerDao) {
		// TODO Auto-generated method stub
		
	}
}
