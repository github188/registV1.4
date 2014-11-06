package com.megaeyes.regist.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.megaeyes.regist.domain.SubscribeEvent;

public class SubscribeEventRowMapper implements RowMapper<SubscribeEvent>{
	@Override
	public SubscribeEvent mapRow(ResultSet rs, int arg1) throws SQLException {
		SubscribeEvent subscribeEvent = new SubscribeEvent();
//		subscribeEvent.setCmsId(rs.getString("cms_id"));
//		subscribeEvent.setDeviceId("devide_id");
//		subscribeEvent.setExpireDate(rs.getDate("expire_date"));
//		subscribeEvent.setFromPlatformId(rs.getString("from_platform_id"));
//		subscribeEvent.setId(rs.getInt("id"));
//		subscribeEvent.setOrganStdId(rs.getString("organ_std_id"));
//		subscribeEvent.setPath(rs.getString("path"));
//		subscribeEvent.setSubscribeId(rs.getString("subscribe_id"));
		return subscribeEvent;
	}
}
