package com.megaeyes.regist.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.hight.performance.annotation.Param;
import net.hight.performance.filter.HPFilter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.megaeyes.regist.bean.IDevice;
import com.megaeyes.regist.bean.IDeviceServer;
import com.megaeyes.regist.bean.IOrgan;
import com.megaeyes.regist.dao.AutoScanDao;
import com.megaeyes.regist.dao.DomainDao;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.tasks.IDoInConnection;
import com.megaeyes.regist.tasks.PullDataTask;
import com.megaeyes.regist.utils.HttpclientUtils;
import com.megaeyes.regist.utils.PullHelper;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("initDB")
@Transactional
public class InitDBController {
	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private AutoScanDao autoScanDao;

	@Autowired
	private DomainDao domainDao;
	
//	@Autowired
//	private JdbcConnectionPool h2DS;
	
	@Autowired
	private RegisterDao registerDao;
	
	@Value("${insertDevicesSQL}")
	private String insertDevicesSQL;

	@Value("${insertServerSQL}")
	private String insertServerSQL;

	@Value("${insertOrganSQL}")
	private String insertOrganSQL;

	private ExecutorService executorService = Executors.newFixedThreadPool(20);
	private CompletionService<PullHelper> taskCompletionService = new ExecutorCompletionService<PullHelper>(
			executorService);

	public String index() {
		return "init/login";
	}

	public String login(Invocation inv, @Param("username") String username,
			@Param("password") String password) throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/config.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		inv.addModel("ownerCmsId", config.getProperty("owner_platform_cmsId"));
		if (((String) config.getProperty("username")).equals(username)
				&& ((String) config.getProperty("password")).equals(password)) {
			return "/init/home";
		}
		if (((String) config.getProperty("developer")).equals(username)
				&& ((String) config.getProperty("pwd")).equals(password)) {
			inv.addModel("isDeveloper", true);
			return "/init/home";
		}
		return "init/login";
	}

	@Transactional
	public synchronized String initAll() throws SQLException {
		DatabaseMetaData md = jdbc.getDataSource().getConnection()
				.getMetaData();
		ResultSet rs = md.getTables(null, null, "authorization", null);
		if (!rs.next()) {
			// 存在表authorization
			resetDatabase(jdbc.getDataSource().getConnection());
		} else {
			// 以platform为准，如果该表已经有cms_id这个字段，表示已经升级了
			rs = md.getColumns(null, null, "platform", "cms_id");
			if (rs.next()) {
				// 已经升级过，不再升级
				return "@数据库已经升级过了";
			} else {
				// 未升级
				upgrade();
			}
		}
		return "@数据库升级成功";
	}

	public String upgrade() {
		Map<Object, Object> userIdMap = new HashMap<Object, Object>();
		Map<Object, Object> originalRoleIdMap = new HashMap<Object, Object>();
		Map<Object, Object> roleIdMap = new HashMap<Object, Object>();
		Map<Object, Object> organIdMap = new HashMap<Object, Object>();
		Map<Object, Object> originalDeviceIdMap = new HashMap<Object, Object>();
		Map<Object, Object> deviceIdMap = new HashMap<Object, Object>();
		Map<Object, Object> platformIdMap = new HashMap<Object, Object>();
		Map<Object, Object> platformOrganIdMap = new HashMap<Object, Object>();

		originalRoleIdMap = domainDao.findForMap("select id,name from role",
				"id", "name");
		originalDeviceIdMap = domainDao.findForMap(
				"select id,device_id from device", "id", "device_id");
		jdbc.execute("drop table if exists device_server_status");
		jdbc.execute("drop table if exists static_file");
		initGbPlatform();
		initPlatform();
		initDeviceStatus();
		initServerStatus();
		initOrganStatus();

		initGbDevice();
		initGbOrgan();
		initGbShare();
		initPlatformOrgan();

		initDevice();
		initServer();
		initOrgan();

		initPlatformStatus();
		initSubscribeEvent();

		initRole();
		initShare();
		initUser();

		initForeignKey();
		autoScanDao.rectify();

		organIdMap = domainDao
				.findForMap(
						"select concat(organ_id,'_',cms_id) as myoran_id,id from organ ",
						"myoran_id", "id");
		platformIdMap = domainDao.findForMap("select cms_id,id from platform",
				"cms_id", "id");
		userIdMap = domainDao.findForMap("select user_id,id from user",
				"user_id", "id");
		deviceIdMap = domainDao.findForMap("select id,device_id from device",
				"device_id", "id");
		roleIdMap = domainDao.findForMap("select id,name from role", "name",
				"id");
		platformOrganIdMap = domainDao
				.findForMap(
						"select source_id,id from platform_organ where source_type='organ'",
						"source_id", "id");

		initAuthorization(userIdMap, platformIdMap, deviceIdMap, organIdMap,
				originalDeviceIdMap, originalRoleIdMap, roleIdMap,
				platformOrganIdMap);
		initUserRole(userIdMap, roleIdMap, originalRoleIdMap);
		return "@success";
	}

	private void initUserRole(Map<Object, Object> userIdMap,
			Map<Object, Object> roleIdMap, Map<Object, Object> originalRoleIdMap) {
		List<Object[]> params = new ArrayList<Object[]>();
		List<Map<String, Object>> rows = jdbc
				.queryForList("select * from r_user_role");
		for (Map<String, Object> row : rows) {
			Object[] param = new Object[] {
					roleIdMap.get(originalRoleIdMap.get(row.get("role_id"))),
					userIdMap.get(row.get("user_id")) };
			params.add(param);
		}
		initUserRole();
		String sql = "insert ignore into r_user_role (role_id,user_id) values(?,?)";
		jdbc.batchUpdate(sql, params);

	}

	private void initAuthorization(Map<Object, Object> userIdMap,
			Map<Object, Object> platformIdMap, Map<Object, Object> deviceIdMap,
			Map<Object, Object> organIdMap,
			Map<Object, Object> originalDeviceIdMap,
			Map<Object, Object> originalRoleIdMap,
			Map<Object, Object> roleIdMap,
			Map<Object, Object> platformOrganIdMap) {
		List<Object[]> params = new ArrayList<Object[]>();
		List<Map<String, Object>> rows = jdbc
				.queryForList("select * from authorization");
		Object grantedId = null;
		Object resourceId = null;
		Object resourceType = null;

		for (Map<String, Object> row : rows) {
			if ((Integer) row.get("granted_type") == 0) {
				grantedId = userIdMap.get(row.get("granted_id"));
			} else {
				grantedId = roleIdMap.get(originalRoleIdMap.get(row
						.get("granted_id")));
			}

			if ((Integer) row.get("resource_type") == 1) {
				resourceId = platformOrganIdMap.get(organIdMap.get(row
						.get("resource_id")));
				resourceType = "organ";
			} else {
				resourceId = deviceIdMap.get(originalDeviceIdMap.get(row
						.get("resource_id")));
				resourceType = "device";
			}

			Object[] param = new Object[] { row.get("cms_id"), grantedId,
					row.get("granted_type"), row.get("item"),
					platformIdMap.get(row.get("resource_cms_id")), resourceId,
					resourceType };
			params.add(param);
		}
		initAuthorization();
		String sql = "insert ignore into authorization (cms_id,granted_id,granted_type,item,resource_cms_id,resource_id,resource_type) values(?,?,?,?,?,?,?)";
		jdbc.batchUpdate(sql, params);
		jdbc.update("update authorization au inner join device d on(au.resource_id=d.id and au.resource_type='device') set au.resource_path=d.path");
		jdbc.update("update authorization au inner join platform_organ o on(au.resource_id=o.id and au.resource_type='organ') set au.resource_path=o.path");
	}

	public String initPlatformOrgan() {
		jdbc.execute("drop table if exists platform_organ");
		jdbc.execute("create table platform_organ (          "
				+ "    id int(20) not null auto_increment,"
				+ "    path varchar(255),                 "
				+ "    organ_id varchar(255),             "
				+ "    source_id int(20),                 "
				+ "    source_type varchar(255),          "
				+ "    name varchar(255),          "
				+ "    platform_id int(20),          "
				+ "    parent_id int(20),                 "
				+ "    primary key (id),                  "
				+ "    unique (source_id, source_type)    "
				+ ") engine=InnoDb default charset=gbk");
		return "@success";

	}

	@Transactional
	public String initAuthorization() {
		jdbc.execute("drop table authorization");
		jdbc.execute("create table authorization ( "
				+ "id int(20) not null auto_increment, "
				+ "cms_id int(20),                     "
				+ "granted_id int(20),                 "
				+ "granted_type integer,               "
				+ "item varchar(255),                  "
				+ "resource_cms_id int(20),            "
				+ "resource_id int(20),                "
				+ "resource_path varchar(255),         "
				+ "resource_type varchar(255),         "
				+ "primary key (id),                   "
				+ "unique (granted_id, granted_type, resource_id, resource_type)) engine=InnoDb default charset=gbk");
		return "@success";
	}

	@Transactional
	public String initGbPlatform() {
		jdbc.execute("alter table gb_platform add unique(cms_id)");
		return "@success";
	}

	@Transactional
	public String initDeviceStatus() {
		jdbc.execute("drop table device_status");
		jdbc.execute("create table device_status ("
				+ "id int(20) not null auto_increment,             "
				+ "base_notify bit not null,                       "
				+ "base_notify_sign integer not null default 0,              "
				+ "change_time datetime,                           "
				+ "device_id int(20),                              "
				+ "location varchar(255),                          "
				+ "name varchar(255),                              "
				+ "nanosecond integer not null,                    "
				+ "online varchar(255),                            "
				+ "online_change_time datetime,                    "
				+ "online_nanosecond integer not null,             "
				+ "online_notify bit not null,                     "
				+ "online_notify_sign integer not null default 0,            "
				+ "status varchar(20),                                 "
				+ "subscribe_event_id int(20),                     "
				+ "platform_id int(20),                            "
				+ "primary key (id),                               "
				+ "unique (device_id, platform_id)                 "
				+ "	    ) engine=InnoDb default charset=gbk                       ");
		return "@success";
	}

	@Transactional
	public String initDevice() {
		jdbc.execute("create table temp_device like device");
		jdbc.execute("insert into  temp_device select * from device");
		jdbc.execute("drop table device");
		createDeviceTable(jdbc);
		jdbc.execute("replace into device ( alarm_reset ,            "
				+ "alarm_statu ,          " + "allocated,             "
				+ "change_time ,          " + "cms_id ,               "
				+ "device_id ,            " + "dircetion_type ,       "
				+ "dispatcher_platform_id," + "gps_z ,                "
				+ "latitude ,             " + "location ,             "
				+ "longitude ,            " + "name ,                 "
				+ "naming ,               " + "nanosecond,            "
				+ "online ,               " + "             "
				+ "outer_platforms ,      " + "owner_id ,             "
				+ "path ,                 " + "permission ,           "
				+ "position_type ,        " + "ptz_type ,             "
				+ "record_count,          " + "room_type ,            "
				+ "server_id,             " + "status ,               "
				+ "std_id ,               " + "supply_light_type ,    "
				+ "support_scheme,        " + "sync,                  "
				+ "type ,                 " + "use_type)  select    "
				+ "alarm_reset ,            " + "alarm_statu ,          "
				+ "allocated,             " + "change_time ,          "
				+ "cms_id ,               " + "device_id ,            "
				+ "dircetion_type ,       " + "dispatcher_platform_id,"
				+ "gps_z ,                " + "latitude ,             "
				+ "location ,             " + "longitude ,            "
				+ "name ,                 " + "naming ,               "
				+ "nanosecond,            " + "online ,               "
				+ "outer_platforms ,      " + "owner_id ,             "
				+ "path ,                 " + "permission ,           "
				+ "position_type ,        " + "ptz_type ,             "
				+ "record_count,          " + "room_type ,            "
				+ "substr(server_id,1,locate('_',server_id)-1),"
				+ "status ,               " + "std_id ,               "
				+ "supply_light_type ,    " + "support_scheme,        "
				+ "sync,                  " + "type ,                 "
				+ "use_type from temp_device");
		jdbc.execute("drop table temp_device");
		return "@success";
	}
	
	public void createDeviceTable(JdbcTemplate jdbc){
		jdbc.execute("create table device (                             "
				+ "id int(20) not null auto_increment,               "
				+ "alarm_reset varchar(255),                         "
				+ "alarm_statu varchar(255),                         "
				+ "allocated bit not null,                           "
				+ "change_time datetime,                             "
				+ "cms_id varchar(255),                              "
				+ "device_id varchar(255),                           "
				+ "dircetion_type integer,                           "
				+ "dispatcher_platform_id varchar(255),              "
				+ "gps_z varchar(255),                               "
				+ "latitude varchar(255),                            "
				+ "location varchar(255),                            "
				+ "longitude varchar(255),                           "
				+ "name varchar(255),                                "
				+ "naming varchar(255),                              "
				+ "nanosecond integer not null,                      "
				+ "online varchar(255),                              "
				+ "organ_id int(20),                            "
				+ "outer_platforms varchar(255),                     "
				+ "owner_id varchar(255),                            "
				+ "path varchar(255),                                "
				+ "permission varchar(255),                          "
				+ "position_type integer,                            "
				+ "ptz_type integer,                                 "
				+ "record_count bigint not null,                     "
				+ "room_type integer,                                "
				+ "server_id varchar(255),                           "
				+ "status varchar(255),                              "
				+ "std_id varchar(255),                              "
				+ "supply_light_type integer,                        "
				+ "support_scheme bit not null,                      "
				+ "sync bit not null,                                "
				+ "type varchar(255),                                "
				+ "use_type integer, "
				+ "platform_id int(20),                                "
				+ "primary key (id),                                 "
				+ "unique (device_id, owner_id, cms_id)) engine=InnoDb default charset=gbk");
	}

	@Transactional
	public String initGbDevice() {
		jdbc.execute("drop table if exists gb_device");
		jdbc.execute("create table gb_device (              "
				+ "	id int(20) not null auto_increment,"
				+ "	path varchar(255),                 "
				+ "	suspend bit default false,                       "
				+ "	device_id int(20),                 "
				+ "	organ_id int(20),                  "
				+ "	original_id int(20),                  "
				+ "	primary key (id),unique (device_id, original_id)) engine=InnoDb default charset=gbk;   ");
		return "@success";
	}

	@Transactional
	public String initServer() {
		jdbc.execute("create table temp_server like device_server");
		jdbc.execute("insert into  temp_server select * from device_server");
		jdbc.execute("drop table device_server");
		jdbc.execute("create table device_server (      "
				+ "id int(20) not null auto_increment,"
				+ "ip varchar(255),                  "
				+ "change_time datetime,             "
				+ "children_status bit not null,     "
				+ "cms_id varchar(255),              "
				+ "inner_device bit not null default 1,        "
				+ "location varchar(255),            "
				+ "manufacturer varchar(255),        "
				+ "model varchar(255),               "
				+ "name varchar(255),                "
				+ "naming varchar(255),              "
				+ "nanosecond integer not null,      "
				+ "online varchar(255),              "
				+ "organ_id int(20) ,            "
				+ "server_id varchar(255),           "
				+ "status varchar(255),              "
				+ "std_id varchar(255),              "
				+ "stream_support varchar(255),      "
				+ "sync bit not null,                "
				+ "type varchar(255),                "
				+ "primary key (id),                 "
				+ "unique (server_id,cms_id)) engine=InnoDb default charset=gbk");
		jdbc.execute("replace into device_server (" + "ip ,               "
				+ "change_time ,      " + "children_status ,  "
				+ "cms_id ,           " + "inner_device ,     "
				+ "location ,         " + "manufacturer ,     "
				+ "model ,            " + "name ,             "
				+ "naming ,           " + "nanosecond,       "
				+ "online ,           " + "         " + "server_id ,        "
				+ "status ,           " + "std_id ,           "
				+ "stream_support ,   " + "sync ,             "
				+ "type) select       " + "ip ,               "
				+ "change_time ,      " + "children_status ,  "
				+ "cms_id ,           " + "1 ,     " + "location ,         "
				+ "manufacturer ,     " + "model ,            "
				+ "name ,             " + "naming ,           "
				+ "nanosecond,        " + "online ,           " + "       "
				+ "id ,        " + "status ,           "
				+ "std_id ,           " + "stream_support ,   "
				+ "sync ,             " + "type from temp_server     ");
		jdbc.execute("drop table temp_server");
		return "@success";

	}

	@Transactional
	public String initGbOrgan() {
		jdbc.execute("drop table if exists gb_organ");
		jdbc.execute("create table gb_organ (            "
				+ "id int(20) not null auto_increment,"
				+ "path varchar(255),                 "
				+ "source_id int(20),            "
				+ "source_type varchar(255),          "
				+ "suspend bit default false,                       "
				+ "parent_id int(20),                 "
				+ "primary key (id),unique (source_id, source_type)"
				+ "	    ) engine=InnoDb default charset=gbk;");
		return "@success";
	}

	@Transactional
	public String initGbShare() {
		jdbc.execute("drop table if exists gb_share");
		jdbc.execute("create table gb_share (                                    "
				+ "id int(20) not null auto_increment,                            "
				+ "platform_id int(20) not null,                                  "
				+ "resource_id int(20),                                           "
				+ "resource_path varchar(255),                                    "
				+ "resource_type varchar(255),                                    "
				+ "primary key (id),                                              "
				+ "unique (platform_id, resource_id, resource_type)) engine=InnoDb default charset=gbk");
		return "@success";
	}

	public String initPlatformStatus() {
		jdbc.execute("drop table if exists platform_status");
		jdbc.execute(" create table platform_status (       "
				+ "id int(20) not null auto_increment,"
				+ "description varchar(255),          "
				+ "error_code varchar(255),           "
				+ "heart_beat_cycle integer,          "
				+ "heartbeat_time datetime,           "
				+ "online bit not null,               "
				+ "gb_platform_id int(20),            " + "primary key (id),"
				+ "unique (gb_platform_id)) engine=InnoDb default charset=gbk");
		return "@success";
	}

	@Transactional
	public String initOrgan() {
		jdbc.execute("set foreign_key_checks=0");
		jdbc.execute("create table temp_organ like organ");
		jdbc.execute("insert into temp_organ select * from organ");
		jdbc.execute("drop table organ");
		jdbc.execute("create table organ (          "
				+ "id int(20) not null auto_increment,          "
				+ "block varchar(255),           "
				+ "change_time datetime,         "
				+ "cms_id varchar(255),          "
				+ "name varchar(255),            "
				+ "nanosecond integer not null,  "
				+ "organ_id varchar(255),        "
				+ "parent_organ_id varchar(255), "
				+ "parent_organ_name varchar(255),"
				+ "path varchar(255),            "
				+ "status varchar(255),          "
				+ "std_id varchar(255),          "
				+ "sync bit not null,            "
				+ "type varchar(255),            "
				+ "parent_id int(20),            "
				+ "platform_id int(20),            " + "primary key (id), "
				+ "unique (organ_id, cms_id)     "
				+ ")engine=InnoDb default charset=gbk         ");
		jdbc.execute("replace into organ (" + "block ,           "
				+ "change_time,         " + "cms_id ,          "
				+ "name ,            " + "nanosecond,  " + "organ_id ,        "
				+ "parent_organ_id, " + "parent_organ_name,"
				+ "path ,            " + "status ,          "
				+ "std_id ,          " + "sync,            " + "type) select  "
				+ "block ,           " + "change_time,         "
				+ "cms_id ,          " + "name ,            " + "nanosecond,  "
				+ "organ_id ,        "
				+ "substr(parent_organ_id,1,locate('_',parent_organ_id)-1) , "
				+ "parent_organ_name, " + "path ,            "
				+ "status ,          " + "std_id ,          "
				+ "sync,            " + "type from temp_organ");
		jdbc.execute("drop table temp_organ");
		jdbc.execute("set foreign_key_checks=1");
		return "@success";

	}

	@Transactional
	public String initOrganStatus() {
		jdbc.execute("drop table if exists organ_status");
		jdbc.execute("create table organ_status (       "
				+ "id int(20) not null auto_increment,"
				+ "base_notify bit not null,         "
				+ "base_notify_sign integer not null default 0 ,"
				+ "block varchar(255),               "
				+ "change_time datetime,             "
				+ "name varchar(255),                "
				+ "nanosecond integer not null,      "
				+ "organ_id int(20),                 "
				+ "status varchar(20),                   "
				+ "subscribe_event_id int(20),       "
				+ "platform_id int(20),              "
				+ "primary key (id),                 "
				+ "unique (organ_id, platform_id)    "
				+ "	    ) engine=InnoDb default charset=gbk");
		return "@success";
	}

	@Transactional
	public String initPlatform() {
		jdbc.execute("set foreign_key_checks=0");
		jdbc.execute("create table temp_platform like platform");
		jdbc.execute("insert into temp_platform select * from platform");
		jdbc.execute("drop table platform");
		jdbc.execute("create table platform (            "
				+ "id int(20) not null auto_increment,"
				+ "cms_id varchar(255),               "
				+ "event_server_ip varchar(255),      "
				+ "event_server_port integer,         "
				+ "gb_platform_cms_id varchar(255),   "
				+ "name varchar(255),                 "
				+ "owner bit not null,                "
				+ "parent_cms_id varchar(255),        "
				+ "password varchar(255),             "
				+ "service_url varchar(255),          "
				+ "status integer,                    "
				+ "sync bit not null,                 "
				+ "parent_id int(20),                 "
				+ "primary key (id),                  "
				+ "unique (cms_id)                    "
				+ "	    ) engine=InnoDb default charset=gbk");
		jdbc.execute("replace into platform (" + "cms_id ,               "
				+ "event_server_ip ,      " + "event_server_port,         "
				+ "gb_platform_cms_id ,   " + "name ,                 "
				+ "owner,                " + "parent_cms_id ,        "
				+ "password ,             " + "service_url ,          "
				+ "status,                    "
				+ "sync) select                  " + "id ,               "
				+ "event_server_ip ,      " + "event_server_port,         "
				+ "gb_platform_cms_id ,   " + "name ,                 "
				+ "owner,                " + "parent_cms_id ,        "
				+ "password ,             " + "service_url ,          "
				+ "status,                " + "sync from temp_platform");
		jdbc.execute("drop table temp_platform");
		jdbc.execute("update platform child inner join platform parent on(child.parent_cms_id=parent.cms_id) set child.parent_id=parent.id");
		jdbc.execute("set foreign_key_checks=1");
		return "@success";
	}

	@Transactional
	public String initRole() {
		jdbc.execute("create table temp_role like role");
		jdbc.execute("insert into  temp_role select * from  role");
		jdbc.execute("drop table role");
		jdbc.execute("create table role (         "
				+ "id int(20) not null auto_increment, "
				+ "cms_id varchar(255),                "
				+ "name varchar(20),                   "
				+ "note varchar(255),                  " + "primary key (id)"
				+ ") engine=InnoDb default charset=gbk");
		jdbc.execute("replace into role (cms_id,name,note) select cms_id,name,note from temp_role");
		jdbc.execute("drop table temp_role");
		return "@success";
	}

	@Transactional
	public String initServerStatus() {
		jdbc.execute("drop table server_status");
		jdbc.execute("create table server_status ("
				+ "id int(20) not null auto_increment,"
				+ "base_notify bit not null,          "
				+ "change_time datetime,              "
				+ "location varchar(255),             "
				+ "name varchar(255),                 "
				+ "nanosecond integer not null,       "
				+ "online varchar(255),               "
				+ "online_change_time datetime,       "
				+ "online_nanosecond integer not null,"
				+ "online_notify bit not null,        "
				+ "server_id int(20),                 "
				+ "status varchar(20),                    "
				+ "platform_id int(20),               "
				+ "primary key (id)                   "
				+ "	    ) engine=InnoDb default charset=gbk");
		return "@success";
	}

	@Transactional
	public String initShare() {
		jdbc.execute("drop table share");
		jdbc.execute("create table share (                                  "
				+ "id int(20) not null auto_increment,                  "
				+ "item varchar(255),                                   "
				+ "platform_cms_id varchar(255),                        "
				+ "resource_cms_id varchar(255),                        "
				+ "resource_id int(20),                                 "
				+ "resource_path varchar(255),                          "
				+ "resource_type integer,                               "
				+ "primary key (id),                                    "
				+ "unique (platform_cms_id, resource_id, resource_type) "
				+ "	) engine=InnoDb default charset=gbk                               ");
		return "@success";
	}

	@Transactional
	public String initSubscribeEvent() {
		jdbc.execute("drop table subscribe_event");
		jdbc.execute("create table subscribe_event ("
				+ "id int(20) not null auto_increment,  "
				+ "device_id varchar(255),              "
				+ "expire_date datetime,                "
				+ "organ_id int(20),                    "
				+ "path varchar(255),                   "
				+ "platform_id int(20),                 "
				+ "subscribe_id varchar(255),           "
				+ "primary key (id)                     "
				+ "		    ) engine=InnoDb default charset=gbk");
		return "@success";
	}

	@Transactional
	public String initUser() {
		jdbc.execute("create table temp_user like user");
		jdbc.execute("insert into temp_user select * from user");
		jdbc.execute("drop table user");
		jdbc.execute("create table user ( "
				+ "id int(20) not null auto_increment,     "
				+ "user_id varchar(255),                   "
				+ "change_time datetime,                   "
				+ "cms_id varchar(255),                    "
				+ "logon_name varchar(255),                "
				+ "name varchar(255),                      "
				+ "naming varchar(255),                    "
				+ "nanosecond integer not null,            "
				+ "organ_id varchar(255),                  "
				+ "password varchar(255),                  "
				+ "sex varchar(255),                       "
				+ "status varchar(255),                    "
				+ "sync bit not null,                      "
				+ "primary key (id)                        "
				+ "	  	) engine=InnoDb default charset=gbk");
		jdbc.execute("replace into user ("
				+ "user_id,                    "
				+ "change_time,                "
				+ "cms_id ,                    "
				+ "logon_name ,                "
				+ "name ,                      "
				+ "naming ,                    "
				+ "nanosecond,"
				+ "organ_id ,                  "
				+ "password ,                  "
				+ "sex ,                       "
				+ "status ,                    "
				+ "sync) select "
				+ "id,                    "
				+ "change_time,                "
				+ "cms_id ,                    "
				+ "logon_name ,                "
				+ "name ,                      "
				+ "naming ,                    "
				+ "nanosecond,"
				+ "substr(organ_id,1,locate('_',organ_id)-1) ,                  "
				+ "password ,                  "
				+ "sex ,                       "
				+ "status ,                    " + "sync from temp_user ");
		jdbc.execute("drop table temp_user");
		return "@success";
	}

	@Transactional
	public String initUserRole() {
		jdbc.execute("drop table r_user_role");
		jdbc.execute("create table r_user_role ("
				+ "	role_id int(20) not null,       "
				+ "	user_id int(20) not null,       "
				+ "	primary key (role_id, user_id)  "
				+ ") engine=InnoDb default charset=gbk                   ");

		try {
			jdbc.execute("alter table r_user_role                              "
					+ "    add index FK95488DD1B4D8FF9 (role_id),           "
					+ "    add constraint FK95488DD1B4D8FF9                 "
					+ "    foreign key (role_id)                            "
					+ "    references role (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			jdbc.execute("alter table r_user_role                              "
					+ "    add index FK95488DDC07853D9 (user_id),           "
					+ "    add constraint FK95488DDC07853D9                 "
					+ "    foreign key (user_id)                            "
					+ "    references user (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "@success";
	}

	public String removeForeignKey() {

		return "@success";
	}

	@Transactional
	public String initForeignKey() {

		try {
			jdbc.execute("alter table device                                   "
					+ "    add index FKB06B1E56AC90B1FB (organ_id),         "
					+ "    add constraint FKB06B1E56AC90B1FB                "
					+ "    foreign key (organ_id)                           "
					+ "    references organ (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			jdbc.execute("alter table device                                   "
					+ "    add index FKB06B1E5647881B59 (platform_id),      "
					+ "    add constraint FKB06B1E5647881B59                "
					+ "    foreign key (platform_id)                        "
					+ "    references platform (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			jdbc.execute("alter table device_status                            "
					+ "    add index FKC65A01FBB9D2DD94 (platform_id),      "
					+ "    add constraint FKC65A01FBB9D2DD94                "
					+ "    foreign key (platform_id)                        "
					+ "    references gb_platform (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			jdbc.execute(" alter table gb_device                                 "
					+ "    add index FKBD96F23ACB9091A0 (organ_id),          "
					+ "    add constraint FKBD96F23ACB9091A0                 "
					+ "    foreign key (organ_id)                            "
					+ "    references gb_organ (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			jdbc.execute(" alter table gb_device                                    "
					+ "    add index FKBD96F23AE44F81E0 (original_id),          "
					+ "    add constraint FKBD96F23AE44F81E0                    "
					+ "    foreign key (original_id)                            "
					+ "    references gb_organ (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			jdbc.execute(" alter table gb_device                                 "
					+ "    add index FKBD96F23A533BC039 (device_id),         "
					+ "    add constraint FKBD96F23A533BC039                 "
					+ "    foreign key (device_id)                           "
					+ "    references device (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			jdbc.execute("alter table gb_organ                                 "
					+ "    add index FKB429B1ADF880E867 (parent_id),        "
					+ "    add constraint FKB429B1ADF880E867                "
					+ "    foreign key (parent_id)                          "
					+ "    references gb_organ (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			jdbc.execute("alter table organ                                    "
					+ "    add index FK651921147881B59 (platform_id),       "
					+ "    add constraint FK651921147881B59                 "
					+ "    foreign key (platform_id)                        "
					+ "    references platform (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			jdbc.execute("alter table organ                                    "
					+ "    add index FK6519211D98108C2 (parent_id),         "
					+ "    add constraint FK6519211D98108C2                 "
					+ "    foreign key (parent_id)                          "
					+ "    references organ (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			jdbc.execute("alter table organ_status                             "
					+ "    add index FK2A57F5E0B9D2DD94 (platform_id),      "
					+ "    add constraint FK2A57F5E0B9D2DD94                "
					+ "    foreign key (platform_id)                        "
					+ "    references gb_platform (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			jdbc.execute("alter table platform                                 "
					+ "    add index FK6FBD6873784F7BC2 (parent_id),        "
					+ "    add constraint FK6FBD6873784F7BC2                "
					+ "    foreign key (parent_id)                          "
					+ "    references platform (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			jdbc.execute("alter table platform_status                          "
					+ "    add index FK62428D3EC323D830 (gb_platform_id),   "
					+ "    add constraint FK62428D3EC323D830                "
					+ "    foreign key (gb_platform_id)                     "
					+ "    references gb_platform (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			jdbc.execute("alter table server_status                            "
					+ "    add index FK43277C6EB9D2DD94 (platform_id),      "
					+ "    add constraint FK43277C6EB9D2DD94                "
					+ "    foreign key (platform_id)                        "
					+ "    references gb_platform (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			jdbc.execute("alter table platform_organ  "
					+ "add index FKE1E9FB45A3EF8F0F (parent_id), "
					+ "add constraint FKE1E9FB45A3EF8F0F      "
					+ "foreign key (parent_id)      "
					+ "references platform_organ (id)");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "@success";
	}

	public String getOwner(Invocation inv) {
		List<Platform> platforms = Ar.of(Platform.class).find();
		for (Platform p : platforms) {
			System.out.println(p.getName());
		}
		return "@success";
	}

	// private String organSQL =
	// "update organ set std_id=concat(substr(std_id,1,10),'215',substr(std_id,14,20)) where parent_id is null and length(std_id)=20";
	// private String virtureOrganSQL =
	// "update organ set std_id=concat(substr(std_id,1,10),'216',substr(std_id,14,20)) where parent_id is not null and length(std_id)=20";

	public void resetDatabase(Connection c) throws SQLException {
		String s = new String();
		StringBuffer sb = new StringBuffer();
		try {
			File file = new File(HPFilter.getFc().getServletContext()
					.getRealPath("WEB-INF/register_init.sql"));
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			while ((s = br.readLine()) != null) {
				if (!s.startsWith("##")) {
					sb.append(s);
				}
			}
			br.close();
			String[] inst = sb.toString().split(";");
			Statement st = c.createStatement();
			for (int i = 0; i < inst.length; i++) {
				if (!inst[i].trim().equals("")) {
					st.executeUpdate(inst[i]);
					System.out.println(">>" + inst[i]);
				}
			}

		} catch (Exception e) {
			System.out.println("*** Error : " + e.toString());
			System.out.println("*** ");
			System.out.println("*** Error : ");
			e.printStackTrace();
			System.out
					.println("################################################");
			System.out.println(sb.toString());
		}
	}

	public String showOracleMsgList(Invocation inv)
			throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/oracle_jdbc.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		Object cmsIds = config.getProperty("cmsIds");
		if (cmsIds instanceof String) {
			String cmsId = (String) cmsIds;
			inv.addModel("cmsId", cmsId);
		}
		inv.addModel("config", config);
		return "init/oracle-msg-list";
	}

	public String updateOracleConf(Invocation inv)
			throws ConfigurationException {
		saveOravleConf(inv);
		return "@success";
	}

	public PropertiesConfiguration saveOravleConf(Invocation inv)
			throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/oracle_jdbc.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		Enumeration<String> e = inv.getRequest().getParameterNames();
		while (e.hasMoreElements()) {
			String key = e.nextElement();
			if (config.containsKey(key)) {
				config.setProperty(key, inv.getRequest().getParameter(key));
			}
		}
		config.save();
		return config;
	}

	public String executePull(Invocation inv, @Param("cmsId") String cmsId)
			throws ExecutionException, ConfigurationException {
		long beginTime = System.currentTimeMillis();
		PropertiesConfiguration config = saveOravleConf(inv);
		PullDataTask task = new PullDataTask();
		task.setCmsId(cmsId);
		task.setConfig(config);
		taskCompletionService.submit(task);
		StringBuilder resp = new StringBuilder("{\"cmsId\":\"");
		resp.append(cmsId).append("\",\"result\":\"");
		try {
			Future<PullHelper> result = taskCompletionService.take();
			PullHelper helper = result.get();
			analyzeOrgan(helper);
			analyzeDevice(helper);
			analyzeServer(helper);
			autoScanDao.rectifyByCmsId(cmsId);
		} catch (InterruptedException e) {
			e.printStackTrace();
			resp.append("获取失败，需要时长:");
			resp.append((System.currentTimeMillis() - beginTime) / 1000f)
					.append("(sec)\"}");
		}
		resp.append("获取成功，需要时长:");
		resp.append((System.currentTimeMillis() - beginTime) / 1000f).append(
				"(sec)\"}");
		return "@"+resp.toString();
	}

	@SuppressWarnings("unchecked")
	public String executeAllPull() throws ConfigurationException,
			ExecutionException {
		long start = System.currentTimeMillis();
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/oracle_jdbc.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		List<String> cmsIds = new ArrayList<String>();
		if (config.getProperty("cmsIds") instanceof List) {
			cmsIds.addAll((List<String>) config.getProperty("cmsIds"));
		}else{
			cmsIds.add((String)config.getProperty("cmsIds"));
		}
		for (String cmsId : cmsIds) {
			PullDataTask task = new PullDataTask();
			task.setCmsId(cmsId);
			task.setConfig(config);
			taskCompletionService.submit(task);
		}
		StringBuilder resp = new StringBuilder("[");
		for (int i = 0; i < cmsIds.size(); i++) {
			long beginTime = System.currentTimeMillis();
			try {
				Future<PullHelper> result = taskCompletionService.take();
				PullHelper helper = result.get();
				resp.append("{\"cmsId\":\"");
				resp.append(helper.getCmsId()).append("\",\"result\":\"");
				if(helper.getMsg().equals("ok")){
					analyzeOrgan(helper);
					analyzeDevice(helper);
					analyzeServer(helper);
				}else{
					resp.append("获取失败，请检查配置是否正确。需要时长:");
					resp.append((System.currentTimeMillis() - beginTime) / 1000f)
							.append("(sec)\"},");
					continue;
				}
			} catch (Exception e) {
				e.printStackTrace();
				resp.append("{\"cmsId\":\"");
				resp.append("unkown").append("\",\"result\":\"");
				resp.append("获取失败，请检查配置是否正确。需要时长:");
				resp.append((System.currentTimeMillis() - beginTime) / 1000f)
						.append("(sec)\"},");
				continue;
			}
			resp.append("获取成功，需要时长:");
			resp.append((System.currentTimeMillis() - beginTime) / 1000f).append(
					"(sec)\"},");
		}
		autoScanDao.rectify();
		resp.append("{\"cmsId\":\"all\",\"result\":\"需要总时长:");
		resp.append((System.currentTimeMillis() - start) / 1000f).append(
				"(sec)\"}]");
		return "@"+resp.toString();
	}
	
	private void analyzeOrgan(PullHelper helper){
		Map<String, Object[]> params = new HashMap<String, Object[]>();
		List<IOrgan> organs = helper.getOrgans();
		for (IOrgan organ : organs) {
			organ.setCmsId(helper.getCmsId());
			params.put(organ.getOrganId(),
					registerDao.getInsertOrganParams(organ));
		}
		List<Organ> organList = Ar.of(Organ.class).find("from Organ where cmsId=?",helper.getCmsId());
		for(Organ organ :organList){
			if(!params.containsKey(organ.getOrganId())){
				organ.setStatus("delete");
				organ.setSync(false);
				params.put(organ.getOrganId(),
						registerDao.getInsertOrganParams(organ));
			}
		}
		executeSave(params,insertOrganSQL);
		params.clear();
	}
	
	private void analyzeDevice(PullHelper helper){
		Map<String, Object[]> params = new HashMap<String, Object[]>();
		List<IDevice> deviceList = helper.getDevices();
		for (IDevice device : deviceList) {
			Device dbDevice = new Device();
			dbDevice.setCmsId(helper.getCmsId());
			registerDao.setDeviceProfile(dbDevice, device);
			params.put(dbDevice.getDeviceId() + "_" + dbDevice.getOwnerId(),
					registerDao.getInsertDeviceSQLParams(dbDevice));
		}
		List<Device> devices = Ar.of(Device.class).find("from Device where cmsId=?",helper.getCmsId());
		for(Device dbDevice :devices){
			if(!params.containsKey(dbDevice.getDeviceId() + "_" + dbDevice.getOwnerId())){
				dbDevice.setStatus("delete");
				dbDevice.setSync(false);
				params.put(dbDevice.getDeviceId() + "_" + dbDevice.getOwnerId(),
						registerDao.getInsertDeviceSQLParams(dbDevice));
			}
		}
		executeSave(params,insertDevicesSQL);
		params.clear();
	}
	
	private void analyzeServer(PullHelper helper){
		Map<String, Object[]> params = new HashMap<String, Object[]>();
		List<IDeviceServer> serverList = helper.getServers();
		for (IDeviceServer server : serverList) {
			server.setCmsId(helper.getCmsId());
			params.put(server.getServerId(),registerDao.getInsertServerParams(server,true));
		}
		List<DeviceServer> servers = Ar.of(DeviceServer.class).find("from DeviceServer where cmsId=?",helper.getCmsId());
		for(DeviceServer server :servers){
			if(!params.containsKey(server.getServerId())){
				server.setStatus("delete");
				server.setSync(false);
				params.put(server.getServerId(),registerDao.getInsertServerParams(server,true));
			}
		}
		executeSave(params,insertServerSQL);
		params.clear();
	}
	
	private void executeSave(Map<String, Object[]> mapParams, String sql) {
		List<Object[]> params = new ArrayList<Object[]>();
		Set<String> keys = mapParams.keySet();
		for (String key : keys) {
			params.add(mapParams.get(key));
			if (params.size() > 100) {
				jdbc.batchUpdate(sql, params);
				params.clear();
			}
		}
		jdbc.batchUpdate(sql, params);
		params.clear();
	}

	@SuppressWarnings("unchecked")
	public String addOracleConf(Invocation inv, @Param("user") String user,
			@Param("cmsId") String cmsId, @Param("password") String password,
			@Param("IPPORT") String IPPORT, @Param("sid") String sid,
			@Param("name") String name, @Param("share") String share)
			throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/oracle_jdbc.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		if (!config.containsKey("cmsIds")) {
			config.setProperty("cmsIds", cmsId);
		} else {
			if (config.getProperty("cmsIds") instanceof List) {
				List<String> cmsIds = (List<String>) config
						.getProperty("cmsIds");
				if (cmsIds.contains(cmsId)) {
					return "@cmsId.has.exist";
				}
				cmsIds.add(cmsId);
				config.setProperty("cmsIds", cmsIds);
			} else {
				String cmsIds = (String) config.getProperty("cmsIds");
				if (cmsId.equals(cmsIds)) {
					return "@cmsId.has.exist";
				} else {
					config.setProperty("cmsIds", cmsIds + "," + cmsId);
				}
			}

		}
		String prefix = "p" + cmsId;
		config.setProperty(prefix + "_jdbc.user", user);
		config.setProperty(prefix + "_jdbc.password", password);
		config.setProperty(prefix + "_jdbc.IPPORT", IPPORT);
		config.setProperty(prefix + "_jdbc.sid", sid);
		config.setProperty(prefix + "_name", name);
		config.setProperty(prefix + "_share.war.url", share);
		config.save();
		return "@success";
	}

	@SuppressWarnings("unchecked")
	public String deleteOracleConf(Invocation inv, @Param("cmsId") String cmsId)
			throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/oracle_jdbc.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		if (config.getProperty("cmsIds") instanceof List) {
			List<String> cmsIds = (List<String>) config.getProperty("cmsIds");
			cmsIds.remove(cmsId);
		} else {
			config.clearProperty("cmsIds");
		}

		String prefix = "p" + cmsId;
		config.clearProperty(prefix + "_jdbc.user");
		config.clearProperty(prefix + "_jdbc.password");
		config.clearProperty(prefix + "_jdbc.IPPORT");
		config.clearProperty(prefix + "_jdbc.sid");
		config.save();
		return "@success";
	}

	public String testOracleConf(Invocation inv, @Param("user") String user,
			@Param("cmsId") String cmsId, @Param("password") String password,
			@Param("IPPORT") String IPPORT, @Param("sid") String sid)
			throws ClassNotFoundException {

		Connection connection = null;
		try {
			connection = PullDataTask.getOracleConn(connection, IPPORT, sid,
					user, password);
			if (connection == null) {
				return "@error";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "@error";
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return "@success";
	}

	public String forImportData(Invocation inv) {
		return "init/for-import-data";
	}

	public void backupMysql(Invocation inv) throws ConfigurationException,
			IOException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/jdbc_init.properties"));
		File configFile = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/config.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		PropertiesConfiguration cnf= new PropertiesConfiguration(configFile);
		String user = (String) config.getProperty("jdbc.user");
		String password = (String) config.getProperty("jdbc.password");
		String url = (String) config.getProperty("jdbc.url");
		url = url.substring(url.indexOf("//") + 2);
		String ipPort = url.split("/")[0];
		String ip = ipPort.split(":")[0];
		String port = ipPort.split(":")[1];
		String dbName = url.split("/")[1].substring(0,
				url.split("/")[1].indexOf("?"));
		String mysqldump = (String) cnf.getProperty("mysqldump.path");
		InputStream in = null;
		try {
			inv.getResponse().setContentType("application/force-download");
			inv.getResponse().setHeader("Content-Disposition",
					"attachment; filename=backup.sql");
			String command[] = new String[] { mysqldump, "--host=" + ip,
					"--port=" + port, "--user=" + user,
					"--password=" + password,
					dbName };
			ProcessBuilder pb = new ProcessBuilder(command);
			Process process = pb.start();
			in = process.getInputStream();
			org.apache.commons.io.IOUtils.copy(in, inv.getResponse()
					.getOutputStream());
			inv.getResponse().flushBuffer();
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally{
			if(in!=null){
				in.close();
			}
		}
	}
	
	public String initH2(){
//		JdbcTemplate jdbc = new JdbcTemplate(h2DS);
		return "@OK";
	}
	
	public String getSyncStatus(final Invocation inv,@Param("cmsId")String cmsId) throws ConfigurationException, ClassNotFoundException{
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/oracle_jdbc.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		PullDataTask.execute(config, cmsId, new IDoInConnection() {
			@Override
			public void execute(Connection conn) {
				JdbcTemplate jdbc = new JdbcTemplate(new SingleConnectionDataSource(conn, false));
				List<Map<String,Object>> organStatusRows = jdbc.queryForList("select count(*) count,sync from organ_status group by sync");
				List<Map<String,Object>> deviceStatusRows = jdbc.queryForList("select count(*) count,sync from device_status group by sync");
				List<Map<String,Object>> visStatusRows = jdbc.queryForList("select count(*) count,sync from vis_status group by sync");
				inv.addModel("organStatusRows", organStatusRows);
				inv.addModel("deviceStatusRows", deviceStatusRows);
				inv.addModel("visStatusRows", visStatusRows);
			}
		});
		return "init/sysc-status-json";
	}
	
	public String clearSyncStatus(final Invocation inv,@Param("cmsId")String cmsId) throws ConfigurationException, ClassNotFoundException{
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/oracle_jdbc.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		PullDataTask.execute(config, cmsId, new IDoInConnection() {
			@Override
			public void execute(Connection conn) {
				JdbcTemplate jdbc = new JdbcTemplate(new SingleConnectionDataSource(conn, false));
				jdbc.execute("delete from organ_status");
				jdbc.execute("delete from device_status");
				jdbc.execute("delete from vis_status");
				jdbc.execute("delete from user_status");
				jdbc.execute("delete from status");
				jdbc.execute("delete from monitor");
			}
		});
		return "@success";
	}
	
	public String triggerPush(final Invocation inv,@Param("cmsId")final String cmsId) throws ConfigurationException, ClassNotFoundException{
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/oracle_jdbc.properties"));
		final PropertiesConfiguration config = new PropertiesConfiguration(file);
		Runnable trigger = new Runnable() {
			public void run() {
				Map<String, String> map = new HashMap<String, String>();
				HttpclientUtils.accessVisGet(map, "/autoScanCtrl/organScan", ((String)config.getProperty("p"+cmsId+"_share.war.url")));
				HttpclientUtils.accessVisGet(map, "/autoScanCtrl/visScan", ((String)config.getProperty("p"+cmsId+"_share.war.url")));
				HttpclientUtils.accessVisGet(map, "/autoScanCtrl/deviceScan", ((String)config.getProperty("p"+cmsId+"_share.war.url")));
				HttpclientUtils.accessVisGet(map, "/autoScanCtrl/userScan", ((String)config.getProperty("p"+cmsId+"_share.war.url")));
			}
		};
		executorService.execute(trigger);
		return "@success";
	}
}
