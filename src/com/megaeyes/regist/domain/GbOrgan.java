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

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.megaeyes.regist.utils.sendResource.GbDomainMapFactory;

/**
 * 机构结构重置，以方便达到符合国标要求的结构
 * @author molc
 *
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"sourceId","sourceType"}))
public class GbOrgan {
	private Integer id; //对应于organ的id
	private String path;//起初对应于organ的path,但经过重置后，path为重置后实际的path
	private Integer sourceId;//对应的原始机构id,或platform的id
	private String sourceType;//值分别有platform,organ
	private GbOrgan parent;
	private List<GbOrgan> children = new ArrayList<GbOrgan>();
	private Boolean suspend;//映射出来的设备在机构重置界面删除后设置为true,保证内部分级同步时不更新该数据
	
	private Integer parentId;

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

	@ManyToOne
	@JoinColumn(name="parent_id")
	@NotFound(action = NotFoundAction.IGNORE)
	public GbOrgan getParent() {
		return parent;
	}

	public void setParent(GbOrgan parent) {
		this.parent = parent;
	}

	@OneToMany(mappedBy="parent")
	public List<GbOrgan> getChildren() {
		return children;
	}

	public void setChildren(List<GbOrgan> children) {
		this.children = children;
	}

	public Integer getSourceId() {
		return sourceId;
	}

	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	
	@Transient
	public IGroup getOrgan(){
		if(this.getSourceType().equals("platform")){
			return GbDomainMapFactory.getInstance().getPlatformMap().get(this.getSourceId());
		}else{
			return GbDomainMapFactory.getInstance().getOrganMap().get(this.getSourceId());
		}
	}

	public Boolean getSuspend() {
		return suspend;
	}

	public void setSuspend(Boolean suspend) {
		this.suspend = suspend;
	}

	@Transient
	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
}
