package com.megaeyes.platform.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.VisModel;

public class DeviceServerRowMapper implements RowMapper<DeviceServer>{
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private static  Map<String,VisModel> modelMap = new HashMap<String,VisModel>();
	@Override
	public DeviceServer mapRow(ResultSet rs, int rownum) throws SQLException {
		DeviceServer ds = new DeviceServer();
		ds.setServerId(rs.getString("id"));
		ds.setIP(rs.getString("ip"));
		ds.setLocation(rs.getString("install_location"));
		ds.setName(rs.getString("f_name"));
		ds.setType("VIS");
		ds.setStatus("add");
		ds.setVisModel(getVisModel(rs.getString("video_input_server_model_id")));
		
		return ds;
	}
	public DeviceServerRowMapper(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	private VisModel getVisModel(String id){
		VisModel model = modelMap.get(id);
		if(model == null){
			model = jdbcTemplate.queryForObject("select * from video_input_server_model where id=?",new Object[]{id},new VisModelRowMapper());
			if(model != null){
				if(modelMap.size()>10000){
					modelMap.clear();
				}
				modelMap.put(id, model);
			}else{
				model = new VisModel();
				model.setName("EYE-1001Y");
				model.setType("general_camera_video_server");
			}
		}
		return model;
	}
	
}
