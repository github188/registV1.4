package com.megaeyes.regist.tasks;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.megaeyes.regist.utils.PullHelper;

public class PullDataTask implements Callable<PullHelper> {
	private static String oralceDriverName = "oracle.jdbc.driver.OracleDriver";
	private PropertiesConfiguration config;
	private String cmsId;

	@Override
	public PullHelper call() throws Exception {
		PullHelper pull = new PullHelper();
		execute(config, cmsId, pull);
		return pull;
	}

	public PropertiesConfiguration getConfig() {
		return config;
	}

	public void setConfig(PropertiesConfiguration config) {
		this.config = config;
	}

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}

	public void execute(PropertiesConfiguration config, String cmsId,
			PullHelper pull) throws ClassNotFoundException {
		Connection connection = null;
		try {
			connection = getOracleConn(config, cmsId, connection);
			if (connection != null) {
				pull.index(cmsId, connection);
				pull.setMsg("ok");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			pull.setCmsId(cmsId);
			pull.setMsg("error");
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void execute(PropertiesConfiguration config, String cmsId,IDoInConnection callback) throws ClassNotFoundException {
		Connection connection = null;
		try {
			connection = getOracleConn(config, cmsId, connection);
			if (connection != null) {
				callback.execute(connection);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static Connection getOracleConn(PropertiesConfiguration config, String cmsId,Connection connection) throws ClassNotFoundException, SQLException{
		String prefix = "p" + cmsId;
		String IPPORT = (String) config
				.getProperty(prefix + "_jdbc.IPPORT");
		String user = (String) config.getProperty(prefix + "_jdbc.user");
		String password = (String) config.getProperty(prefix
				+ "_jdbc.password");
		String sid = (String) config.getProperty(prefix + "_jdbc.sid");
		return getOracleConn(connection, IPPORT, sid, user, password);
	}
	

	public static Connection getOracleConn(Connection connection, String IPPORT,
			String sid, String user, String password)
			throws ClassNotFoundException, SQLException {
		Class.forName(oralceDriverName);
		StringBuilder url = new StringBuilder("jdbc:oracle:thin:@");
		url.append(IPPORT).append(":").append(sid);
		connection = DriverManager
				.getConnection(url.toString(), user, password);
		return connection;
	}
}
