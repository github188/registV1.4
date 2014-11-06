package com.megaeyes.regist.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.megaeyes.regist.domain.Authorization;

public class AuthorizationRowMapper implements RowMapper<Authorization>{
	@Override
	public Authorization mapRow(ResultSet rs, int arg1) throws SQLException {
		Authorization au = new Authorization();
		au.setResourceCmsId(rs.getInt("resource_cms_id"));
		au.setResourceId(rs.getInt("resource_id"));
		return au;
	}
}
