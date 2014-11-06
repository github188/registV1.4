package com.megaeyes.regist.controllers;

import java.util.List;
import java.util.Set;

import net.hight.performance.annotation.Param;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.Role;
import com.megaeyes.regist.domain.User;
import com.megaeyes.regist.other.GrantedType;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.Page;
import com.megaeyes.regist.webservice.PlatformUtils;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("user")
public class UserController {
	
	public String index(Invocation inv, @Param("page") Page page,
			@Param("organId") String organId, @Param("name") String name) {
		queryUser(inv, page, organId, name);
		String platformCmsId = PlatformUtils.getCmsId(inv);
		List<Organ> organs = Ar
				.of(Organ.class)
				.find(
						"from Organ where cms_id=? and status!=? and parentOrganId is null",
						platformCmsId, Status.delete.name());
		inv.addModel("organTree", organs);
		return "user-main";
	}

	public String queryUser(Invocation inv, @Param("page") Page page,
			@Param("ownerOrganId") String ownerOrganId, @Param("name") String name) {
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		String platformCmsId = (String) inv.getRequest().getSession()
				.getAttribute("platformCmsId");
		criteria.add(Restrictions.eq("cmsId", platformCmsId));
		if (StringUtils.isNotBlank(ownerOrganId) && !ownerOrganId.equals("all")) {
			criteria.add(Restrictions.eq("organId", ownerOrganId));
		}
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("logonName", name,
					MatchMode.ANYWHERE));
		}
		page.setRecordCount(criteria);
		List<User> userList = Ar.find(criteria, page.getFirstResult(), page
				.getPageSize());
		inv.addModel("userList", userList);
		return "user-list";
	}
	
	public String getUserByName(Invocation inv, @Param("name")String name){
		String platformCmsId = (String) inv.getRequest().getSession()
		.getAttribute("platformCmsId");
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		criteria.add(Restrictions.eq("cmsId", platformCmsId));
		criteria.add(Restrictions.like("logonName", name,MatchMode.ANYWHERE));
		List<User> users = Ar.find(criteria,0,20);
		inv.addModel("users",users);
		return "user-options";
	}
}
