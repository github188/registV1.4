package com.megaeyes.regist.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.utils.Ar;

public class OrganDevicesRowMapper implements RowMapper<Device>{
	private Map<Organ,List<Device>> organDeviceMap;
	@Override
	public Device mapRow(ResultSet rs, int arg1) throws SQLException {
		Device device = new Device();
		String path = rs.getString("d_path");
		device.setId(rs.getInt("d_id"));
		device.setDeviceId(rs.getString("device_id"));
		device.setType(rs.getString("type"));
		device.setLocation(rs.getString("location"));
		device.setName(rs.getString("d_name"));
		device.setNaming(rs.getString("naming"));
		device.setPath(path);
		device.setOwnerId(path.substring(path.lastIndexOf("/") + 1));
		device.setPermission(rs.getString("permission"));
		device.setSupportScheme(rs.getBoolean("support_scheme"));
		device.setLatitude(rs.getString("latitude"));
		device.setLongitude(rs.getString("longitude"));
		device.setGpsZ(rs.getString("gps_z"));
		device.setCmsId(rs.getString("d_cms_id"));
		device.setServerId(rs.getString("server_id"));
		
		Organ organ = new Organ();
		organ.setCmsId(device.getCmsId());
		organ.setName(rs.getString("o_name"));
		organ.setOrganId(rs.getString("o_organ_id"));
		organ.setParentOrganId(rs.getString("o_parent_id"));
		organ.setPath(rs.getString("o_path"));
		this.organDeviceMap.get(organ).add(device);
		return device;
	}
	public OrganDevicesRowMapper(Map<Organ,List<Device>> organDeviceMap) {
		this.organDeviceMap = organDeviceMap;
	}
}
