package com.megaeyes.regist.controllers;

import java.util.List;
import java.util.Set;

import net.hight.performance.annotation.Param;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.megaeyes.regist.dao.GrantedDao;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.domain.PlatformOrgan;
import com.megaeyes.regist.utils.Page;
import com.megaeyes.regist.webservice.PlatformUtils;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Transactional("txManager")
@Component("granted")
public class GrantedController {
	@Autowired
	private GrantedDao grantedDao;

	public String index(Invocation inv, @Param("page") Page page,
			@Param("organId") String organId, @Param("name") String name) {
		return "user-main";
	}

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
		inv.addModel("callback", "getUserList");
		return "granted/granted-list";
	}

	public String getSelectedUserList(Invocation inv, @Param("page") Page page,
			@Param("name") String name, @Param("organId") String organId,
			@Param("userIds") String userIds) {
		String cmsId = PlatformUtils.getCmsId(inv);
		inv.addModel("userList", grantedDao.getUserList(page, name, organId,
				cmsId, grantedDao.getIdsFormJson(userIds)));
		inv.addModel("callback", "getSelectedUserList");
		return "granted/selected-granted-list";
	}

	public String getRoleList(Invocation inv, @Param("page") Page page,
			@Param("name") String name) {
		String cmsId = PlatformUtils.getCmsId(inv);
		inv.addModel("roleList",
				grantedDao.getRoleList(page, name, cmsId, null));
		inv.addModel("callback", "getRoleList");
		return "granted/granted-list";
	}

	public String getSelectedRoleList(Invocation inv, @Param("page") Page page,
			@Param("name") String name, @Param("roleIds") String roleIds) {
		String cmsId = PlatformUtils.getCmsId(inv);
		inv.addModel("roleList", grantedDao.getRoleList(page, name, cmsId,
				grantedDao.getIdsFormJson(roleIds)));
		inv.addModel("callback", "getSelectedRoleList");
		return "granted/selected-granted-list";
	}

	public String getOrganList(Invocation inv, @Param("page") Page page,
			@Param("name") String name, @Param("platformId") Integer platformId,
			@Param("sourceType") String sourceType) {
		inv.addModel("organList",
				grantedDao.getOrganList(page, name, platformId, sourceType, null));
		inv.addModel("callback", "getOrganList");
		return "granted/resource-list";
	}

	public String getDeviceList(Invocation inv, @Param("page") Page page,
			@Param("name") String name, @Param("platformId") Integer platformId,
			@Param("organId") String organId, @Param("type") String type) {
		inv.addModel("deviceList", grantedDao.getDeviceList(page, name, platformId,
				organId, type, null));
		inv.addModel("callback", "getDeviceList");
		return "granted/resource-list";
	}

	public String confirmAuthorization(Invocation inv,
			@Param("page") Page page, @Param("organIds") String jsonOrganId) {
		getSelectedOrganList(inv, page, null, null, "organ", jsonOrganId);
		return "granted/show-selected";
	}

	public String getSelectedOrganList(Invocation inv,
			@Param("page") Page page, @Param("name") String name,
			@Param("platformId") Integer platformId, @Param("sourceType") String sourceType,
			@Param("organIds") String jsonOrganId) {
		inv.addModel("organList", grantedDao.getOrganList(page, name, platformId,
				sourceType, grantedDao.getIdsFormJson(jsonOrganId)));
		inv.addModel("sourceType", sourceType);
		inv.addModel("callback", "getSelectedOrgan");
		return "granted/selected-resource-list";
	}

	public String getSelectedDeviceList(Invocation inv,
			@Param("page") Page page, @Param("name") String name,
			@Param("platformId") Integer platformId, @Param("organId") String organId,
			@Param("type") String type, @Param("deviceIds") String jsonDeviceId) {
		inv.addModel("deviceList", grantedDao.getDeviceList(page, name, platformId,
				organId, type, grantedDao.getIdsFormJson(jsonDeviceId)));
		inv.addModel("callback", "getSelectedDeviceList");
		return "granted/selected-resource-list";
	}
	
	public String getUserOrganTree(Invocation inv,@Param("id")String organId){
		if("#".equals(organId)){
			Platform platform =  Ar.of(Platform.class).one("cmsId",inv.getModelFromSession("platformCmsId"));
			List<Organ> root = Ar.of(Organ.class).find("from Organ where parent is null and platform.id=?",platform.getId());
			inv.addModel("platform", platform);
			inv.addModel("children", root);
		}else{
			List<Organ> children = Ar.of(Organ.class).find("from Organ where parent.id=?",Integer.valueOf(organId));
			inv.addModel("children", children);
		}
		return "granted/user-organ-tree";
	}
	
	public String getResourePlatformTree(Invocation inv,@Param("id")String id) {
		if("#".equals(id)){
			Platform platform =  Ar.of(Platform.class).one("cmsId",inv.getModelFromSession("platformCmsId"));
			PlatformOrgan root = Ar.of(PlatformOrgan.class).one("from PlatformOrgan where sourceId=? and sourceType='platform'",platform.getId());
			inv.addModel("platform", root);
		}else{
			List<PlatformOrgan> children = Ar.of(PlatformOrgan.class).find("from PlatformOrgan where parent.id=?",Integer.valueOf(id));
			inv.addModel("children", children);
		}
		return "granted/resource-platform-tree";
	}

	

	public String authorization(Invocation inv,
			@Param("deviceIds") String jsonDeviceId,
			@Param("organIds") String jsonOrganId,
			@Param("userIds") String jsonUserId,
			@Param("roleIds") String jsonRoleId,
			@Param("permission") String permission) {
		try{
			Set<Integer> deviceIdSet = grantedDao.getIdsFormJson(jsonDeviceId);
			Set<Integer> organIdSet = grantedDao.getIdsFormJson(jsonOrganId);
			Set<Integer> userIdSet = grantedDao.getIdsFormJson(jsonUserId);
			Set<Integer> roleIdSet = grantedDao.getIdsFormJson(jsonRoleId);
			String cmsId = PlatformUtils.getCmsId(inv);
			grantedDao.authorization(cmsId,deviceIdSet,organIdSet,userIdSet,roleIdSet, permission);
		}catch(Exception e){
			e.printStackTrace();
			return "@error";
		}
		return "@success";
	}

}
