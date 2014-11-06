package com.megaeyes.platform.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.megaeyes.regist.domain.VisModel;

public class VisModelRowMapper implements RowMapper<VisModel>{
	@Override
	public VisModel mapRow(ResultSet rs, int rownum) throws SQLException {
		VisModel model = new VisModel();
		model.setId(rs.getString("id"));
		model.setEncode(rs.getString("encode"));
		model.setName(rs.getString("f_name"));
		model.setNote(rs.getString("note"));
		model.setOptimisticLock(rs.getLong("optimistic_lock"));
		model.setType(rs.getString("f_type"));
		return model;
	}
}
