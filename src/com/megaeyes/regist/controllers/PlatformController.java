package com.megaeyes.regist.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hight.performance.annotation.Param;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.annotation.NotRequire;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.GbPlatformType;
import com.megaeyes.regist.utils.HttpclientUtils;
import com.megaeyes.regist.webservice.PlatformUtils;
import com.megaeyes.regist.webservice.RegistUtil;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("platform")
public class PlatformController {
	/**
	 * 获取本平台以及下级平台
	 * 
	 * @param inv
	 * @param cmsId
	 * @return
	 */
	
	@Autowired
	private RegistUtil registUtil;

	public void setRegistUtil(RegistUtil registUtil) {
		this.registUtil = registUtil;
	}
	
	@Autowired
	private RegisterDao registerDao;

	public void setRegisterDao(RegisterDao registerDao) {
		this.registerDao = registerDao;
	}
	
	@Value("${requireSessionId}")
	private boolean requireSessionId;
	
	@NotRequire
	public String getChildrenPlatforms(Invocation inv,
			@Param("cmsId") String cmsId, @Param("sessionId") String sessionId,@Param("tabId")String tabId) {
		Platform platform = Ar.of(Platform.class).one("cmsId",cmsId);
		if (platform == null) {
			return "@wrong.cms.id";
		}
//		// 验证用户是否已经登录
		if(requireSessionId){
			if (authentication(platform.getServiceUrl(), sessionId)) {
				return "user-not-login";
			}
		}
		if (platform.getChildren().size() == 0) {
			inv.addModel("hasChildren", "no");
		} else if (platform.getChildren().size() > 0) {
			inv.addModel("hasChildren", "yes");
		}
		inv.addModel("shareResource", hasShareResource(cmsId));
		inv.addModel("platform", platform);
		inv.addModelToSession("platformCmsId", cmsId);
		GbPlatform gbPlatform = Ar.of(GbPlatform.class).one("from GbPlatform where type=?",GbPlatformType.CHILD);
		if(gbPlatform!=null){
			inv.addModel("hasGbChildPlatform", "yes");
		}
//		return "home";
		return "index";
	}

	public String hasShareResource(@Param("cmsId")String cmsId) {
		return registUtil.getRS(registerDao).hasShareResource(cmsId);
	}

	private boolean authentication(String platformGradeUrl, String sessionId) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		HttpGet platformGrade = HttpclientUtils.accessPlatformGrade(map,
				"/user/authentication", platformGradeUrl.replaceFirst(
						"http://", ""));
		try {
			HttpResponse response = HttpclientUtils.getResponse(map,
					platformGrade);
			String msg = EntityUtils.toString(response.getEntity());
			if ("success".equals(msg)) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 共享平台主页
	 * 
	 * @param inv
	 * @param cmsId
	 * @return
	 */
	public String shareDeviceToPlatforms(Invocation inv) {
		String cmsId = PlatformUtils.getCmsId(inv);
		inv.getRequest().getSession().setAttribute("shareCmsId", cmsId);
		inv.addModel("ownerOrganTree", getOrgansByCmsId(cmsId));
		return PlatformUtils.getSharePath("navigation");
	}

	/**
	 * 互联平台主页
	 * 
	 * @param inv
	 * @param cmsId
	 * @return
	 */
	public String getDeviceFromPlatforms(Invocation inv) {
		String cmsId = PlatformUtils.getCmsId(inv);
		inv.getRequest().getSession().setAttribute("obtainCmsId", cmsId);
		inv.addModel("ownerOrganTree", getOrgansByCmsId(cmsId));
		return PlatformUtils.getObtainPath("navigation");
	}

	public String forOrganMain(Invocation inv, @Param("cmsId") String cmsId) {
		String platformCmsId = PlatformUtils.getCmsId(inv);
		inv.addModel("ownerOrganTree", getOrgansByCmsId(platformCmsId));
		return "navigate";
	}

	private List<Organ> getOrgansByCmsId(String cmsId) {
		List<Organ> organs = Ar
				.of(Organ.class)
				.find(
						"from Organ where cms_id=? and status!=? and parentOrganId is null",
						cmsId, Status.delete.name());
		return organs;
	}

	public String getOrgansBycmsId(Invocation inv, @Param("cmsId") String cmsId) {
		List<Organ> organs = Ar
				.of(Organ.class)
				.find(
						"from Organ where cms_id=? and status!=? and parentOrganId is null",
						cmsId, Status.delete.name());
		inv.addModel("organTree", organs);
		return "granted-manage";
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

	public String getPlatformTree(Invocation inv, @Param("root") String parentId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Platform.class);
		if (parentId.equals("source") || "all".equals(parentId)) {
			criteria.add(Restrictions.eq("parent.id", PlatformUtils
					.getCmsId(inv)));
		} else {
			criteria.add(Restrictions.eq("parent.id", parentId));
		}
		List<Platform> children = Ar.find(criteria);
		inv.addModel("children", children);
		return "platform-tree-json";
	}
	
	/**
	 * 重新设置organ的path
	 * @return
	 */
	public String resetOrganPath(){
		return registerDao.resetOrgan();
	}
}
