package com.megaeyes.platform.rowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.megaeyes.regist.bean.IOrgan;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.other.Status;

public class POrganRowMapper implements RowMapper<IOrgan> {
	private String cmsId;

	@Override
	public IOrgan mapRow(ResultSet rs, int arg1) throws SQLException {
		Timestamp changeTime = new Timestamp(System.currentTimeMillis());
		int nanosecond = changeTime.getNanos() / 1000;
		Organ organ = new Organ();
		organ.setCmsId(cmsId);

		organ.setOrganId(rs.getString("id"));
		organ.setName(rs.getString("f_name"));
		organ.setParentOrganId(rs.getString("parent_organ_id") == null ? null
				: rs.getString("parent_organ_id"));
		organ.setPath(rs.getString("path"));
		organ.setStatus(Status.add.name());
		organ.setChangeTime(changeTime);
		organ.setNanosecond(nanosecond);
		organ.setSync(false);
		if (columns.contains(("process_std_id"))) {
			organ.setStdId(rs.getString("process_std_id"));
		}
		if (StringUtils.isBlank(organ.getStdId())) {
			StringBuilder stdId = new StringBuilder();
			stdId.append(cmsId);
			if(StringUtils.isNotBlank(organ.getParentOrganId())){
				stdId.append("0000216");
			}else{
				stdId.append("0000215");
			}
			stdId.append(organ.getOrganId().substring(
					organ.getOrganId().length() - 7));
			organ.setStdId(stdId.toString());
		}
		return organ;

	}

	private Set<String> columns = new HashSet<String>();

	public POrganRowMapper(Set<String> columns, String cmsId) {
		this.columns = columns;
		this.cmsId = cmsId;
	}
}
