package com.megaeyes.regist.utils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.hight.performance.annotation.Param;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.megaeyes.platform.rowMapper.OrganDeviceRowMapper;
import com.megaeyes.platform.rowMapper.POrganRowMapper;
import com.megaeyes.platform.rowMapper.VisRowMapper;
import com.megaeyes.regist.bean.IDevice;
import com.megaeyes.regist.bean.IDeviceServer;
import com.megaeyes.regist.bean.IOrgan;
import com.megaeyes.regist.bean.IPlatform;
import com.megaeyes.regist.other.RegistCallable;
import com.megaeyes.regist.utils.sendResource.ConfigUtil;

public class PullHelper {
	private ExecutorService executorService = Executors.newFixedThreadPool(10);
	private CompletionService<String> taskCompletionService = new ExecutorCompletionService<String>(
			executorService);
	private List<IDevice> devices = new ArrayList<IDevice>();
	private List<IOrgan> organs = new ArrayList<IOrgan>();
	private List<IDeviceServer> servers = new ArrayList<IDeviceServer>();
	private String cmsId;
	private String msg;
	
	private static String selfServiceUrl ="http://"+ConfigUtil.LOCAL_URL+"/services";
	public String index(String cmsId,Connection conn) throws SQLException {
		this.cmsId = cmsId;
		Statement stat = conn.createStatement();
		Map<String, Set<String>> columnMap = setColumns(cmsId,stat);
		JdbcTemplate jdbc = new JdbcTemplate(new SingleConnectionDataSource(conn, false));
		RegistCallable task = new RegistCallable(selfServiceUrl);
		pullPlatform(cmsId,columnMap,jdbc,task);
		pullUser(cmsId,columnMap,jdbc,task);
		pullOrgan(cmsId,columnMap,jdbc,task);
		pullServer(cmsId,columnMap,jdbc,task);
		pullDevice(cmsId,columnMap,jdbc,task);
		return "@success";
	}
	private static String userSQL = "select * from t_user t where t.organ_id is not null and t.IS_SUSPENDED=0";
	private void pullUser(String cmsId, Map<String, Set<String>> columnMap,JdbcTemplate jdbc,RegistCallable task) {
		List<Map<String,Object>> rows = jdbc.queryForList(userSQL);
		Timestamp changeTime = new Timestamp(System.currentTimeMillis());
		int nanosecond = changeTime.getNanos() / 1000;
		List<com.megaeyes.regist.bean.UserBean> users = new ArrayList<com.megaeyes.regist.bean.UserBean>();
		for(Map<String,Object> row: rows){
			users.add(getUserByRow(row, changeTime, nanosecond));
		}
		task.setClazz(new Class[] { String.class, List.class });
		task.setMethodStr("userRegist");
		task.setObjects(new Object[]{cmsId,users});
		taskCompletionService.submit(task);
		try {
			taskCompletionService.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private com.megaeyes.regist.bean.UserBean getUserByRow(Map<String,Object> row,Timestamp changeTime,int nanosecond){
		com.megaeyes.regist.bean.UserBean user = new com.megaeyes.regist.bean.UserBean();
		user.setId((String)row.get("id"));
		user.setOrganId((String)row.get("organ_id"));
		user.setName((String)row.get("f_name"));
		user.setLogonName((String)row.get("logon_name"));
		user.setNaming((String)row.get("naming"));
		user.setSex((String)row.get("sex"));
		user.setStatus("add");
		user.setPassword((String)row.get("f_password"));
		user.setChangeTime(changeTime);
		user.setNanosecond(nanosecond);
		user.setSync(false);
		return user;
	}

	public void pullPlatform(String cmsId,Map<String, Set<String>> columnMap,JdbcTemplate jdbc,RegistCallable task) {
		Map<String,Object> row= jdbc.queryForMap("select * from platform");
		IPlatform platform = new com.megaeyes.regist.domain.Platform();
		platform.setCmsId((String)row.get("cms_id"));
		platform.setName((String)row.get("f_name"));
		platform.setPassword((String)row.get("pqssword"));
		platform.setEventServerIp((String)row.get("event_server_ip"));
		platform.setEventServerPort(((BigDecimal)row.get("event_server_port")).intValue());
		
		task.setClazz(new Class[] { IPlatform.class });
		task.setMethodStr("regist");
		task.setObjects(new Object[] {platform});
		taskCompletionService.submit(task);
		try {
			taskCompletionService.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}


	private int pageSize = 5000;

	public void pullDevice(@Param("cmsId") String cmsId,Map<String, Set<String>> columnMap,JdbcTemplate jdbc,RegistCallable task) throws SQLException {
		int i = 0;
		task.setClazz(new Class[] { String.class, List.class });
		task.setMethodStr("deviceRegist");
		while (true) {
			List<IDevice> deviceList = jdbc
					.query("select * from (select rownum rnum,od.* from (select * from organ_devices where naming is not null and type in('VIC','IPVIC','AIC')) od where rownum<?) where rnum>?",
							new Object[] { (i + 1) * pageSize, i * pageSize },
							new OrganDeviceRowMapper(columnMap.get("organ_devices"), cmsId));
			if (deviceList.size() == 0) {
				break;
			}
			this.devices.addAll(deviceList);
			i++;
		}
	}
	

	public void pullServer(@Param("cmsId") String cmsId,Map<String, Set<String>> columnMap,JdbcTemplate jdbc,RegistCallable task) throws SQLException {
		task.setClazz(new Class[] { String.class, List.class });
		task.setMethodStr("visRegist");
		int i = 0;
		while (true) {
			List<IDeviceServer> servers = jdbc
					.query("select * from ( select rownum rnum,vis.* from (select * from video_input_server where naming is not null)vis where rownum<? ) where rnum>?",
							new Object[] { (i + 1) * pageSize, i * pageSize },
							new VisRowMapper(jdbc, columnMap.get("video_input_server"), cmsId));
			if (servers.size() == 0) {
				break;
			}
			this.servers.addAll(servers);
			i++;
		}
	}

	public void pullOrgan(@Param("cmsId") String cmsId,Map<String, Set<String>> columnMap,JdbcTemplate jdbc,RegistCallable task) throws SQLException {
		List<IOrgan> organList = jdbc.query("select * from organ ",
				new Object[] {}, new POrganRowMapper(columnMap.get("organ"),cmsId));
		this.organs.addAll(organList);
	}

	
	public Map<String, Set<String>> setColumns(@Param("cmsId") String cmsId, Statement st)
			throws SQLException {
		String[] tableNames = new String[] { "organ_devices", "organ","platform","t_user",
				"video_input_server" };
		Map<String, Set<String>> tableMap = new HashMap<String, Set<String>>();
		for (String tableName : tableNames) {

			ResultSet rset = st.executeQuery("SELECT * FROM " + tableName
					+ " where rownum<2");
			ResultSetMetaData md = rset.getMetaData();
			Set<String> set = new HashSet<String>();
			for (int i = 1; i <= md.getColumnCount(); i++) {
				set.add(md.getColumnLabel(i).toLowerCase());
			}
			tableMap.put(tableName, set);
		}
		return tableMap;
	}

	public List<IDevice> getDevices() {
		return devices;
	}

	public void setDevices(List<IDevice> devices) {
		this.devices = devices;
	}

	public List<IOrgan> getOrgans() {
		return organs;
	}

	public void setOrgans(List<IOrgan> organs) {
		this.organs = organs;
	}

	public List<IDeviceServer> getServers() {
		return servers;
	}

	public void setServers(List<IDeviceServer> servers) {
		this.servers = servers;
	}

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
