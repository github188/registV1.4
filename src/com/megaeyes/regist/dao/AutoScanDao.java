package com.megaeyes.regist.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.enump.DeviceIdType;
import com.megaeyes.regist.utils.sendResource.GbDomainMapFactory;
import com.megaeyes.utils.Ar;

@Component("autoScanDao")
public class AutoScanDao {
	@Value("${is_city_regist}")
	private boolean hasParent;
	
	@Value("${owner_platform_gbId}")
	private String ownerPlatformGbId;
	
	@Autowired
	private JdbcTemplate jdbc;
	
	@Autowired
	private RegisterDao registerDao;
	
	
	public String rectify() {
		jdbc.execute("update organ set path=null");
		jdbc.execute("update organ set parent_organ_id=null where id in(select * from (select child.id from organ child where child.parent_organ_id is not null and not exists(select * from organ parent where parent.organ_id=child.parent_organ_id)) as t)");
		jdbc.execute("update organ child inner join organ parent on(child.parent_organ_id=parent.organ_id and child.cms_id=parent.cms_id) set child.parent_id=parent.id");
		jdbc.execute("update organ set path=concat('/',id) where parent_id is null");
		jdbc.execute("update organ o inner join platform p on(o.cms_id=p.cms_id) set o.platform_id=p.id");
		resetOrganPath();

		jdbc.update("update platform set gb_platform_cms_id=? where owner is true",ownerPlatformGbId);
		jdbc.execute("update platform set gb_platform_cms_id = cms_id where gb_platform_cms_id is null");
		
		jdbc.execute("insert ignore into platform_organ (source_id,source_type,name,platform_id,organ_id) select p.id,'platform',p.name,p.id,p.cms_id from platform p on duplicate key update name=p.name");
		jdbc.execute("insert ignore into platform_organ (source_id,source_type,name,platform_id,organ_id) select o.id,'organ',o.name,o.platform_id ,o.organ_id from organ o on duplicate key update name=o.name");
		jdbc.execute("update platform_organ gbo inner join platform p on(gbo.source_id=p.id and gbo.source_type='platform' and p.parent_id is not null) inner join platform parent on(p.parent_id=parent.id) inner join platform_organ gb_parent on(parent.id=gb_parent.source_id and gb_parent.source_type='platform') set gbo.parent_id=gb_parent.id");
		jdbc.execute("update platform_organ gbo inner join organ myorgan on(gbo.source_id=myorgan.id and gbo.source_type='organ' and myorgan.parent_id is null) inner join platform_organ gb_parent on(gb_parent.source_type='platform') inner join platform p on(p.cms_id=myorgan.cms_id and p.id=gb_parent.source_id and gb_parent.source_type='platform') set gbo.parent_id=gb_parent.id");
		jdbc.execute("update platform_organ gbo inner join organ myorgan on(gbo.source_id=myorgan.id and gbo.source_type='organ' and myorgan.parent_id is not null) inner join organ parent on(myorgan.parent_id=parent.id) inner join platform_organ gb_parent on(parent.id=gb_parent.source_id and gb_parent.source_type='organ') set gbo.parent_id=gb_parent.id");
		registerDao.resetPlatformOrganPath();
		
		jdbc.execute("update device d inner join organ o on(o.organ_id=d.owner_id and o.cms_id=d.cms_id) inner join platform_organ po on(po.source_id=o.id and po.source_type='organ') set d.path=po.path,d.organ_id=o.id,d.platform_id=o.platform_id");
		jdbc.execute("update device_server set online='OFF' where online is null");
		jdbc.execute("update device set online='OFF' where online is null");
		
		jdbc.execute("insert ignore into gb_organ (source_id,source_type,suspend) select id,'platform',false from platform");
		jdbc.execute("insert ignore into gb_organ (source_id,source_type,suspend) select id,'organ',false from organ ");
		jdbc.execute("update gb_organ gbo inner join platform p on(gbo.source_id=p.id and gbo.source_type='platform' and p.parent_id is not null) inner join platform parent on(p.parent_id=parent.id) inner join gb_organ gb_parent on(parent.id=gb_parent.source_id and gb_parent.source_type='platform') set gbo.parent_id=gb_parent.id");
		jdbc.execute("update gb_organ gbo inner join organ myorgan on(gbo.source_id=myorgan.id and gbo.path is null and gbo.source_type='organ' and myorgan.parent_id is null) inner join gb_organ gb_parent on(gb_parent.source_type='platform') inner join platform p on(p.cms_id=myorgan.cms_id and p.id=gb_parent.source_id and gb_parent.source_type='platform') set gbo.parent_id=gb_parent.id");
		jdbc.execute("update gb_organ gbo inner join organ myorgan on(gbo.source_id=myorgan.id and gbo.path is null and gbo.source_type='organ' and myorgan.parent_id is not null) inner join organ parent on(myorgan.parent_id=parent.id) inner join gb_organ gb_parent on(parent.id=gb_parent.source_id and gb_parent.source_type='organ') set gbo.parent_id=gb_parent.id");
		registerDao.resetGbOrganPath();
		jdbc.execute("insert ignore into gb_device (path,device_id,organ_id,original_id) select gbo.path,d.id,gbo.id,gbo.id from device d inner join organ o on(d.cms_id=o.cms_id and d.owner_id=o.organ_id) inner join gb_organ gbo on(gbo.source_type='organ' and gbo.source_id=o.id)");
		
		if (!hasParent) {
			jdbc.execute("update organ set sync=true");
			jdbc.execute("update device_server set sync=true");
			jdbc.execute("update device set sync=true");
			jdbc.execute("update platform set sync=true");
		}
		return "@success";
	}
	
	public  String rectifyByCmsId(String cmsId) {
		resetOrganByCmsId(cmsId);
		resetPlatformOrgan(cmsId);
		resetDeviceByCmsId(cmsId);
		createGbOrganByCmsId(cmsId);
		createGbDeviceByCmsId(cmsId);
		resetOrganStatus();
		resetDeviceStatus();
		if (!hasParent) {
			jdbc.update("update organ set sync=true where cms_id=?",new Object[]{cmsId});
			jdbc.update("update device_server set sync=true where cms_id=?",new Object[]{cmsId});
			jdbc.update("update device set sync=true  where cms_id=?",new Object[]{cmsId});
			jdbc.execute("update platform set sync=true");
		}
		GbDomainMapFactory.getInstance().initMap();
		return "@success";
	}
	
	public  void resetPlatformOrgan(String cmsId){
		
		jdbc.execute("update platform set gb_platform_cms_id = id where gb_platform_cms_id is null");
		
		jdbc.update("insert ignore into platform_organ (source_id,source_type,name,platform_id,organ_id) select p.id,'platform',p.name,p.id,p.cms_id from platform p where p.cms_id=? on duplicate key update name=p.name",cmsId);
		jdbc.update("insert ignore into platform_organ (source_id,source_type,name,platform_id,organ_id) select o.id,'organ',o.name,o.platform_id ,o.organ_id from organ o where o.cms_id=? on duplicate key update name=o.name",cmsId);
		jdbc.update("update platform_organ gbo inner join platform p on(gbo.source_id=p.id  and p.cms_id=? and gbo.source_type='platform' and p.parent_id is not null) inner join platform parent on(p.parent_id=parent.id) inner join platform_organ gb_parent on(parent.id=gb_parent.source_id and gb_parent.source_type='platform') set gbo.parent_id=gb_parent.id",cmsId);
		jdbc.update("update platform_organ gbo inner join organ myorgan on(gbo.source_id=myorgan.id  and myorgan.cms_id=? and gbo.source_type='organ' and myorgan.parent_id is null) inner join platform_organ gb_parent on(gb_parent.source_type='platform') inner join platform p on(p.cms_id=myorgan.cms_id and p.id=gb_parent.source_id and gb_parent.source_type='platform') set gbo.parent_id=gb_parent.id",cmsId);
		jdbc.update("update platform_organ gbo inner join organ myorgan on(gbo.source_id=myorgan.id  and myorgan.cms_id=? and gbo.source_type='organ' and myorgan.parent_id is not null) inner join organ parent on(myorgan.parent_id=parent.id) inner join platform_organ gb_parent on(parent.id=gb_parent.source_id and gb_parent.source_type='organ') set gbo.parent_id=gb_parent.id",cmsId);
		registerDao.resetPlatformOrganPath();
	}
	
	
	
	public  void resetDeviceByCmsId(String cmsId){
		DeviceServer virtualServer = Ar.of(DeviceServer.class).one("serverId,cmsId",DeviceIdType.Mega.sample(), cmsId);
		jdbc.update("update device d inner join organ o on(o.organ_id=d.owner_id and d.cms_id=? and o.cms_id=d.cms_id) inner join platform_organ po on(po.source_id=o.id and po.source_type='organ') set d.path=po.path,d.organ_id=o.id,d.platform_id=o.platform_id",cmsId);
		jdbc.update("update device_server set online='OFF' where online is null and cms_id=?",cmsId);
		jdbc.update("update device set online='OFF' where online is null and cms_id=?",cmsId);
		if(virtualServer!=null){
			jdbc.update("update device d set server_id=?,naming=concat(d.device_id,':',?) where server_id is null and cms_id=?",new Object[]{virtualServer.getServerId(),virtualServer.getNaming(),cmsId});
		}
	}
	
	public  void resetOrganByCmsId(String cmsId){
		jdbc.update("update organ set path=null where cms_id=?",cmsId);
		jdbc.update("update organ set parent_organ_id=null and cms_id=? where id in(select * from (select child.id from organ child where child.parent_organ_id is not null and not exists(select * from organ parent where parent.organ_id=child.parent_organ_id)) as t)",cmsId);
		jdbc.update("update organ child inner join organ parent on(child.parent_organ_id=parent.organ_id and child.cms_id = ? and child.cms_id=parent.cms_id) set child.parent_id=parent.id",cmsId);
		jdbc.update("update organ set path=concat('/',id) where parent_id is null and cms_id=?",cmsId);
		jdbc.update("update organ o inner join platform p on(o.cms_id=? and o.cms_id=p.cms_id) set o.platform_id=p.id",cmsId);
		resetOrganPath();
	}
	
	public  void createGbOrganByCmsId(String cmsId){
		jdbc.update("insert ignore into gb_organ (source_id,source_type,suspend) select id,'platform',false from platform where cms_id=?",cmsId);
		jdbc.update("insert ignore into gb_organ (source_id,source_type,suspend) select id,'organ',false from organ where cms_id=?",cmsId);
		jdbc.update("update gb_organ gbo inner join platform p on(p.cms_id=? and gbo.source_id=p.id and gbo.source_type='platform' and p.parent_id is not null) inner join platform parent on(p.parent_id=parent.id) inner join gb_organ gb_parent on(parent.id=gb_parent.source_id and gb_parent.source_type='platform') set gbo.parent_id=gb_parent.id",cmsId);
		jdbc.update("update gb_organ gbo inner join organ myorgan on(myorgan.cms_id=? and gbo.path is null and gbo.source_id=myorgan.id and gbo.source_type='organ' and myorgan.parent_id is null) inner join gb_organ gb_parent on(gb_parent.source_type='platform') inner join platform p on(p.cms_id=myorgan.cms_id and p.id=gb_parent.source_id and gb_parent.source_type='platform') set gbo.parent_id=gb_parent.id",cmsId);
		jdbc.update("update gb_organ gbo inner join organ myorgan on(myorgan.cms_id=? and gbo.path is null and gbo.source_id=myorgan.id and gbo.source_type='organ' and myorgan.parent_id is not null) inner join organ parent on(myorgan.parent_id=parent.id) inner join gb_organ gb_parent on(parent.id=gb_parent.source_id and gb_parent.source_type='organ') set gbo.parent_id=gb_parent.id",cmsId);
		registerDao.resetGbOrganPath();
	}
	
	public  void createGbDeviceByCmsId(String cmsId){
		jdbc.update("insert ignore into gb_device (path,device_id,organ_id,original_id) select gbo.path,d.id,gbo.id,gbo.id from device d inner join organ o on(d.cms_id=? and d.cms_id=o.cms_id and d.owner_id=o.organ_id) inner join gb_organ gbo on(gbo.source_type='organ' and gbo.source_id=o.id)",cmsId);
	}
	
	public   void resetDeviceStatus(){
		Timestamp changeTime = new Timestamp(System.currentTimeMillis());
		int nanosecond = changeTime.getNanos() / 1000;
		jdbc.update("update device_status ds inner join gb_device gbd on(ds.device_id=gbd.id) inner join device d on(gbd.device_id=d.id and d.sync=false and (ds.name!=d.name or ds.location!=d.location)) set ds.status=d.status,ds.base_notify=false,ds.change_time=?,ds.nanosecond=?",new Object[]{changeTime,nanosecond});
		jdbc.update("insert ignore into device_status (base_notify,change_time,nanosecond,online,online_change_time,online_nanosecond,online_notify,status,device_id,platform_id,name,location,subscribe_event_id) select false,d.change_time,d.nanosecond,d.online,d.change_time,d.nanosecond,true,0,gbd.id,se.platform_id,d.name,d.location,se.id from device d inner join gb_device gbd on(d.id=gbd.device_id) inner join subscribe_event se on(gbd.path like concat(se.path,'/%') or gbd.path=se.path)");
	}
	
	public  void resetDeviceStatusByOnline(){
		Timestamp changeTime = new Timestamp(System.currentTimeMillis());
		int nanosecond = changeTime.getNanos() / 1000;
		jdbc.update("update device_status ds inner join gb_device gbd on(ds.device_id=gbd.id) inner join device d on(gbd.device_id=d.id and (ds.online!=d.online)) set ds.online_notify=false,ds.online_change_time=?,ds.online_nanosecond=?",new Object[]{changeTime,nanosecond});
	}
	
	public  void resetOrganStatus(){
		Timestamp changeTime = new Timestamp(System.currentTimeMillis());
		int nanosecond = changeTime.getNanos() / 1000;
		jdbc.update("update organ_status os inner join gb_organ gbo on(os.organ_id=gbo.id) inner join organ o on(gbo.source_id=o.id and gbo.source_type='organ'  and o.sync=false and (os.name!=o.name or os.block!=o.block)) set os.base_notify=false,os.change_time=?,os.nanosecond=?",new Object[]{changeTime,nanosecond});
		jdbc.update("insert ignore into organ_status (base_notify,change_time,nanosecond,status,organ_id,platform_id,name,block,subscribe_event_id) select false,o.change_time,o.nanosecond,0,gbo.id,se.platform_id,o.name,o.block,se.id from organ o inner join gb_organ gbo on(o.id=gbo.source_id) inner join subscribe_event se on(gbo.path like concat(se.path,'/%') or gbo.path=se.path)");
	}

	public  String resetOrganPath() {
		Map<Integer, String> organPathMap = new HashMap<Integer, String>();
		List<Organ> organs = Ar.of(Organ.class).find("from Organ where path is null");
		StringBuilder path = new StringBuilder();
		for (Organ organ : organs) {
			organPathMap.put(organ.getId(), getPath(organ, path));
			path.setLength(0);
		}
		List<Object[]> params = new ArrayList<Object[]>();
		int i = 1;
		for (Integer id : organPathMap.keySet()) {
			params.add(new Object[] { organPathMap.get(id), id, });
			if (i++ % 200 == 0) {
				jdbc.batchUpdate("update organ set path=? where id=? ", params);
				params.clear();
			}
		}
		jdbc.batchUpdate("update organ set path=? where id=? ", params);
		return "@success";
	}
	
	public void beforeQueryOuterDevice(String cmsId){
	}

	private String getPath(Organ organ, StringBuilder path) {
		path.insert(0, organ.getId());
		path.insert(0, "/");
		if (organ.getParent() != null) {
			getPath(organ.getParent(), path);
		}
		return path.toString();
	}
}
