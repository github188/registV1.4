package com.megaeyes.regist.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.megaeyes.regist.domain.Organ;

public class OrganRowMapper implements RowMapper<Organ>{
	@Override
	public Organ mapRow(ResultSet rs, int arg1) throws SQLException {
		Organ organ = new Organ();
		organ.setId(rs.getInt("id"));
		organ.setCmsId(rs.getString("cms_id"));
		organ.setName(rs.getString("name"));
		organ.setOrganId(rs.getString("organ_id"));
		organ.setParentOrganId(rs.getString("parent_id"));
		organ.setPath(rs.getString("path"));
		organ.setStatus(rs.getString("status"));
		organ.setStdId(rs.getString("std_id"));
		organ.setType(rs.getString("type"));
		organ.setBlock(rs.getString("block"));
		return organ;
	}
}
