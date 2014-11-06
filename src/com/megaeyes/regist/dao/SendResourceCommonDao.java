package com.megaeyes.regist.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.utils.Ar;

@Component("SendResourceCommonDao")
public  class SendResourceCommonDao {

	/**
	 * 获取对接的platform信息
	 * 
	 * @param gbPlatform
	 * @return
	 */
	public Set<String> getCmsIdsByGbPlatform(GbPlatform gbPlatform,
			Platform platform, RequestEntity entity) {
		Set<String> cmsIds = new HashSet<String>();
		boolean contain = gbPlatform.isContain();
		cmsIds.add(platform.getCmsId());
		if (contain) {
			getChildrenCmsIds(platform.getCmsId(), cmsIds, entity);
		}
		return cmsIds;
	}

	/**
	 * 递归获取子孙平台cmsId
	 * 
	 * @param cmsId
	 * @param cmsIds
	 */
	public void getChildrenCmsIds(String cmsId, Collection<String> cmsIds,
			RequestEntity entity) {
		List<String> ids = Ar.of(Platform.class).find(
				"select id from Platform where parent.id=?", cmsId);
		cmsIds.addAll(ids);
		for (String id : ids) {
			getChildrenCmsIds(id, cmsIds, entity);
		}
	}

}
