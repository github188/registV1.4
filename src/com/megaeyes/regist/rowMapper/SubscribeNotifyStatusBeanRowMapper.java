package com.megaeyes.regist.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.megaeyes.regist.bean.SubscribeNotifyStatusBean;
import com.megaeyes.regist.domain.SubscribeEvent;

public class SubscribeNotifyStatusBeanRowMapper implements RowMapper<SubscribeNotifyStatusBean>{
	@Override
	public SubscribeNotifyStatusBean mapRow(ResultSet rs, int rowNum) throws SQLException {
		SubscribeNotifyStatusBean bean = new SubscribeNotifyStatusBean();
		SubscribeEvent event = new SubscribeEvent();
//		event.setCmsId(rs.getString("cms_id"));
//		event.setExpireDate(rs.getDate("expire_date"));
//		event.setDeviceId(rs.getString("s_device_id"));
//		event.setFromPlatformId(rs.getString("from_platform_id"));
//		event.setId(rs.getInt("s_id"));
//		event.setPath(rs.getString("s_path"));
//		event.setSubscribeId(rs.getString("subscribe_id"));
//		event.setOrganStdId(rs.getString("organ_std_id"));
//		bean.setSubscribeEvent(event);
		return bean;
	}
}
