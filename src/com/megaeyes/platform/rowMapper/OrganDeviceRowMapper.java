package com.megaeyes.platform.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.megaeyes.regist.bean.IDevice;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.other.DeviceType;
import com.megaeyes.regist.other.Status;

public class OrganDeviceRowMapper implements RowMapper<IDevice> {
	private Set<String> columns = new HashSet<String>();
	private String cmsId;

	public OrganDeviceRowMapper(Set<String> columns, String cmsId) {
		this.columns = columns;
		this.cmsId = cmsId;
	}

	@Override
	public IDevice mapRow(ResultSet rs, int rowNum) throws SQLException {
		Timestamp changeTime = new Timestamp(System.currentTimeMillis());
		int nanosecond = changeTime.getNanos() / 1000;
		Device pdevice = new Device();
		pdevice.setCmsId(cmsId);
		pdevice.setDeviceId(rs.getString("deviceid"));
		pdevice.setType(rs.getString("type"));
		pdevice.setLocation(rs.getString("location"));
		pdevice.setName(rs.getString("vname"));
		pdevice.setNaming(rs.getString("naming"));
		pdevice.setPath(rs.getString("path"));
		pdevice.setStatus(Status.add.name());
		pdevice.setChangeTime(changeTime);
		pdevice.setNanosecond(nanosecond);
		pdevice.setSync(false);
		if (columns.contains("gps_x")) {
			pdevice.setLongitude("" + rs.getDouble("gps_x"));
		}else{
			pdevice.setLongitude("0");
		}
		if (columns.contains("gps_y")) {
			pdevice.setLatitude("" + rs.getDouble("gps_y"));
		}else{
			pdevice.setLatitude("0");
		}
		if (columns.contains("gps_z")) {
			pdevice.setGpsZ("" + rs.getDouble("gps_z"));
		}else{
			pdevice.setGpsZ("0");
		}
		if (columns.contains("permission")) {
			pdevice.setPermission(getPermission(pdevice.getType(),rs.getString("permission")));
		}
		if (columns.contains("is_support_scheme")) {
			pdevice.setSupportScheme(rs.getBoolean("is_support_scheme"));
		}

		if (columns.contains("std_id")) {
			pdevice.setStdId(rs.getString("std_id"));
		}

		if (StringUtils.isBlank(pdevice.getStdId())) {
			StringBuilder stdId = new StringBuilder();
			stdId.append(cmsId);
			if (pdevice.getType().equals("VIC")) {
				stdId.append("0000131");
			} else if (pdevice.getType().equals("IPVIC")) {
				stdId.append("0000132");
			} else if (pdevice.getType().equals("AIC")) {
				stdId.append("0000133");
			}
			stdId.append(pdevice.getDeviceId().substring(pdevice.getDeviceId().length()-7));
			pdevice.setStdId(stdId.toString());
		}
		pdevice.setIptzType(0);
		pdevice.setIdircetionType(0);
		pdevice.setIuseType(0);
		pdevice.setIpositionType(0);
		pdevice.setIroomType(0);
		pdevice.setIsupplyLightType(0);
		return pdevice;
	}

	private static String getPermission(String type,String permission) {
		if (StringUtils.isNotBlank(permission)
				&& (type.equals(DeviceType.VIC.name()) || type
						.equals(DeviceType.IPVIC.name()))) {
			return "11" + permission.substring(1, 2);
		}
		return "";
	}
	
	public static void main(String[] args) {
		Double d = null;
		System.out.println(""+d);
	}
}
