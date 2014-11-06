package com.megaeyes.regist.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.hight.performance.utils.json.JsonArray;
import net.hight.performance.utils.json.JsonObject;
import net.hight.performance.utils.json.JsonValue;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.PlatformOrgan;
import com.megaeyes.regist.domain.Role;
import com.megaeyes.regist.domain.User;
import com.megaeyes.regist.other.GrantedType;
import com.megaeyes.regist.other.ResourceType;
import com.megaeyes.regist.utils.Page;
import com.megaeyes.utils.Ar;

@Component("grantedDao")
public class GrantedDao {
	@Autowired
	private JdbcTemplate jdbc;

	public List<PlatformOrgan> getOrganList(Page page, String name,
			Integer platformId, String sourceType, Set<Integer> organIds) {
		DetachedCriteria criteria = DetachedCriteria
				.forClass(PlatformOrgan.class);
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name, MatchMode.START));
		}
		if (platformId != null) {
			PlatformOrgan parent = Ar.of(PlatformOrgan.class).get(platformId);
			criteria.add(Restrictions.like("path", parent.getPath(),MatchMode.START));
		}
		if("root".equals(sourceType)){
			criteria.add(Restrictions.eq("sourceType", "organ"));
			criteria.createAlias("parent", "parent");
			criteria.add(Restrictions.eq("parent.sourceType", "platform"));
		}else{
			criteria.add(Restrictions.eq("sourceType", sourceType));
		}
		
		if (organIds != null) {
			criteria.add(Restrictions.in("id", organIds));
		}
		page.setLastRecordCount(criteria);
		List<PlatformOrgan> organList = Ar.find(criteria, page.getFirstResult(),
				page.getPageSize());
		return organList;
	}

	public List<Role> getRoleList(Page page, String name, String cmsId,
			Set<Integer> ids) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Role.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name, MatchMode.START));
		}
		if (ids != null) {
			criteria.add(Restrictions.in("id", ids));
		}
		page.setLastRecordCount(criteria);
		List<Role> roleList = Ar.find(criteria, page.getFirstResult(),
				page.getPageSize());
		return roleList;
	}

	public List<User> getUserList(Page page, String name, String organId,
			String cmsId, Set<Integer> ids) {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		criteria.add(Restrictions.eq("cmsId", cmsId));
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("logonName", name, MatchMode.START));
		}
		if (StringUtils.isNotBlank(organId)) {
			criteria.add(Restrictions.eq("organId", organId));
		}
		if (ids != null) {
			criteria.add(Restrictions.in("id", ids));
		}
		page.setLastRecordCount(criteria);
		List<User> userList = Ar.find(criteria, page.getFirstResult(),
				page.getPageSize());
		return userList;
	}

	public List<Device> getDeviceList(Page page, String name, Integer platformId,
			String organId, String type, Set<Integer> ids) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Device.class);
		criteria.add(Restrictions.ne("status", "delete"));
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name, MatchMode.START));
		}
		if (platformId!=null) {
			PlatformOrgan parent = Ar.of(PlatformOrgan.class).get(platformId);
			criteria.add(Restrictions.like("path", parent.getPath(),MatchMode.START));
		}
		if (StringUtils.isNotBlank(type)) {
			criteria.add(Restrictions.eq("type", type));
		}
		if (StringUtils.isNotBlank(organId)) {
			Organ organ = Ar.of(Organ.class).get(organId);
			criteria.add(Restrictions.like("path", organ.getPath(),
					MatchMode.START));
		}
		if (ids != null) {
			criteria.add(Restrictions.in("id", ids));
		}
		page.setLastRecordCount(criteria);
		List<Device> deviceList = Ar.find(criteria, page.getFirstResult(),
				page.getPageSize());
		return deviceList;
	}

	public void authorization(String userCmsId, Set<Integer> deviceIdSet,
			Set<Integer> organIdSet, Set<Integer> userIdSet,
			Set<Integer> roleIdSet, String permission) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Device.class);
		criteria.add(Restrictions.in("id", deviceIdSet));
		List<Device> devices = Ar.find(criteria);

		DetachedCriteria crit = DetachedCriteria.forClass(PlatformOrgan.class);
		crit.add(Restrictions.in("id", organIdSet));
		crit.addOrder(Order.asc("path"));
		List<PlatformOrgan> organs = Ar.find(crit);
		Map<String, PlatformOrgan> organMap = new HashMap<String, PlatformOrgan>();
		Map<String, Device> deviceMap = new HashMap<String, Device>();
		Set<Device> afterFilterDevice = new HashSet<Device>();
		for (PlatformOrgan organ : organs) {
			if (!contain(organMap.keySet(), organ.getPath())) {
				organMap.put(organ.getPath(), organ);
			}
		}
		for (Device device : devices) {
			if (!contain(organMap.keySet(), device.getPath())) {
				afterFilterDevice.add(device);
			}
		}

		List<Object[]> resourceParmas = new ArrayList<Object[]>();
		List<Object[]> pathParams = new ArrayList<Object[]>();
		for (Integer userId : userIdSet) {
			if (userId == 0) {
				continue;
			}
			for (String key : organMap.keySet()) {
				PlatformOrgan organ = organMap.get(key);
				resourceParmas.add(new Object[] { userId,
						GrantedType.USER.ordinal(), organ.getId(),
						"organ", permission, userCmsId,
						organ.getPlatformId(), organ.getPath() });
				pathParams.add(new Object[] { userId,
						GrantedType.USER.ordinal(),
						organ.getPlatformId(), organ.getPath() + "%" });
			}
			for (Device device : afterFilterDevice) {
				resourceParmas.add(new Object[] { userId,
						GrantedType.USER.ordinal(), device.getId(),
						"device", permission, userCmsId,
						device.getPlatform().getId(), device.getPath() });
			}
		}

		for (Integer roleId : roleIdSet) {
			if (roleId == 0) {
				continue;
			}
			Role role = Ar.of(Role.class).get(Integer.valueOf(roleId));
			for (String key : organMap.keySet()) {
				PlatformOrgan organ = organMap.get(key);
				resourceParmas.add(new Object[] { role.getId(),
						GrantedType.ROLE.ordinal(), organ.getId(),
						"organ", permission, userCmsId,
						organ.getPlatform().getId(), organ.getPath() });
				pathParams.add(new Object[] { role.getId(),
						GrantedType.ROLE.ordinal(),
						organ.getPlatform().getId(), organ.getPath() + "%" });
			}
			for (String key : deviceMap.keySet()) {
				Device device = deviceMap.get(key);
				resourceParmas.add(new Object[] { role.getId(),
						GrantedType.ROLE.ordinal(), device.getId(),
						"device", permission, userCmsId,
						device.getPlatform().getId(), device.getPath() });
			}
		}
		jdbc.batchUpdate(
				"insert ignore into authorization (granted_id,granted_type,resource_id,resource_type,item,cms_id,resource_cms_id,resource_path) values(?,?,?,?,?,?,?,?)",
				resourceParmas);
		jdbc.execute("delete from authorization where id in(select id from (select au.id  from authorization au  where  exists(select * from authorization other where au.granted_id=other.granted_id and au.granted_type=other.granted_type  and other.resource_type=1 and au.resource_id!=other.resource_id  and au.resource_path like concat(other.resource_path,'%'))) as t)");
	}

	public List<Organ> getOrgansByParentId(Integer parentId) {
		List<Organ> organs = Ar.of(Organ.class).find(
				"from Organ where parent.id=? and status!=?", parentId,
				"delete");
		return organs;
	}

	public List<Device> getDeviceByOrganId(Integer parentId) {
		List<Device> devices = Ar.of(Device.class).find(
				"from Device where organ.id=? and status!=?", parentId,
				"delete");
		return devices;
	}

	public Set<Integer> getIdsFormJson(String jsonIds) {
		JsonObject parent = JsonObject.readFrom(jsonIds.substring(1,
				jsonIds.length() - 1));
		JsonArray children = parent.get("children").asArray();
		Set<Integer> ids = new HashSet<Integer>();
		ids.add(0);
		for (JsonValue child : children) {
			ids.add(Integer.valueOf(child.asObject().get("id").asString()));
		}
		return ids;
	}

	private boolean contain(Set<String> pathSet, String path) {
		for (String key : pathSet) {
			String[] ids = key.split("/");
			String[] currentIds = path.split("/");
			boolean target = true;
			for (int i = 0; i < ids.length; i++) {
				if (!ids[i].equals(currentIds[i])) {
					target = false;
					break;
				}
			}
			if (target) {
				return true;
			}
		}
		return false;
	}
}
