package com.megaeyes.regist.controllers;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hight.performance.annotation.Param;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.bean.PlatformResourceStatisticBean;
import com.megaeyes.regist.domain.IResource;
import com.megaeyes.regist.other.ResourceType;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.Page;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("gbPlatform")
public class GbPlatformController {
	public String getResourcesByCmsId(Invocation inv, @Param("page") Page page,
			@Param("resourceType") String resourceType,
			@Param("name") String name, @Param("cmsId") String cmsId) {
		if (StringUtils.isBlank(resourceType)) {
			resourceType = "VIC";
			inv.addModel("resourceType", resourceType);
		}
		ResourceType type = ResourceType.getInstance(resourceType);
		DetachedCriteria criteria = DetachedCriteria.forClass(type
				.getResourceClass());
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name, MatchMode.START));
		}
		criteria.add(Restrictions.eq("cmsId", cmsId));
		criteria.add(Restrictions.eq("type", type.getDesc()));

		page.setRecordCount(criteria);
		List<IResource> resources = Ar.find(criteria, page.getFirstResult(),
				page.getPageSize());
		inv.addModel("resources", resources);
		if (type.equals(ResourceType.ORGAN)) {
			statisticByOrgan(inv, cmsId);
		} else if (type.equals(ResourceType.DVR)
				|| type.equals(ResourceType.VIS)) {
			statisticByServer(inv, cmsId, resources);
		}
		return "/GB/platform-resource-list";
	}

	private void statisticByServer(Invocation inv, String cmsId,
			List<IResource> resources) {
		PlatformResourceStatisticBean bean;
		Map<String, PlatformResourceStatisticBean> map = new HashMap<String, PlatformResourceStatisticBean>();
		for (IResource resource : resources) {
			List<Object[]> deviceStatistic = Ar
					.sql("select count(*), type,server_id from device where cms_id=? and status!=? and server_id=? group by type,server_id",
							cmsId, Status.delete.name(), resource.getId());
			for (Object[] row : deviceStatistic) {
				String key = (String) row[2];
				if (map.containsKey(key)) {
					bean = map.get(key);
				} else {
					bean = new PlatformResourceStatisticBean();
					map.put(key, bean);
				}
				if (row[1].equals("VIC")) {
					bean.setVics((BigInteger) row[0]);
				} else if (row[1].equals("AIC")) {
					bean.setAics((BigInteger) row[0]);
				} else if (row[1].equals("IPVIC")) {
					bean.setIpvics((BigInteger) row[0]);
				}
			}
		}
		inv.addModel("serverMap", map);

	}

	private void statisticByOrgan(Invocation inv, String cmsId) {
		List<Object[]> deviceStatistic = Ar
				.sql("select count(*), type,concat(owner_id,'_',cms_id) from device where cms_id=? and status!=? group by type,concat(owner_id,'_',cms_id)",
						cmsId, Status.delete.toString());
		PlatformResourceStatisticBean bean;
		Map<String, PlatformResourceStatisticBean> map = new HashMap<String, PlatformResourceStatisticBean>();

		for (Object[] row : deviceStatistic) {
			String key = (String) row[2];
			if (map.containsKey(key)) {
				bean = map.get(key);
			} else {
				bean = new PlatformResourceStatisticBean();
				map.put(key, bean);
			}
			if (row[1].equals("VIC")) {
				bean.setVics((BigInteger) row[0]);
			} else if (row[1].equals("AIC")) {
				bean.setAics((BigInteger) row[0]);
			} else if (row[1].equals("IPVIC")) {
				bean.setIpvics((BigInteger) row[0]);
			}
		}
		inv.addModel("organMap", map);
	}
}
