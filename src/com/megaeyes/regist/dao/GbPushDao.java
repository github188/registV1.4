package com.megaeyes.regist.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.GbOrgan;
import com.megaeyes.regist.utils.sendResource.GbDomainMapFactory;

@Component("gbPushDao")
public class GbPushDao {
	private static int pageSize = 5000;
	@Autowired
	private JdbcTemplate jdbc;
	@Autowired
	private DomainDao domainDao;

	public static String countOrganSQL = "from gb_share s inner join gb_organ o on(o.suspend=false and s.platform_id=?  and ("
			+ HomeDao.getOrganPathSQL(null, "s")
			+ " or "
			+ HomeDao.getDevicePathSQL(null, "s") + "))";
	public static String countDeviceSQL = "from gb_share s inner join gb_device d on(d.suspend=false and s.platform_id=? and ("
			+ HomeDao.getDeviceByOrganPathSQL("d", "s")
			+ " or (s.resource_type='device' and s.resource_id=d.id)))";

	public static String countOrganByParentSQL = "from gb_share s inner join gb_organ o on(o.suspend=false and s.platform_id=?  and o.path like ? and ("
			+ HomeDao.getOrganPathSQL(null, "s")
			+ " or "
			+ HomeDao.getDevicePathSQL(null, "s") + "))";
	public static String countDeviceByOrganSQL = "from gb_share s inner join gb_device d on(d.suspend=false and s.platform_id=?  and d.path like ? and ( "
			+ HomeDao.getDeviceByOrganPathSQL("d", "s")
			+ " or (s.resource_type='device' and s.resource_id=d.id)))";

	public Integer getOrganCounts(Integer platformId) {
		String sql = "select count(distinct(o.id)) " + countOrganSQL;
		return jdbc.queryForInt(sql, new Object[] { platformId });
	}

	public Integer getDeviceCounts(Integer platformId) {
		String sql = "select count(distinct(d.id)) " + countDeviceSQL;
		return jdbc.queryForInt(sql, new Object[] { platformId });
	}

	public Integer getOrganCountsByParent(Integer platformId, String path) {
		String sql = "select count(distinct(o.id)) " + countOrganByParentSQL;
		return jdbc.queryForInt(sql, new Object[] { platformId, path + "/%" });
	}

	public Integer getDeviceCountsByOrgan(Integer platformId, String path) {
		String sql = "select count(distinct(d.device_id)) " + countDeviceByOrganSQL;
		return jdbc.queryForInt(sql, new Object[] { platformId, path + "/%" });
	}

	public List<Integer> getOrgans(Integer platformId, int pageNO) {
		String sql = "select distinct(o.id) " + countOrganSQL
				+ " order by o.path limit ?,?";
		return jdbc.queryForList(sql, new Object[] { platformId,
				(pageNO - 1) * pageSize, pageSize }, Integer.class);
	}

	public List<Device> getDevices(Integer platformId, int pageNO) {
		String sql = "select distinct(d.id) as d_id "
				+ countDeviceSQL + " limit ?,?";
		List<Integer> ids = jdbc.queryForList(sql, new Object[] {
				platformId, (pageNO - 1) * pageSize, pageSize },Integer.class);
		return getDevicesByGbDeviceIds(ids);
	}

	public GbOrgan getGbOrganByStdId(String stdId) {
		String sql = "select gb.* from gb_organ gb inner join organ o on(gb.source_id=o.id and gb.source_type='organ' and o.std_id=?) union "
				+ " select gb.* from gb_organ gb inner join platform p on(gb.source_id=p.id and gb.source_type='platform' and p.gb_platform_cms_id=?)";
		GbOrgan organ = domainDao.one(GbOrgan.class, sql, new Object[] { stdId,
				stdId });
		return organ;
	}

	public String getSumNum(Integer platformId) {
		return "" + (getOrganCounts(platformId) + getDeviceCounts(platformId));
	}

	public String getSumNumByOrgan(Integer platformId, GbOrgan organ) {
		return ""
				+ (getOrganCountsByParent(platformId, organ.getPath()) + getDeviceCountsByOrgan(
						platformId, organ.getPath()));
	}

	public List<Integer> getOrganByParent(GbOrgan organ, Integer platformId,
			int pageNO) {
		String sql = "select distinct(o.source_id) " + countOrganByParentSQL
				+ " order by o.path limit ?,?";
		return jdbc.queryForList(sql,
				new Object[] { platformId, organ.getPath() + "%",
						(pageNO - 1) * pageSize, pageSize }, Integer.class);
	}

	public List<Device> getDevicesByOrgan(GbOrgan organ, Integer platformId,
			int pageNO) {
		String sql = "select distinct(d.id) as d_id "
				+ countDeviceByOrganSQL + " limit ?,?";
		List<Integer> ids = jdbc.queryForList(sql, new Object[] {
				platformId, organ.getPath() + "%", (pageNO - 1) * pageSize,
				pageSize },Integer.class);
		return getDevicesByGbDeviceIds(ids);
	}

	private List<Device> getDevicesByGbDeviceIds(List<Integer> gbDeviceIds) {
		List<Device> devices = new ArrayList<Device>();
		for (Integer id : gbDeviceIds) {
			devices.add(GbDomainMapFactory.getInstance().getDeviceForPush(id));
		}
		return devices;
	}
	
	
}
