package com.megaeyes.regist.utils;

import java.util.Set;

import net.hight.performance.utils.json.JsonArray;
import net.hight.performance.utils.json.JsonObject;
import net.hight.performance.utils.json.JsonValue;

public class ControllerHelper {
	public static  void setResourceIds(JsonObject item, Set<Integer> gbOrganIds,
			Set<Integer> gbDeviceIds) {
		String id = item.get("id").asString();
		if (id.indexOf("device__") > -1) {
			gbDeviceIds.add(Integer.valueOf(id.split("__")[1]));
		} else {
			gbOrganIds.add(Integer.valueOf(id));
		}
		JsonArray children = item.get("children").asArray();
		for (JsonValue child : children) {
			JsonObject jo = child.asObject();
			setResourceIds(jo, gbOrganIds, gbDeviceIds);
		}
	}
}
