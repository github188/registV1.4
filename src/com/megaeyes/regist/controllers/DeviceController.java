package com.megaeyes.regist.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hight.performance.annotation.Param;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.OrganServer;
import com.megaeyes.regist.utils.GbPlatformType;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("device")
public class DeviceController {
	public String firstResource(Invocation inv, @Param("cmsId") String cmsId,
			@Param("resourceType") String resourceType,
			@Param("name") String name, @Param("vicIds") String vicIds,
			@Param("ipvicIds") String ipvicIds, @Param("aicIds") String aicIds,
			@Param("organIds") String organIds) {
		return "first-resource";
	}
	
	public String createOrganServer(Invocation inv){
		Map<String,OrganServer> map = new HashMap<String,OrganServer>();
		List<Device> devices = Ar.of(Device.class).find();
		for(Device dbDevice : devices){
			StringBuilder key = new StringBuilder();
			key.append(dbDevice.getOwnerId()).append(dbDevice.getServerId());
			if(!map.containsKey(key.toString())){
				OrganServer organServer = new OrganServer();
				organServer.setOrganId(dbDevice.getOwnerId()+"_"+dbDevice.getCmsId());
				organServer.setServerId(dbDevice.getServerId());
				organServer.setPath(dbDevice.getPath());
				organServer.setCmsId(dbDevice.getCmsId());
				map.put(key.toString(), organServer);
			}
		}
		for(OrganServer organServer : map.values()){
			OrganServer tempOrganServer = Ar.of(OrganServer.class).one("from OrganServer where organId=? and serverId=?",organServer.getOrganId(),organServer.getServerId());
			if(tempOrganServer == null){
				Ar.save(organServer);
			}
		}
		return "@success";
	}
	
	public String initResourceStatus(){
		DetachedCriteria criteria = DetachedCriteria.forClass(GbPlatform.class);
		criteria.add(Restrictions.eq("type", GbPlatformType.PARENT));
		List<GbPlatform> gbPlatforms = Ar.find(criteria);
		
		initOrganStatus(gbPlatforms);
		initServerStatus(gbPlatforms);
		initDeviceStatus(gbPlatforms);
		return "@OK";
	}
	
	public String initDeviceStatus(List<GbPlatform> gbPlatforms){
		if(gbPlatforms == null){
			DetachedCriteria criteria = DetachedCriteria.forClass(GbPlatform.class);
			criteria.add(Restrictions.eq("type", GbPlatformType.PARENT));
			gbPlatforms = Ar.find(criteria);
		}
		Ar.exesql("delete from device_status");
		for(GbPlatform platform : gbPlatforms){
			Ar.exesql("insert into device_status (status,online_notify,base_notify,online,change_time,nanosecond,online_change_time,online_nanosecond,platform_id,device_id,name,path,location) select " +
					"0,true,true,d.online,d.change_time,d.nanosecond,d.change_time,d.nanosecond,?,d.id,d.name,d.path,d.location from device d", platform.getId());
		}
		return "@OK";
	}
	public String initServerStatus(List<GbPlatform> gbPlatforms){
		if(gbPlatforms == null){
			DetachedCriteria criteria = DetachedCriteria.forClass(GbPlatform.class);
			criteria.add(Restrictions.eq("type", GbPlatformType.PARENT));
			gbPlatforms = Ar.find(criteria);
		}
		Ar.exesql("delete from server_status");
		for(GbPlatform platform : gbPlatforms){
			Ar.exesql("insert into server_status (status,online_notify,base_notify,online,change_time,nanosecond,online_change_time,online_nanosecond,platform_id,server_id,name,location) select " +
					"0,true,true,d.online,d.change_time,d.nanosecond,d.change_time,d.nanosecond,?,d.id,d.name,d.location from device_server d", platform.getId());
		}
		return "@OK";
	}
	
	public String initOrganStatus(List<GbPlatform> gbPlatforms){
		if(gbPlatforms == null){
			DetachedCriteria criteria = DetachedCriteria.forClass(GbPlatform.class);
			criteria.add(Restrictions.eq("type", GbPlatformType.PARENT));
			gbPlatforms = Ar.find(criteria);
		}
		Ar.exesql("delete from organ_status");
		for(GbPlatform platform : gbPlatforms){
			Ar.exesql("insert into organ_status (status,base_notify,change_time,nanosecond,platform_id,organ_id,name,path,block) select " +
					"0,true,o.change_time,o.nanosecond,?,o.id,o.name,o.path,o.block from organ o", platform.getId());
		}
		return "@OK";
	}
}
