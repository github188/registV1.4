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

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.dao.DomainDao;
import com.megaeyes.regist.dao.GbPushDao;
import com.megaeyes.regist.dao.OrganTreeDao;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.GbDevice;
import com.megaeyes.regist.domain.GbOrgan;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.GbShare;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.utils.ControllerHelper;
import com.megaeyes.regist.utils.GbPlatformType;
import com.megaeyes.regist.utils.Page;
import com.megaeyes.regist.utils.sendResource.GbDomainMapFactory;
import com.megaeyes.regist.webservice.RegistUtil;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("organTree")
public class OrganTreeController {
	@Autowired
	private RegistUtil registUtil;

	public void setRegistUtil(RegistUtil registUtil) {
		this.registUtil = registUtil;
	}

	@Autowired
	private RegisterDao registerDao;

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private OrganTreeDao organTreeDao;

	@Autowired
	private GbPushDao gbPushDao;
	
	@Autowired
	private DomainDao domainDao;
	
	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public String forOrganTree(Invocation inv, @Param("cmsId") String cmsId) {
		setForHome(inv, cmsId);
		inv.addModel("currentTab", "organTree");
		return "static/home";
	}

	public String forGbShare(Invocation inv, @Param("cmsId") String cmsId) {
		setForHome(inv, cmsId);
		inv.addModel("currentTab", "gbShare");
		return "static/home";
	}

	public void setForHome(Invocation inv, @Param("cmsId") String cmsId) {
		Platform platform = Ar.of(Platform.class).get(cmsId);
		if (platform.getChildren().size() == 0) {
			inv.addModel("hasChildren", "no");
		} else if (platform.getChildren().size() > 0) {
			inv.addModel("hasChildren", "yes");
		}
		inv.addModel("shareResource", hasShareResource(cmsId));
		inv.addModel("platform", platform);
		inv.getRequest().getSession().setAttribute("platformCmsId", cmsId);
		GbPlatform gbPlatform = Ar.of(GbPlatform.class).one(
				"from GbPlatform where type=?", GbPlatformType.CHILD);
		if (gbPlatform != null) {
			inv.addModel("hasGbChildPlatform", "yes");
		}

		List<GbPlatform> parents = Ar.of(GbPlatform.class).find(
				"from GbPlatform where type=?", GbPlatformType.PARENT);
		inv.addModel("parents", parents);
	}

	public String hasShareResource(@Param("cmsId") String cmsId) {
		return registUtil.getRS(registerDao).hasShareResource(cmsId);
	}

	public String getOrgans(Invocation inv, @Param("id") String id) {
		getResourceForShare(inv, id);
		return "static/views/gb-organ-tree";
	}

	public String getResourceForShare(Invocation inv, @Param("id") String id) {
		if ("#".equals(id)) {
			List<GbOrgan> organs = Ar.of(GbOrgan.class).find(
					"from GbOrgan where parent is null");
			inv.addModel("organs", organs);
			inv.addModel("organIds", new ArrayList<String>());
			inv.addModel("devices", new ArrayList<String>());
		} else {
			getResourceByOrganId(inv, id);
		}

		inv.addModel("organInfoMap", organTreeDao.statisticOrganInfo());
		inv.addModel("organDevicesInfoMap",
				organTreeDao.statisticOrganDevicesInfo());
		return "static/views/organ-tree-for-share";
	}

	private void getOrgansByParentId(Invocation inv, String parentId) {
		List<GbOrgan> organs = Ar.of(GbOrgan.class).find(
				"from GbOrgan where parent.id=? and suspend=false", Integer.valueOf(parentId));
		inv.addModel("organs", organs);
	}

	private void getResourceByOrganId(Invocation inv, String organId) {
		getOrgansByParentId(inv, organId);
		DetachedCriteria criteria = DetachedCriteria.forClass(GbDevice.class);
		criteria.add(Restrictions.eq("organ.id", Integer.valueOf(organId)));
		criteria.add(Restrictions.eq("suspend", false));
		List<GbDevice> devices = Ar.find(criteria);
		inv.addModel("devices", devices);
	}

	public String resetGbOrganTree(Invocation inv,
			@Param("organDatas") String organDatas,@Param("deleteNodeIds") String deleteNodeIds) {
		JsonObject parent = JsonObject.readFrom(organDatas.substring(1,
				organDatas.length() - 1));
		JsonObject deleteParent = JsonObject.readFrom(deleteNodeIds.substring(1,
				deleteNodeIds.length() - 1));
		setSuspend(deleteParent);
		resetGbOrganTree(parent);
		Ar.flush();
		registerDao.resetGbOrganPath();
		Ar.flush();
		jdbc.execute("update gb_device d inner join gb_organ o on(d.organ_id=o.id) set d.path=o.path");
		jdbc.execute("delete from gb_share");
		GbDomainMapFactory.getInstance().initMap();
		return "@success";
	}

	private void setSuspend(JsonObject deleteParent) {
		JsonArray items = deleteParent.get("children").asArray();
		Set<Integer> organIds = new HashSet<Integer>();
		Set<Integer> deviceIds = new HashSet<Integer>();
		for (JsonValue value : items) {
			JsonObject item = value.asObject();
			String id = item.get("id").asString();
			if(id.indexOf("device")>-1){
				deviceIds.add(Integer.valueOf(id.split("__")[1]));
			}else{
				organIds.add(Integer.valueOf(id));
			}
		}
		
		MapSqlParameterSource paramMap = new MapSqlParameterSource();
		paramMap.addValue("organIds", organIds);
		paramMap.addValue("deviceIds", deviceIds);
		if (organIds.size() > 0) {
			namedParameterJdbcTemplate.update("update gb_organ set suspend=true where id in (:organIds)",paramMap);
		} 
		if (deviceIds.size()>0){
			namedParameterJdbcTemplate.update("update gb_device set suspend=true where id in (:deviceIds)",paramMap);
		}
	}

	private void resetGbOrganTree(JsonObject parent) {
		JsonArray items = parent.get("children").asArray();
		for (JsonValue value : items) {
			JsonObject item = value.asObject();
			if (item.get("type").asString().equals("platform")) {
				// item为平台
				resetGbOrganTree(item);
			} else if(item.get("type").asString().equals("organ")) {
				GbOrgan gbParent = getGbOrgan(parent);
				GbOrgan organ = getGbOrgan(item);
				organ.setParent(gbParent);
				resetGbOrganTree(item);
			}else{
				// item为该设备
				GbOrgan organ = getGbOrgan(parent);
				GbDevice  device = getGbDevice(item);
				device.setOrgan(organ);
			}
		}
	}

	private GbOrgan getGbOrgan(JsonObject item) {
		return Ar.of(GbOrgan.class).get(
				Integer.valueOf(item.get("id").asString()));
	}
	private GbDevice getGbDevice(JsonObject item) {
		return Ar.of(GbDevice.class).get(
				Integer.valueOf(item.get("id").asString().split("__")[1]));
	}

	public String getDevices(Invocation inv, @Param("page") Page page,
			@Param("organId") String organId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(GbDevice.class);
		if (organId.indexOf("platform") > -1) {
			criteria.add(Restrictions.eq("cmsId", organId.split("__")[1]));
		} else {
			GbOrgan organ = Ar.of(GbOrgan.class).get(
					Integer.valueOf(organId.split("__")[1]));
			criteria.add(Restrictions.like("path", organ.getPath(),
					MatchMode.START));
		}
		page.setRecordCount(criteria);
		List<Device> devices = Ar.find(criteria, page.getFirstResult(),
				page.getPageSize());
		inv.addModel("deviceList", devices);
		inv.addModel("callback", "getDevices");
		return "static/views/organ-device-list";
	}

	public String confirmGbShare(Invocation inv,
			@Param("gbPlatformId") Integer gbPlatformId,
			@Param("organIds") String organIds, @Param("nodes") String nodes) {
		JsonObject parent = JsonObject.readFrom(nodes.substring(1,
				nodes.length() - 1));

		Map<String, JsonObject> resultMap = organTreeDao.getResultMap(organIds,
				nodes);
		clearGbShare(parent, resultMap, gbPlatformId);

		List<Object[]> resultOrganParmas = new ArrayList<Object[]>();
		for (String key : resultMap.keySet()) {
			JsonObject jo = resultMap.get(key);
			String id = jo.get("id").asString();
			JsonObject resourceJO = organTreeDao.getJsonObject(id, key);

			resultOrganParmas.add(new Object[] { gbPlatformId,
					resourceJO.get("resourceId").asInt(),
					resourceJO.get("resourceType").asString(),
					jo.get("path").asString() });
		}
		jdbc.batchUpdate(
				"insert ignore into gb_share (platform_id,resource_id,resource_type,resource_path) values(?,?,?,?)",
				resultOrganParmas);
		return "@success";
	}

	public String showResourcesForPlatform(Invocation inv,
			@Param("platformId") Integer platformId) {
		DetachedCriteria criteria = DetachedCriteria.forClass(GbShare.class);
		criteria.add(Restrictions.eq("platformId", platformId));
		List<GbShare> shares = Ar.find(criteria);
		inv.addModel("shares", shares);
		inv.addModel("deviceCount", gbPushDao.getDeviceCounts(platformId));
		inv.addModel("organCount", gbPushDao.getOrganCounts(platformId));
		return "static/views/show-resources-for-platform";
	}

	private void clearGbShare(JsonObject parent,
			Map<String, JsonObject> resultMap, Integer platformId) {
		Set<Integer> gbOrganIds = new HashSet<Integer>();
		Set<Integer> gbDeviceIds = new HashSet<Integer>();
		ControllerHelper.setResourceIds(parent, gbOrganIds, gbDeviceIds);
		List<Object[]> organParams = new ArrayList<Object[]>();
		for (Integer id : gbOrganIds) {
			organParams.add(new Object[] { platformId, id });
		}
		List<Object[]> deviceParams = new ArrayList<Object[]>();
		for (Integer id : gbDeviceIds) {
			deviceParams.add(new Object[] { platformId, id });
		}

		List<Object[]> resultOrganParmas = new ArrayList<Object[]>();
		for (String key : resultMap.keySet()) {
			JsonObject jo = resultMap.get(key);
			if (jo.get("id").asString().indexOf("organ__") > -1) {
				resultOrganParmas.add(new Object[] { platformId,
						jo.get("path").asString() + "%" });
			}
		}

		jdbc.batchUpdate(
				"delete from gb_share where platform_id=? and resource_id=? and resource_type='organ'",
				organParams);
		jdbc.batchUpdate(
				"delete from gb_share where platform_id=? and resource_id=? and resource_type='device'",
				deviceParams);
		jdbc.batchUpdate(
				"delete from gb_share where platform_id=? and resource_path like ? ",
				resultOrganParmas);
	}

	public String searchResource(Invocation inv, @Param("value") String value) {
		DetachedCriteria organCrit = DetachedCriteria.forClass(Organ.class);
		organCrit.add(Restrictions.like("name", value, MatchMode.START));
		organCrit.setProjection(Projections.id());
		List<String> organIds = Ar.find(organCrit);
		organIds.add("no data");

		DetachedCriteria platformCrit = DetachedCriteria
				.forClass(Platform.class);
		platformCrit.add(Restrictions.like("name", value, MatchMode.START));
		platformCrit.setProjection(Projections.id());
		List<String> platformIds = Ar.find(platformCrit);
		platformIds.add("no data");

		DetachedCriteria criteria = DetachedCriteria.forClass(GbOrgan.class);
		criteria.add(Restrictions.or(Restrictions.and(
				Restrictions.eq("sourceType", "organ"),
				Restrictions.in("sourceId", organIds)), Restrictions.and(
				Restrictions.eq("sourceType", "platform"),
				Restrictions.in("sourceId", platformIds))));
		List<GbOrgan> organs = Ar.find(criteria);
		Set<String> pathList = new HashSet<String>();
		for (GbOrgan organ : organs) {
			StringBuilder sb = new StringBuilder();
			setOrganPath(sb, organ.getParent());
			pathList.add(sb.toString());
		}

		DetachedCriteria crit = DetachedCriteria.forClass(GbDevice.class);
		crit.createAlias("device", "device");
		crit.add(Restrictions.like("device.name", value, MatchMode.START));
		List<GbDevice> devices = Ar.find(crit);
		for (GbDevice device : devices) {
			StringBuilder sb = new StringBuilder();
			setOrganPath(sb, device.getOrgan());
			pathList.add(sb.toString());
		}
		inv.addModel("pathList", pathList);
		return "static/views/search-node-result";
	}

	public String organReset(Invocation inv) {
		return "static/views/reset-organ-tree";
	}

	public String toGbShare(Invocation inv, @Param("cmsId") String cmsId) {
		List<GbPlatform> parents = Ar.of(GbPlatform.class).find(
				"from GbPlatform where type=?", GbPlatformType.PARENT);
		inv.addModel("parents", parents);
		return "static/views/gbshare";
	}

	public String getPlatformTreeForGB(Invocation inv) {
		Platform platform = Ar.of(Platform.class).one("cmsId",
				(String) inv.getModelFromSession("platformCmsId"));
		List<String> gbChildren = jdbc.queryForList(
				"select cms_id from gb_platform where type=1", String.class);
		inv.addModel("platform", platform);
		inv.addModel("gbChildren", gbChildren);
		return "platform-tree";
	}

	private void setOrganPath(StringBuilder sb, GbOrgan organ) {
		if (organ != null) {
			sb.insert(0, "/organ__" + organ.getId());
			setOrganPath(sb, organ.getParent());
		}
	}

	public String test(){
		GbDevice device = domainDao.one(GbDevice.class);
		return "@"+device.getDeviceId();
	}
	
	public String validOrganStdId(Invocation inv,@Param("stdId")String stdId){
		Organ organ = Ar.of(Organ.class).one("from Organ where stdId=? and id!=?",stdId.split("__")[1],Integer.valueOf(stdId.split("__")[0]));
		if (organ == null) {
			return "@true";
		} else {
			return "@false";
		}
	}
	public String editOrgan(Invocation inv,@Param("stdId")String stdId,@Param("id")Integer organId){
		Organ organ = Ar.of(Organ.class).get(organId);
		organ.setStdId(stdId);
		Ar.save(organ);
		Ar.flush();
		GbDomainMapFactory.getInstance().initOrganMap();
		return "@success";
	}
}
