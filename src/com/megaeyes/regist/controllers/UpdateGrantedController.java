package com.megaeyes.regist.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.hight.performance.annotation.Param;
import net.hight.performance.utils.json.JsonArray;
import net.hight.performance.utils.json.JsonObject;
import net.hight.performance.utils.json.JsonValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.dao.DomainDao;
import com.megaeyes.regist.dao.GrantedDao;
import com.megaeyes.regist.dao.OrganTreeDao;
import com.megaeyes.regist.domain.Authorization;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.domain.PlatformOrgan;
import com.megaeyes.regist.domain.Role;
import com.megaeyes.regist.domain.User;
import com.megaeyes.regist.other.GrantedType;
import com.megaeyes.regist.utils.ControllerHelper;
import com.megaeyes.regist.utils.Page;
import com.megaeyes.regist.webservice.PlatformUtils;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("updateGranted")
public class UpdateGrantedController {
	@Autowired
	private GrantedDao grantedDao;

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private OrganTreeDao organTreeDao;

	@Autowired
	private DomainDao domainDao;

	/**
	 * 获取要进行授权的用户或角色
	 * 
	 */
	public String getGrantedList(Invocation inv, @Param("page") Page page,
			@Param("name") String name, @Param("organId") String organId) {
		getUserList(inv, page, name, organId);
		return "granted/granted-main";
	}

	public String getUserList(Invocation inv, @Param("page") Page page,
			@Param("name") String name, @Param("organId") String organId) {
		String cmsId = PlatformUtils.getCmsId(inv);
		inv.addModel("userList",
				grantedDao.getUserList(page, name, organId, cmsId, null));
		inv.addModel("callback", "UpdateGranted.getGrantedList");
		return "granted/update/granted-list";
	}

	public String getRoleList(Invocation inv, @Param("page") Page page,
			@Param("name") String name) {
		String cmsId = PlatformUtils.getCmsId(inv);
		inv.addModel("roleList",
				grantedDao.getRoleList(page, name, cmsId, null));
		inv.addModel("callback", "UpdateGranted.getGrantedList");
		return "granted/update/granted-list";
	}

	public String getSelectedRoleList(Invocation inv, @Param("page") Page page,
			@Param("name") String name, @Param("roleIds") String roleIds) {
		String cmsId = PlatformUtils.getCmsId(inv);
		inv.addModel("roleList", grantedDao.getRoleList(page, name, cmsId,
				getIdsFormJson(roleIds)));
		inv.addModel("callback", "getSelectedRoleList");
		return "granted/selected-granted-list";
	}

	private Set<Integer> getIdsFormJson(String jsonIds) {
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

	public String showAuthorizationMain(Invocation inv,
			@Param("page") Page page, @Param("organId") String organId,
			@Param("name") String name) {
		getUserList(inv, page, name, organId);
		getAllRoles(inv);
		return "granted/update/authorization-main";
	}

	public String getAllUsers(Invocation inv) {
		List<User> users = Ar.of(User.class).find("cmsId",
				inv.getModelFromSession("platformCmsId"));
		inv.addModel("allUsers", users);
		return "granted/update/user-list";
	}

	public String getAllRoles(Invocation inv) {
		List<User> users = Ar.of(Role.class).find("cmsId",
				inv.getModelFromSession("platformCmsId"));
		inv.addModel("allRoles", users);
		return "granted/update/role-list";
	}

	public String getResourceForGranted(Invocation inv, @Param("id") String id) {
		if("#".equals(id)){
			Platform platform =  Ar.of(Platform.class).one("cmsId",inv.getModelFromSession("platformCmsId"));
			PlatformOrgan root = Ar.of(PlatformOrgan.class).one("from PlatformOrgan where sourceId=? and sourceType='platform'",platform.getId());
			inv.addModel("parent", root);
		}else{
			PlatformOrgan parent = Ar.of(PlatformOrgan.class).get(Integer.valueOf(id));
			List<PlatformOrgan> children = Ar.of(PlatformOrgan.class).find("from PlatformOrgan where parent.id=?",parent.getId());
			List<Device> devices = Ar.of(Device.class).find("from Device where path=?",parent.getPath());
			inv.addModel("organs", children);
			inv.addModel("devices", devices);
			inv.addModel("children", children);
		}
		
		inv.addModel("organDevicesInfo", organTreeDao.getOrganDevicesInfo());
		inv.addModel("childrenOrganInfo", organTreeDao.getChildrenOrganInfo());

		return "granted/update/organ-tree-for-granted";
	}

	public String getResourceByUserId(Invocation inv,
			@Param("userId") String userId) {
		List<Integer> roleIds = jdbc.queryForList(
				"select role_id from r_user_role where user_id=?",
				new Object[] { userId }, Integer.class);
		List<Authorization> authorizations = domainDao
				.find(Authorization.class,
						"select * from authorization where granted_id=? and granted_type=0",
						userId);
		inv.addModel("roleIds", roleIds);
		inv.addModel("authorizations", authorizations);
		return "granted/update/granted-resource";
	}

	public String getResourceByRoleId(Invocation inv,
			@Param("roleId") String roleId) {
		List<Integer> userIds = jdbc.queryForList(
				"select user_id from r_user_role where role_id=?",
				new Object[] { roleId }, Integer.class);
		List<Authorization> authorizations = domainDao
				.find(Authorization.class,
						"select * from authorization where granted_id=? and granted_type=1",
						roleId);
		inv.addModel("userIds", userIds);
		inv.addModel("authorizations", authorizations);
		return "granted/update/granted-resource";
	}

	public String confirmAuthorization(Invocation inv,
			@Param("grantedId") Integer grantedId,
			@Param("grantedType") Integer grantedType,
			@Param("userRoleList") String userRoleList,
			@Param("organIds") String organIds, @Param("nodes") String nodes) {
		JsonObject parent = JsonObject.readFrom(nodes.substring(1,
				nodes.length() - 1));
		createUserRole(grantedId, grantedType, userRoleList);
		
		Map<String, JsonObject> resultMap = organTreeDao.getResultMap(organIds, nodes);

		clearAuthorization(parent, resultMap, grantedId, grantedType);

		List<Object[]> resultOrganParmas = new ArrayList<Object[]>();
		
		String cmsId = PlatformUtils.getCmsId(inv);
		for (String key : resultMap.keySet()) {
			JsonObject jo = resultMap.get(key);
			String id = jo.get("id").asString();
			JsonObject resourceJO = organTreeDao.getJsonObject(id, key);
			resultOrganParmas.add(new Object[] {cmsId,grantedId, grantedType,
					resourceJO.get("resourceId").asInt(), resourceJO.get("resourceType").asString(),Integer.valueOf(jo.get("platformId").asString()), jo.get("path").asString(),"11111"});
		}
		jdbc.batchUpdate(
				"insert ignore into authorization (cms_id,granted_id,granted_type,resource_id,resource_type,resource_cms_id,resource_path,item) values(?,?,?,?,?,?,?,?)",
				resultOrganParmas);
		return "@success";
	}

	private void clearAuthorization(JsonObject parent,
			Map<String, JsonObject> resultMap, Integer grantedId,
			Integer grantedType) {
		Set<Integer> organIds = new HashSet<Integer>();
		Set<Integer> deviceIds = new HashSet<Integer>();
		ControllerHelper.setResourceIds(parent, organIds, deviceIds);
		List<Object[]> organParams = new ArrayList<Object[]>();
		for (Integer id : organIds) {
			organParams.add(new Object[] { grantedId, grantedType, id });
		}
		List<Object[]> deviceParams = new ArrayList<Object[]>();
		for (Integer id : deviceIds) {
			deviceParams.add(new Object[] { grantedId, grantedType, id });
		}

		List<Object[]> resultOrganParmas = new ArrayList<Object[]>();
		for (String key : resultMap.keySet()) {
			JsonObject jo = resultMap.get(key);
			if (jo.get("id").asString().indexOf("device__") == -1) {
				resultOrganParmas.add(new Object[] { grantedId, grantedType,
						jo.get("path").asString() + "%" });
			}
		}

		jdbc.batchUpdate(
				"delete from authorization where granted_id=?  and granted_type=? and resource_id=? and resource_type='organ'",
				organParams);
		jdbc.batchUpdate(
				"delete from authorization where granted_id=? and granted_type=? and resource_id=? and resource_type='device'",
				deviceParams);
		jdbc.batchUpdate(
				"delete from authorization where granted_id=? and granted_type=? and resource_path like ? ",
				resultOrganParmas);
	}

	private void createUserRole(Integer grantedId, Integer grantedType,
			String userRoleList) {
		Set<Integer> ids = grantedDao.getIdsFormJson(userRoleList);
		if (grantedType == GrantedType.USER.ordinal()) {
			jdbc.update("delete from r_user_role where user_id=?", grantedId);
			List<Object[]> params = new ArrayList<Object[]>();
			for (Integer roleId : ids) {
				if (roleId == 0) {
					continue;
				}
				params.add(new Object[] { grantedId, roleId });
			}
			jdbc.batchUpdate(
					"insert into r_user_role (user_id,role_id) values(?,?)",
					params);
		} else {
			jdbc.update("delete from r_user_role where role_id=?", grantedId);
			List<Object[]> params = new ArrayList<Object[]>();
			for (Integer userId : ids) {
				if (userId == 0) {
					continue;
				}
				params.add(new Object[] { userId, grantedId });
			}
			jdbc.batchUpdate(
					"insert into r_user_role (user_id,role_id) values(?,?)",
					params);
		}
	}
}
