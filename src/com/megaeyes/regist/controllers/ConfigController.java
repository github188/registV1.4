package com.megaeyes.regist.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.hight.performance.annotation.Param;
import net.hight.performance.filter.HPFilter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.megaeyes.utils.Invocation;

@Component("configManage")
public class ConfigController {

	private static String[] fileNames = new String[]{"jdbc.properties","config.properties","task.properties","developer.properties","log4j.properties","oracle_jdbc.properties","jdbc_init.properties"};
	
	public String getJdbcConfig(Invocation inv) throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/jdbc_init.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		inv.addModel("user", config.getProperty("jdbc.user"));
		inv.addModel("password", config.getProperty("jdbc.password"));
		String url = (String) config.getProperty("jdbc.url");
		url = url.substring(url.indexOf("//") + 2);
		String ipPort = url.split("/")[0];
		String dbName = url.split("/")[1].substring(0,
				url.split("/")[1].indexOf("?"));
		inv.addModel("dbName", dbName);
		inv.addModel("ipPort", ipPort);
		return "init/config/jdbc-properties";
	}

	public String getTaskConf(Invocation inv) throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/task.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		inv.addModel("config", config);
		return "init/config/task-properties";
	}

	public String updateJdbcConfig(Invocation inv, @Param("user") String user,
			@Param("password") String password, @Param("ipPort") String ipPort,
			@Param("dbName") String dbName) throws ConfigurationException {
		File initFile = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/jdbc_init.properties"));
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/jdbc.properties"));
		PropertiesConfiguration initConfig = new PropertiesConfiguration(
				initFile);
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		config.setProperty("jdbc.user", user);
		config.setProperty("jdbc.password", password);
		StringBuilder url = new StringBuilder("jdbc:mysql://");
		url.append(ipPort);
		url.append("/");
		url.append(dbName);
		url.append("?useUnicode=true&characterEncoding=GBK");
		config.setProperty("jdbc.url", url.toString());
		config.setProperty("jdbc.driverClassName",
				initConfig.getProperty("jdbc.driverClassName"));

		initConfig.setProperty("jdbc.user", user);
		initConfig.setProperty("jdbc.password", password);
		initConfig.setProperty("jdbc.url", config.getProperty("jdbc.url"));

		Connection conn = null;
		Statement statement = null;
		try {
			Class.forName((String) config.getProperty("jdbc.driverClassName"));
			conn = DriverManager.getConnection("jdbc:mysql://" + ipPort + "/",
					user, password);
			statement = conn.createStatement();
			statement.executeUpdate("create database " + dbName
					+ " charset gbk");
			System.out.println("Database created!");
			conn.close();
		} catch (SQLException e) {
			if (e.getErrorCode() == 1007) {
				// Database already exists error
				System.out.println(e.getMessage());
			} else {
				// Some other problems, e.g. Server down, no permission, etc
				e.printStackTrace();
				return "@error";
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "@error";
		} catch (Exception e) {
			e.printStackTrace();
			return "@error";
		} finally {
			try {
				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		initConfig.save();
		config.save();
		return "@success";
	}
	
	public void exportJdbcConf(Invocation inv, @Param("user") String user,
			@Param("password") String password, @Param("ipPort") String ipPort,
			@Param("dbName") String dbName) throws ConfigurationException {
		File initFile = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/jdbc_init.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration();
		PropertiesConfiguration initConfig = new PropertiesConfiguration(
				initFile);
		config.setProperty("jdbc.user", user);
		config.setProperty("jdbc.password", password);
		StringBuilder url = new StringBuilder("jdbc:mysql://");
		url.append(ipPort);
		url.append("/");
		url.append(dbName);
		url.append("?useUnicode=true&characterEncoding=GBK");
		config.setProperty("jdbc.url", url.toString());
		config.setProperty("jdbc.driverClassName",
				initConfig.getProperty("jdbc.driverClassName"));
		try {
			inv.getResponse().setContentType("application/force-download");
			inv.getResponse().setHeader("Content-Disposition",
					"attachment; filename=jdbc.properties");
			config.save(inv.getResponse()
					.getOutputStream());
			inv.getResponse().flushBuffer();
		} catch (IOException ex) {
			throw new RuntimeException("IOError writing file to output stream");
		}

		
	}

	public String getRegistConf(Invocation inv) throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/config.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		inv.addModel("config", config);
		return "init/config/config-properties";
	}

	public String getLog4jConf(Invocation inv) throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/log4j.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		List<String> keys = new ArrayList<String>();
		Iterator<String> iterator = config.getKeys();
		while (iterator.hasNext()) {
			keys.add(iterator.next());
		}
		inv.addModel("keys", keys);
		inv.addModel("config", config);
		return "init/config/log4j-properties";
	}
	
	public String getDeveloperConf(Invocation inv) throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/developer.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		List<String> keys = new ArrayList<String>();
		Iterator<String> iterator = config.getKeys();
		while (iterator.hasNext()) {
			keys.add(iterator.next());
		}
		inv.addModel("keys", keys);
		inv.addModel("config", config);
		return "init/config/developer-properties";
	}

	public String updateRegistConf(Invocation inv,
			@Param("parent_regist_url") String parent_regist_url,
			@Param("is_city_regist") String is_city_regist,
			@Param("local_url") String local_url,
			@Param("owner_platform_url") String owner_platform_url,
			@Param("owner_platform_cmsId") String owner_platform_cmsId,
			@Param("owner_platform_gbId") String owner_platform_gbId,
			@Param("subscribe_period") String subscribe_period,
			@Param("recordSendLog") String recordSendLog,
			@Param("recordSendLogDir") String recordSendLogDir,
			@Param("username") String username,
			@Param("password") String password,
			@Param("access_server_ip") String access_server_ip,
			@Param("mysqldump_path") String mysqldumpPath)
			throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/config.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		config.setProperty("parent_regist_url", parent_regist_url);
		config.setProperty("is_city_regist", is_city_regist);
		config.setProperty("local_url", local_url);
		config.setProperty("owner_platform_url", owner_platform_url);
		config.setProperty("owner_platform_cmsId", owner_platform_cmsId);
		config.setProperty("owner_platform_gbId", owner_platform_gbId);
		config.setProperty("subscribe_period", subscribe_period);
		config.setProperty("recordSendLog", recordSendLog);
		config.setProperty("recordSendLog.dir", recordSendLogDir);
		config.setProperty("access_server_ip", access_server_ip);
		config.setProperty("username", username);
		config.setProperty("password", password);
		config.setProperty("mysqldump.path", mysqldumpPath);
		config.save();
		return "@success";
	}

	public String updateTaskConf(
			Invocation inv,
			@Param("monitor_start_delay") String monitor_start_delay,
			@Param("monitor_repeat_interval") String monitor_repeat_interval,
			@Param("initAccessIp_start_delay") String initAccessIp_start_delay,
			@Param("flushStatus_start_delay") String flushStatus_start_delay,
			@Param("flushStatus_repeat_interval") String flushStatus_repeat_interval,
			@Param("initSubscribe_start_delay") String initSubscribe_start_delay)
			throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/task.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		config.setProperty("monitor_start_delay", monitor_start_delay);
		config.setProperty("monitor_repeat_interval", monitor_repeat_interval);
		config.setProperty("initAccessIp_start_delay", initAccessIp_start_delay);
		config.setProperty("flushStatus_repeat_interval",
				flushStatus_repeat_interval);
		config.setProperty("initSubscribe_start_delay",
				initSubscribe_start_delay);
		config.setProperty("flushStatus_start_delay", flushStatus_start_delay);
		config.save();
		return "@success";
	}

	public String updateLog4jConf(Invocation inv) throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/log4j.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		Iterator<String> iterator = config.getKeys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			config.setProperty(key, inv.getRequest().getParameter(key).replaceAll("\\[", "").replaceAll("\\]", ""));
		}
		config.save();
		return "@success";
	}
	public String updateDeveloperConf(Invocation inv) throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/developer.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		Iterator<String> iterator = config.getKeys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			config.setProperty(key, inv.getRequest().getParameter(key).replaceAll("\\[", "").replaceAll("\\]", ""));
		}
		config.save();
		return "@success";
	}
	
	public String forImportMyconf(Invocation inv) throws ConfigurationException {
		return "init/import-myconf";
	}
	
	public String importMyconf(Invocation inv,@Param("file") MultipartFile myconf) throws ConfigurationException, IllegalStateException, IOException {
		if(myconf != null){
			File myconfigFile = new File("myconf.properties");
			myconf.transferTo(myconfigFile);
			inv.addModel("size", myconf.getSize());
			inv.addModel("contentType", myconf.getContentType());
			inv.addModel("name", myconf.getOriginalFilename());
			PropertiesConfiguration myconfig = new PropertiesConfiguration(myconfigFile);
			for(String fileName: fileNames){
				saveConf(fileName, myconfig);
			}
			
			saveOracleConfs(myconfig);
			
			myconfigFile.delete();
		}
		return "init/import-result";
	}
	
	private void saveOracleConfs(PropertiesConfiguration myconfig) throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext().getRealPath("WEB-INF/oracle_jdbc.properties"));
		PropertiesConfiguration conf = new PropertiesConfiguration(file);
		Iterator<String> keys = myconfig.getKeys();
		while(keys.hasNext()){
			String key = keys.next();
			if(key.startsWith("p")){
				conf.setProperty(key, myconfig.getProperty(key));
			}
		}
		conf.setProperty("cmsIds", myconfig.getProperty("cmsIds"));
		conf.save();
	}

	public void exportMyconf(Invocation inv) throws ConfigurationException, IllegalStateException, IOException {
		PropertiesConfiguration myconfig = new PropertiesConfiguration();
		for(String fileName: fileNames){
			setMyconf(fileName, myconfig);
		}
		try {
			inv.getResponse().setContentType("application/force-download");
			inv.getResponse().setHeader("Content-Disposition",
					"attachment; filename=myconf.properties");
			myconfig.save(inv.getResponse()
					.getOutputStream());
			inv.getResponse().flushBuffer();
		} catch (IOException ex) {
			throw new RuntimeException("IOError writing file to output stream");
		}
	}
	
	private void saveConf(String fileName,PropertiesConfiguration myconfig) throws ConfigurationException{
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/"+fileName));
		PropertiesConfiguration conf = new PropertiesConfiguration(file);
		Iterator<String> keys = conf.getKeys();
		while(keys.hasNext()){
			String key = keys.next();
			conf.setProperty(key, myconfig.getProperty(key));
		}
		conf.save();
	}
	
	private void setMyconf(String fileName,PropertiesConfiguration myconfig) throws ConfigurationException{
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/"+fileName));
		PropertiesConfiguration conf = new PropertiesConfiguration(file);
		Iterator<String> keys = conf.getKeys();
		while(keys.hasNext()){
			String key = keys.next();
			myconfig.setProperty(key, conf.getProperty(key));
		}
	}
	
	public String deleteMyconf(Invocation inv) throws ConfigurationException, IllegalStateException, IOException {
		File file = new File("myconf.properties");
		file.deleteOnExit();
		return "@success";
	}
	
	public void getcmsIds(Invocation inv) throws ConfigurationException{
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/oracle_jdbc.properties"));
		PropertiesConfiguration conf = new PropertiesConfiguration(file);
		Iterator<String> keys = conf.getKeys();
		Set<String> cmsIds = new HashSet<String>();
		while(keys.hasNext()){
			String key = keys.next();
			cmsIds.add(key.substring(1,7));
		}
		StringBuilder str = new StringBuilder();
		for(String cmsId:cmsIds){
			str.append(cmsId).append(",");
		}
		System.out.println(str.toString());
	}
}
