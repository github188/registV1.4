package com.megaeyes.regist.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.hight.performance.utils.json.JsonArray;
import net.hight.performance.utils.json.JsonObject;
import net.hight.performance.utils.json.JsonValue;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("organTreeDao")
public class OrganTreeDao {
	
	@Autowired
	private JdbcTemplate jdbc;
	
	public Map<String,Long> statisticOrganInfo(){
//		String sql ="select count(*) organs,g2.id as id from gb_organ g1 inner join gb_organ g2 on(g1.path like concat(g2.path,'%'))  group by g2.id";
//		List<Map<String, Object>> list = jdbc.queryForList(sql);
//		Map<String,Long> result = new HashMap<String, Long>();
//		for(Map<String, Object> map:list){
//			result.put(""+map.get("id"), (Long)map.get("organs")-1);
//		}
//		return result;
		String sql ="select count(*)count,substr(path,1,length(path)-length(id)-1) as path from gb_organ where suspend=false group by substr(path,1,length(path)-length(id)-1)";
		List<Map<String, Object>> rows = jdbc.queryForList(sql);
		return groupBy(rows);
	}
	
	public Map<String,Long> statisticOrganDevicesInfo(){
		String sql ="select count(*) as count,path from gb_device where suspend=false group by path";
		List<Map<String, Object>> rows = jdbc.queryForList(sql);
		return groupBy(rows);
	}
	
	private Map<String,Long> groupBy(List<Map<String, Object>> rows){
		Map<String,Long> result = new HashMap<String, Long>();
		long count = 0;
		for(Map<String, Object> row:rows){
			count = (Long) row.get("count");
			String[] ids = (""+row.get("path")).split("/");
			for(String id : ids){
				if(StringUtils.isNotBlank(id)){
					if(result.containsKey(id)){
						result.put(id, result.get(id)+count);
					}else{
						result.put(id, count);
					}
				}
			}
		}
		return result;
	}
	
	public Map<String,Long> getOrganDevicesInfo(){
		String sql ="select count(*) count,path from device  where status!='delete' group by path";
		List<Map<String, Object>> rows = jdbc.queryForList(sql);
		return groupBy(rows);
	}
	public Map<String,Long> getChildrenOrganInfo(){
		String sql ="select count(*)count,substr(path,1,length(path)-length(id)-1) as path from platform_organ  group by substr(path,1,length(path)-length(id)-1)";
		List<Map<String, Object>> rows = jdbc.queryForList(sql);
		return groupBy(rows);
	}
	public Map<Integer,Long> getPlatformOrganInfo(){
		String sql ="select count(*)count,platform_id from organ where status!='delete' group by platform_id";
		List<Map<String, Object>> list = jdbc.queryForList(sql);
		Map<Integer,Long> result = new HashMap<Integer, Long>();
		for(Map<String, Object> row:list){
			result.put((Integer) row.get("platform_id"), (Long)row.get("count"));
		}
		return result;
	}
	
	public Map<String, JsonObject> getResultMap(String organIds,String nodes){
		JsonObject selectedResource = JsonObject.readFrom(organIds.substring(1,
				organIds.length() - 1));
		JsonArray selectedNodes = selectedResource.get("children").asArray();
		Map<String, JsonObject> shareMap = new HashMap<String, JsonObject>();
		Map<String, JsonObject> resultMap = new HashMap<String, JsonObject>();
		Set<String> involidKeys = new HashSet<String>();
		
		for (JsonValue node : selectedNodes) {
			JsonObject shareObject = node.asObject();
			String nodeId = shareObject.get("id").asString();
			if (nodeId.indexOf("device")== -1) {
				shareMap.put(nodeId, shareObject);
			}
			resultMap.put(nodeId, shareObject);
		}
		
		for (JsonValue node : selectedNodes) {
			JsonObject shareObject = node.asObject();
			String[] ids = shareObject.get("path").asString().split("/");
			for (String organId : ids) {
				if (shareMap.containsKey(organId)
						&& !shareMap.get(organId).asObject()
								.get("id").asString()
								.equals(shareObject.get("id").asString())) {
					involidKeys.add(shareObject.get("id").asString());
					break;
				}
			}
		}
		
		for (String key : involidKeys) {
			resultMap.remove(key);
		}
		
		return resultMap;
	}
	
	public JsonObject getJsonObject(String id  ,String key){
		JsonObject object = new JsonObject();
		if(id.indexOf("device__")==-1){
			object.add("resourceId", Integer.valueOf(id));
			object.add("resourceType", "organ");
		}else{
			object.add("resourceId",Integer.valueOf(id.split("__")[1]));
			object.add("resourceType", "device");
		}
		return object;
	}
	
}
