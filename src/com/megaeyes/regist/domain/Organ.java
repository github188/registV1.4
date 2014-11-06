package com.megaeyes.regist.domain;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.beans.BeanUtils;

import com.megaeyes.regist.bean.IOrgan;
import com.megaeyes.regist.bean.sendResource.Item;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.webservice.RegistUtil;
import com.megaeyes.utils.Ar;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"organId","cmsId"}))
public class Organ implements Comparable<Organ>,ResourceStatus,Cloneable,IOrgan,IResource,IGroup{
	/**
	 * 
	 */
	
	private Integer id; // id
	private String status; // 状态 add,update,delete
	private boolean sync = false; // 是否已经同步
	private Timestamp changeTime; // 更新时间
	private int nanosecond; // 纳秒
	private String organId;
	private String name;
	private String parentOrganId;
	private String parentOrganName;
	private String cmsId;
	private String path;
	private Organ parent;
	private String type = "ORGAN";
	private List<Organ> children = new ArrayList<Organ>();
	private String stdId;
	
	private String block;
	private Set<OrganStatus> organStatus = new HashSet<OrganStatus>();
	private String parentStdId;
	
	private Platform platform;

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
	
	public String getOrganId() {
		return organId;
	}

	public void setOrganId(String organId) {
		this.organId = organId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentOrganId() {
		return parentOrganId;
	}

	public void setParentOrganId(String parentOrganId) {
		this.parentOrganId = parentOrganId;
	}

	public String getParentOrganName() {
		return parentOrganName;
	}

	public void setParentOrganName(String parentOrganName) {
		this.parentOrganName = parentOrganName;
	}

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@ManyToOne
	@JoinColumn(name="parent_id")
	@NotFound(action = NotFoundAction.IGNORE)
	public Organ getParent() {
		return parent;
	}

	public void setParent(Organ parent) {
		this.parent = parent;
	}

	@OneToMany(mappedBy="parent")
	public List<Organ> getChildren() {
		return children;
	}

	public void setChildren(List<Organ> children) {
		this.children = children;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Organ) {
			Organ other = (Organ) obj;
			if(this.getOrganId().equals(other.getOrganId())&& this.getCmsId().equals(other.getCmsId())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (this.getOrganId()+this.getCmsId()).hashCode();
	}
	
	@Override
	public int compareTo(Organ o) {
		return this.name.compareTo(o.getName());
	}

	public String getStdId() {
		return stdId;
	}

	public void setStdId(String stdId) {
		this.stdId = stdId;
	}
	
	public Organ clone() {
		Organ clone = new Organ();
		BeanUtils.copyProperties(this, clone,new String[]{"children"});
		return clone;
	}

	public String getBlock() {
		return block;
	}

	public void setBlock(String block) {
		this.block = block;
	}

	@Transient
	public Set<OrganStatus> getOrganStatus() {
		return organStatus;
	}

	public void setOrganStatus(Set<OrganStatus> organStatus) {
		this.organStatus = organStatus;
	}
	
	@Transient
	@Override
	public ResourceStatus getResource(Item item, GbPlatform platform,RegisterDao registerDao) {
		return registerDao.getOrgan(item, platform);
	}
	
	@Transient
	@Override
	public ResourceStatus getInstance() {
		return new Organ();
	}
	
	@Transient
	@Override
	public void setOnline(String online) {
		// TODO Auto-generated method stub
	}
	
	
	@Transient
	@Override
	public void updateProperties(Item item, GbPlatform platform,
			RegisterDao registerDao, ResourceStatus rs) {
		Organ organ = (Organ) rs;
		organ.setName(item.getName());
	}

	@Transient
	public String getParentStdId() {
		return parentStdId;
	}

	public void setParentStdId(String parentStdId) {
		this.parentStdId = parentStdId;
	}
	
	public OrganStatus getOrganStatusByGbPlatformId(int id){
		return Ar.of(OrganStatus.class).one("from OrganStatus where organId=? and platform.id=?",this.getId(),id);
	}
	
	@Transient
	@Override
	public void parentRegist(RegistUtil registUtil, Integer id) {
		registUtil.getRS().parentRegistByOrganId(id);
	}
	
	@Override
	public void setResourceStatus(ResourceStatus rs,RegisterDao registerDao) {
	}
	
	@Override
	public void resetOrganPath(ResourceStatus rs,RegisterDao registerDao) {
		Organ organ = (Organ) rs;
		StringBuilder path = new StringBuilder();
		organ.setPath(registerDao.getPath(organ, path));
		Ar.update(organ);
	}
	
	
	@Transient
	@Override
	public Class<? extends IGroup> getMyClass() {
		return this.getClass();
	}
	
	@ManyToOne
	public Platform getPlatform() {
		return platform;
	}

	public void setPlatform(Platform platform) {
		this.platform = platform;
	}
}
