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
 * 
 * @author molc
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "sourceId",
		"sourceType" }))
public class PlatformOrgan {
	private Integer id; // 对应于organ的id
	private String path;
	private Integer sourceId;// 对应的原始机构id,或platform的id
	private String sourceType;// 值分别有platform,organ
	private String name;// 对应的平台名称，或机构名称
	private PlatformOrgan parent;
	private Integer platformId;// 对应的platform表主键，sourceType为platform时,platformId与sourceId相等
	private String organId;//platform表的cms_id,organ表里的organ_id主要用于客户端获取设备列表，无需去相应的原始表取值
	private List<PlatformOrgan> children = new ArrayList<PlatformOrgan>();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
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
	public IGroup getOrgan() {
		if (this.getSourceType().equals("platform")) {
			return GbDomainMapFactory.getInstance().getPlatformMap()
					.get(this.getSourceId());
		} else {
			return GbDomainMapFactory.getInstance().getOrganMap()
					.get(this.getSourceId());
		}
	}

	@Transient
	public Platform getPlatform() {
		return GbDomainMapFactory.getInstance().getPlatformMap()
				.get(this.getPlatformId());
	}

	@ManyToOne
	@JoinColumn(name = "parent_id")
	@NotFound(action = NotFoundAction.IGNORE)
	public PlatformOrgan getParent() {
		return parent;
	}

	public void setParent(PlatformOrgan parent) {
		this.parent = parent;
	}

	@OneToMany(mappedBy = "parent")
	public List<PlatformOrgan> getChildren() {
		return children;
	}

	public void setChildren(List<PlatformOrgan> children) {
		this.children = children;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPlatformId() {
		return platformId;
	}

	public void setPlatformId(Integer platformId) {
		this.platformId = platformId;
	}

	public String getOrganId() {
		return organId;
	}

	public void setOrganId(String organId) {
		this.organId = organId;
	}
}
