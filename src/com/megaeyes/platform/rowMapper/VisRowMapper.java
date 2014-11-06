package com.megaeyes.platform.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.megaeyes.regist.bean.IDeviceServer;
import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.VisModel;
import com.megaeyes.regist.other.Status;

public class VisRowMapper implements RowMapper<IDeviceServer> {
	private JdbcTemplate jdbcTemplate;
	private static Map<String, VisModel> modelMap = new HashMap<String, VisModel>();
	private Set<String> columns = new HashSet<String>();
	private String cmsId;

	public VisRowMapper(Set<String> columns) {
		this.columns = columns;
	}

	@Override
	public IDeviceServer mapRow(ResultSet rs, int rownum) throws SQLException {       
		Timestamp changeTime = new Timestamp(System.currentTimeMillis());             
		int nanosecond = changeTime.getNanos() / 1000;                                
                                                                                      
		DeviceServer ds = new DeviceServer();                                         
		ds.setVisModel(getVisModel(rs.getString("video_input_server_model_id")));     
		ds.setManufacturer(ds.getVisModel().getName());                               
		ds.setModel(ds.getVisModel().getType());                                      
		ds.setCmsId(cmsId);                                                           
		ds.setServerId(rs.getString("id"));                                           
		ds.setIP(rs.getString("ip"));                                                 
		ds.setLocation(rs.getString("install_location"));                             
                                                                                      
		ds.setName(rs.getString("f_name"));                                           
		ds.setType("VIS");                                                            
		ds.setStatus(Status.add.name());                                              
		ds.setChangeTime(changeTime);                                                 
		ds.setNanosecond(nanosecond);                                                 
		ds.setSync(false);                                                            
		ds.setNaming(rs.getString("naming"));                                         
		ds.setInnerDevice(true);
																						
                                                                                      
		if (columns.contains("process_std_id")) {                                     
			ds.setStdId(rs.getString("process_std_id"));                              
		}                                                                             
		if (StringUtils.isBlank(ds.getStdId())) {                                     
			StringBuilder stdId = new StringBuilder();                                
			stdId.append(cmsId);                                                      
			stdId.append("0000111");                                                  
			String id = rs.getString("id");                                           
			stdId.append(id.substring(id.length()-7));                                
			ds.setStdId(stdId.toString());                                            
		}                                                                             

		return ds;

	}

	public VisRowMapper(JdbcTemplate jdbcTemplate, Set<String> columns,
			String cmsId) {
		this.jdbcTemplate = jdbcTemplate;
		this.columns = columns;
		this.cmsId = cmsId;
	}

	private VisModel getVisModel(String id) {
		VisModel model = modelMap.get(id);
		if (model == null) {
			model = jdbcTemplate.queryForObject(
					"select * from video_input_server_model where id=?",
					new Object[] { id }, new VisModelRowMapper());
			if (model != null) {
				if (modelMap.size() > 10000) {
					modelMap.clear();
				}
				modelMap.put(id, model);
			} else {
				model = new VisModel();
				model.setName("EYE-1001Y");
				model.setType("general_camera_video_server");
			}
		}
		return model;
	}

}
