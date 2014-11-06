package com.megaeyes.regist.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.megaeyes.regist.domain.DeviceServer;

public class DeviceServerRowMapper implements RowMapper<DeviceServer> {
	@Override
	public DeviceServer mapRow(ResultSet rs, int arg1) throws SQLException {
		DeviceServer server = new DeviceServer();
		server.setId(rs.getInt("id"));
		server.setServerId(rs.getString("server_id"));
		server.setLocation(rs.getString("location"));
		server.setName(rs.getString("name"));
		server.setNaming(rs.getString("naming"));
		server.setOrganId(rs.getInt("organ_id"));
		server.setStatus(rs.getString("status"));
		server.setCmsId(rs.getString("cms_id"));
		server.setStdId(rs.getString("std_id"));
		server.setType(rs.getString("type"));
		server.setManufacturer(rs.getString("manufacturer"));
		server.setModel(rs.getString("model"));
		return server;
	}
}
