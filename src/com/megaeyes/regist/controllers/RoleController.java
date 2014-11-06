package com.megaeyes.regist.controllers;

import java.util.List;

import net.hight.performance.annotation.Param;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.domain.Role;
import com.megaeyes.regist.domain.User;
import com.megaeyes.regist.other.GrantedType;
import com.megaeyes.regist.utils.Page;
import com.megaeyes.regist.webservice.PlatformUtils;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("role")
public class RoleController {
	public String forRoleMain(Invocation inv, @Param("name") String name) {
		getRoleList(inv, name);
		return "role-main";
	}

	public String getRoleList(Invocation inv, @Param("name") String name) {
		String platformCmsId = (String) inv
				.getModelFromSession("platformCmsId");
		DetachedCriteria criteria = DetachedCriteria.forClass(Role.class);
		criteria.add(Restrictions.eq("cmsId", platformCmsId));
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		}
		List<Role> roles = Ar.find(criteria);
		inv.addModel("roles", roles);
		return "role-list";
	}

	public String forCreateRole(Invocation inv) {
		return "create-role";
	}

	public String createRole(Invocation inv, @Param("role") Role role) {
		String cmsId = PlatformUtils.getCmsId(inv);
		Role temp = Ar.of(Role.class).one("cmsId,name", cmsId, role.getName());
		if (temp != null) {
			return "@name.has.exist";
		}
		Role dbRole = new Role();
		dbRole.setCmsId(cmsId);
		dbRole.setName(role.getName());
		dbRole.setNote(role.getNote());
		Ar.save(dbRole);
		return "@success";
	}

	public String deleteRole(Invocation inv, @Param("id") Integer id) {
		Role role = Ar.of(Role.class).get(id);
		Ar.exesql(
				"delete from authorization where cms_id=? and granted_id=? and granted_type=?",
				role.getCmsId(), role.getId(), GrantedType.ROLE.ordinal());
		Ar.delete(role);
		return "@success";
	}

	public String getUsersForRole(Invocation inv,
			@Param("roleId") String roleId, @Param("name") String name) {
		Role role = Ar.of(Role.class).get(roleId);
		inv.addModel("role", role);
		return "role-user";
	}

	public String getUserByRoleId(Invocation inv, @Param("page") Page page,
			@Param("roleId") String roleId, @Param("name") String name,
			@Param("first") String first,
			@Param("ownerOrganId") String ownerOrganId) {
		Role role = Ar.of(Role.class).get(roleId);
		inv.addModel("role", role);
		DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
		criteria.add(Restrictions.eq("cmsId", PlatformUtils.getCmsId(inv)));
		DetachedCriteria crit = criteria.createCriteria("roles");
		crit.add(Restrictions.idEq(roleId));
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("logonName", name,
					MatchMode.ANYWHERE));
		}
		if (StringUtils.isNotBlank(ownerOrganId) && !"all".equals(ownerOrganId)) {
			criteria.add(Restrictions.eq("organId", ownerOrganId));
		}
		page.setRecordCount(criteria);
		List<User> userList = Ar.find(criteria, page.getFirstResult(),
				page.getPageSize());
		inv.addModel("userHasList", userList);
		if (first.equals("true")) {
			return "role-user";
		}
		return "user-has";
	}
}
