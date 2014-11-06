package com.megaeyes.regist.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.domain.Platform;
import com.megaeyes.utils.Ar;

@Component("homeDao")
public class HomeDao {
	@Autowired
	private JdbcTemplate jdbc;
	
	public static String getOrganPathSQL(String source,String target){
		String sql =  "(au.resource_type='organ' and (o.path like concat(au.resource_path,'/%') or o.path=au.resource_path or au.resource_path regexp concat(o.path,'/')))";
		return replaceSQL(sql, source, target);
	}
	
	public static String getDeviceByOrganPathSQL(String source,String target){
		String sql =  "(au.resource_type='organ' and (o.path like concat(au.resource_path,'/%') or o.path=au.resource_path))";
		return replaceSQL(sql, source, target);
	}
	
	public static String getDevicePathSQL(String source,String target){
		String sql =  "( au.resource_type='device' and (au.resource_path regexp concat(o.path,'/') or o.path=au.resource_path))";
		return replaceSQL(sql, source, target);
	}
	
	public static String replaceSQL(String sql,String source,String target){
		if(StringUtils.isNotBlank(source) ){
			sql =  sql.replaceAll("o\\.", source+"\\.");
		}
		if(StringUtils.isNotBlank(target)){
			sql = sql.replaceAll("au\\.", target+"\\.");
		}
		return sql;
	}
	
	
	
	public static String userSQL = " (select role_id as id,1 as type from r_user_role rur where user_id=? union select ? as id,0 as type) as granted on(au.cms_id=? and au.granted_id=granted.id and au.granted_type=granted.type)";
	
	private static String getOrganSQL = "select distinct(o.id),o.* from platform_organ o "+
			" inner join authorization au on ("+ getOrganPathSQL(null, null) +" or "+ getDevicePathSQL(null, null) +")"+
			" inner join  "+ userSQL;
	
	private static String getOrganSQLByCmsId = "select distinct(o.id),o.* from platform_organ o " +
			" inner join authorization au on(o.platform_id=? and ( " +getOrganPathSQL(null, null) +" or "+ getDevicePathSQL(null, null) +"))"+
			" inner join  "+ userSQL;

	private static String getDeviceSQL = "select distinct(d.id),d.* from device d " +
			" inner join authorization au on("+ getDeviceByOrganPathSQL("d",null) +" or (au.resource_type='device' and au.resource_id=d.id)) " +
			" inner join  "+ userSQL;
	private static String getDeviceSQLByCmsId = "select distinct(d.id),d.* from device d " +
			" inner join authorization au on(d.platform_id=? and ("+ getDeviceByOrganPathSQL("d", null)+" or (au.resource_type='device' and au.resource_id=d.id))) " +
			" inner join  "+ userSQL;
	private static String getPlatformSQL = "select distinct(o.platform_id) from platform_organ o " +
			" inner join authorization au on("+ getOrganPathSQL(null, null) +" or "+ getDevicePathSQL(null, null) +")"+
			" inner join  "+ userSQL;
	
	public List<Map<String,Object>> getOrgansByUser(Integer userId,String userCmsId,String resourceCmsId){
		if(StringUtils.isNotBlank(resourceCmsId)){
			Platform platform = Ar.of(Platform.class).one("cmsId",resourceCmsId);
			return jdbc.queryForList(getOrganSQLByCmsId,platform.getId(),userId,userId,userCmsId);
		}else{
			return jdbc.queryForList(getOrganSQL,userId,userId,userCmsId);
		}
		
	}
	
	public List<Map<String,Object>> getDevicesByUser(Integer userId,String userCmsId,String resourceCmsId){
		if(StringUtils.isNotBlank(resourceCmsId)){
			Platform platform = Ar.of(Platform.class).one("cmsId",resourceCmsId);
			return jdbc.queryForList(getDeviceSQLByCmsId,platform.getId(),userId,userId,userCmsId);
		}else{
			return jdbc.queryForList(getDeviceSQL,userId,userId,userCmsId);
		}
	}
	
	public List<Integer> getPlatformIds(Integer userId, String userCmsId){
		List<Integer> ids = jdbc.queryForList(getPlatformSQL, new Object[]{userId,userId,userCmsId},Integer.class);
		return ids;
	}

	public List<Platform> getOuterPlatforms(Integer userId, String userCmsId) {
		List<Integer> ids = jdbc.queryForList(getPlatformSQL, new Object[]{userId,userId,userCmsId},Integer.class);
		
		List<Platform> platforms = new ArrayList<Platform>();
		if(ids.size()>0){
			Set<Integer> idSet = new HashSet<Integer>(); 
			idSet.addAll(ids);
			DetachedCriteria criteria = DetachedCriteria.forClass(Platform.class);
			criteria.add(Restrictions.in("id", idSet));
			platforms = Ar.find(criteria);
			for(Platform platform : platforms){
				addParentIds(platform,idSet);
			}
			
			DetachedCriteria crit = DetachedCriteria.forClass(Platform.class);
			crit.add(Restrictions.in("id", idSet));
			platforms = Ar.find(crit);
			
		}
		return platforms;
	}

	private void addParentIds(Platform platform, Set<Integer> idSet) {
		if(platform.getParent()!=null && !idSet.contains(platform.getParent().getId())){
			idSet.add(platform.getParent().getId());
			addParentIds(platform.getParent(), idSet);
		}
	}
}
