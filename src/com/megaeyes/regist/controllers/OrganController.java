package com.megaeyes.regist.controllers;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.hight.performance.annotation.Param;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.enump.DeviceIdType;
import com.megaeyes.regist.other.ResourceType;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.GbPlatformType;
import com.megaeyes.regist.utils.Page;
import com.megaeyes.regist.webservice.PlatformUtils;
import com.megaeyes.regist.webservice.RegistUtil;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("organ")
public class OrganController {
	@Autowired
	private RegisterDao registerDao;
	
	@Autowired
	private RegistUtil registUtil;

	public String getTree(Invocation inv, @Param("root") String parentId,
			@Param("cmsId") String cmsId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Organ.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		criteria.add(Restrictions.ne("status", Status.delete.name()));
		if (parentId.equals("source") || parentId.equals("all")) {
			criteria.add(Restrictions.isNull("parent.id"));
		} else {
			criteria.add(Restrictions.eq("parent.id", parentId));
		}
		List<Organ> children = Ar.find(criteria);
		inv.addModel("children", children);
		return "tree-json";
	}

	public String getOwnerOrganTree(Invocation inv,
			@Param("root") String parentId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Organ.class);
		criteria.add(Restrictions.eq("cmsId", PlatformUtils.getCmsId(inv)));
		criteria.add(Restrictions.ne("status", Status.delete.name()));
		if (parentId.equals("source") || parentId.equals("all")) {
			criteria.add(Restrictions.isNull("parent.id"));
		} else {
			criteria.add(Restrictions.eq("parent.id", parentId));
		}
		List<Organ> children = Ar.find(criteria);
		inv.addModel("children", children);
		return "owner-tree-json";
	}

	public String getParents(Invocation inv, @Param("organId") String organId) {
		Organ organ = Ar.of(Organ.class).get(organId);
		inv.addModel("organ", organ);
		return "organ-parents";
	}

	public String getOwnerOrganByName(Invocation inv,
			@Param("ownerOrganName") String name) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Organ.class);
		criteria.add(Restrictions.eq("cmsId", PlatformUtils.getCmsId(inv)));
		criteria.add(Restrictions.ne("status", Status.delete.name()));
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		} else {
			criteria.add(Restrictions.isNull("parent"));
		}
		List<Organ> organs = Ar.find(criteria, 0, 30);
		inv.addModel("organs", organs);
		inv.addModel("isOwner", true);
		return "organ-option";
	}

	public String getOrganByName(Invocation inv,
			@Param("organName") String name, @Param("r.cmsId") String cmsId) {
		inv.addModel("organs", registerDao.getOrgansByName(cmsId, name));
		inv.addModel("isOwner", false);
		return "organ-option";
	}

	public String getOrgan(Invocation inv, @Param("organName") String name,
			@Param("r.cmsId") String cmsId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Organ.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		criteria.add(Restrictions.ne("status", Status.delete.name()));
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		}
		List<Organ> organs = Ar.find(criteria, 0, 30);
		inv.addModel("organs", organs);
		inv.addModel("isOwner", false);
		return "organ-xml";
	}

	public String getOrganIdByName(Invocation inv,
			@Param("organName") String name, @Param("r.cmsId") String cmsId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Organ.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		criteria.add(Restrictions.ne("status", Status.delete.name()));
		criteria.add(Restrictions.eq("name", name));
		Organ organ = Ar.one(criteria);
		String id = "no";
		if (organ != null) {
			id = organ.getOrganId();
		}
		return "@" + id;
	}

	public String getOwnerOrganIdByName(Invocation inv,
			@Param("ownerOrganName") String name) {
		return getOrganIdByName(inv, name, PlatformUtils.getCmsId(inv));
	}

	public String getOrganDetailById(Invocation inv,
			@Param("root") String parentId) {
		String[] temp = parentId.split("_");
		Organ organ = Ar.of(Organ.class).get(parentId);
		List<Device> devices = Ar.of(Device.class).find(
				"from Device where cmsId=? and  ownerId=? limit 20", temp[1],
				temp[0]);
		inv.addModel("devices", devices);
		inv.addModel("organ", organ);
		return "organ-detail";
	}

	public void parentRegistOrgan(Invocation inv, @Param("cmsId") String cmsId,
			@Param("beginChangeTime") Timestamp beginChangeTime,
			@Param("beginNanosecond") Integer beginNanosecond,
			@Param("endChangeTime") Timestamp endChangeTime,
			@Param("endNanosecond") Integer endNanosecond) {
		List<Organ> organList = Ar
				.of(Organ.class)
				.find("from Organ where cmsId=? and status=? and (change_time>? or  (change_time=? and nanosecond>=?)) and (change_time<? or (change_time=? and nanosecond<=?))",
						cmsId, Status.delete.name(), beginChangeTime,
						beginChangeTime, beginNanosecond, endChangeTime,
						endChangeTime, endNanosecond);
		Set<Integer> ids = new HashSet<Integer>();
		for (Organ organ : organList) {
			if (!ids.contains(organ.getId())) {
				deleteOrgan(organ, ids);
			}
		}
		setOrganParent();
	}

	public void topRegistOrgan(Invocation inv, @Param("cmsId") String cmsId) {
		List<Organ> organList = Ar.of(Organ.class).find(
				"from Organ where cmsId=? and status=?", cmsId,
				Status.delete.name());
		Set<Integer> ids = new HashSet<Integer>();
		for (Organ organ : organList) {
			if (!ids.contains(organ.getId())) {
				deleteOrgan(organ, ids);
			}
		}
		setOrganParent();
	}

	public void setOrganParent() {
		List<Organ> organs = Ar.of(Organ.class).find(
				"from Organ where status!=?", Status.delete.name());
		for (Organ organ : organs) {
			if (organ.getParentOrganId() != null && organ.getParent() == null) {
				Organ parent = Ar.of(Organ.class).get(organ.getParentOrganId());
				organ.setParent(parent);
				Ar.update(organ);
			}
		}
	}

	private void deleteOrgan(Organ organ, Set<Integer> ids) {
		ids.add(organ.getId());
		List<Organ> children = organ.getChildren();
		for (Organ child : children) {
			deleteOrgan(child, ids);
		}
		Ar.delete(organ);
		Ar.exesql("delete from authorization where resource_cms_id=? "
				+ "and resource_id=? and resource_type=?", organ.getCmsId(),
				organ.getId(), ResourceType.valueOf(organ.getType()).ordinal());
		Ar.exesql("delete from share where resource_cms_id=? "
				+ "and resource_id=? and resource_type=?", organ.getCmsId(),
				organ.getId(), ResourceType.valueOf(organ.getType()).ordinal());
	}

	public String getTargetOrganByName(Invocation inv,
			@Param("targetOrganName") String name,
			@Param("r.targetCmsId") String cmsId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Organ.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		criteria.add(Restrictions.ne("status", Status.delete.name()));
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		} else {
			criteria.add(Restrictions.isNull("parent"));
		}
		List<Organ> organs = Ar.find(criteria, 0, 1000);
		inv.addModel("organs", organs);
		inv.addModel("isOwner", false);
		return "redistribution/organ-option";
	}

	public String getOrganByCmsId(Invocation inv, @Param("page") Page page,
			@Param("cmsId") String cmsId, @Param("name") String name,
			@Param("parentId") String parentId) {
		if(StringUtils.isBlank(cmsId)){
			List<GbPlatform> gbPlatforms = Ar.of(GbPlatform.class).find("from GbPlatform where type=?",GbPlatformType.CHILD);
			inv.addModel("platforms", gbPlatforms);
			cmsId = gbPlatforms.get(0).getCmsId();
			inv.addModel("cmsId", cmsId);
		}
		
		DetachedCriteria criteria = DetachedCriteria.forClass(Organ.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		if(StringUtils.isNotBlank(name)){
			criteria.add(Restrictions.like("name", name,MatchMode.START));
		}
		if(StringUtils.isNotBlank(parentId)){
			criteria.add(Restrictions.eq("parent.id", parentId));
		}
		page.setRecordCount(criteria);
		criteria.addOrder(Order.asc("path"));
		List<Organ> organs = Ar.find(criteria,page.getFirstResult(),page.getPageSize());
		inv.addModel("organs", organs);
		return "organ/organ-main";
	}
	
	public String forCreateOrgan(@Param("parentId")String parentId,@Param("name")String name,@Param("cmsId")String cmsId){
		return "organ/for-create-organ";
	}
	
	public String createOrgan(@Param("organ") Organ organ,@Param("parentId")String parentId){
		Organ dbOrgan = Ar.of(Organ.class).one("cmsId,name",organ.getCmsId(),organ.getName());
		if(dbOrgan!=null){
			return "@organ.name.has.exist";
		}
		GbPlatform gbPlatform = Ar.of(GbPlatform.class).one("cmsId",organ.getCmsId());
		registUtil.getManufacturerUtils(
				gbPlatform.getManufacturer()).getOrganStdId(organ);
		organ.setOrganId(DeviceIdType.getMegaId(organ.getStdId()));
		if(StringUtils.isNotBlank(parentId)){
			Organ parent = Ar.of(Organ.class).get(parentId);
			organ.setParent(parent);
			organ.setPath(parent.getPath()+"/"+organ.getOrganId());
			organ.setParentOrganId(parent.getOrganId());
		}else{
			organ.setPath("/" + organ.getOrganId());
		}
		organ.setStatus(Status.normal.name());
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		organ.setChangeTime(timestamp);
		organ.setSync(false);
		organ.setNanosecond(timestamp.getNanos() / 1000);
		Ar.save(organ);
		return "@success";
	}

	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis());
	}
}
