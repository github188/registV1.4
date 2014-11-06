package com.megaeyes.regist.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.megaeyes.regist.bean.ResourceStatusBean;
import com.megaeyes.regist.domain.GbPlatform;

public class ResourceStatusBeanRowMapper implements
		RowMapper<ResourceStatusBean> {
	private GbPlatform platform;

	@Override
	public ResourceStatusBean mapRow(ResultSet rs, int arg1)
			throws SQLException {
		ResourceStatusBean bean = new ResourceStatusBean();
		bean.setPlatform(platform);
		bean.setStatusId(rs.getInt("status_id"));
		bean.setResourceId(rs.getString("resource_id"));
		bean.setResourceType(rs.getString("resource_type"));
		return bean;
	}

	public ResourceStatusBeanRowMapper(GbPlatform platform) {
		this.platform = platform;
	}

	public ResourceStatusBeanRowMapper() {
	}

}
