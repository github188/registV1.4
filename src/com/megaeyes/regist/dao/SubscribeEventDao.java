package com.megaeyes.regist.dao;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.megaeyes.regist.domain.DeviceStatus;
import com.megaeyes.regist.domain.OrganStatus;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.sendResource.GBUtils;
import com.megaeyes.utils.Ar;

@Repository("subscribeEventDao")
public class SubscribeEventDao {

	@Autowired
	private GBUtils gbUtils;

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private DomainDao domainDao;

	public void notifyDelete(Timestamp changeTime, int nanosecond) {
		for (DeviceStatus status : getDeviceStatus(changeTime, nanosecond,
				Status.delete.name())) {
			gbUtils.notifyDevice(status, nanosecond);
		}
		for (OrganStatus status : getOrganStatus(changeTime, nanosecond,
				Status.delete.name())) {
			gbUtils.notifyOrgan(status, nanosecond);
		}

	}

	public void notifyUpdate(Timestamp changeTime, int nanosecond) {
		for (OrganStatus status : getOrganStatus(changeTime, nanosecond,
				Status.update.name())) {
			gbUtils.notifyOrgan(status, nanosecond);
		}
		for (DeviceStatus status : getDeviceStatus(changeTime, nanosecond,
				Status.update.name())) {
			gbUtils.notifyDevice(status, nanosecond);
		}
	}

	public void notifyAdd(Timestamp changeTime, int nanosecond) {
		for (OrganStatus status : getOrganStatus(changeTime, nanosecond,
				Status.add.name())) {
			gbUtils.notifyOrgan(status, nanosecond);
		}

		for (DeviceStatus status : getDeviceStatus(changeTime, nanosecond,
				Status.add.name())) {
			gbUtils.notifyDevice(status, nanosecond);
		}
	}

	private List<OrganStatus> getOrganStatus(Timestamp changeTime,
			int nanosecond, String status) {
		String sql = "select os.* from gb_share s inner join gb_organ o on("
				+ HomeDao.getOrganPathSQL(null, "s")
				+ " or "
				+ HomeDao.getDevicePathSQL(null, "s")
				+ ") inner join organ_status os on(os.organ_id=o.id and os.platform_id=s.platform_id) where os.base_notify is false and os.status=? and (os.change_time<? or (os.change_time=? and os.nanosecond<?))";
		if (status.equals("add")) {
			sql = sql + "order by os.organ_id";
		} else if (status.equals("delete")) {
			sql = sql + "order by os.organ_id desc";
		}
		List<OrganStatus> organStatusList = domainDao.find(OrganStatus.class,
				sql, status, changeTime, changeTime, nanosecond);
		return organStatusList;
	}

	private List<DeviceStatus> getDeviceStatus(Timestamp changeTime,
			int nanosecond, String status) {
		String sql = "select ds.* from gb_share s inner join gb_device d on("
				+ HomeDao.getOrganPathSQL("d", "s")
				+ " or (s.resource_type='device' and s.resource_id=d.id)) inner join device_status ds on(ds.device_id=d.id and ds.platform_id=s.platform_id) where ds.base_notify is false and ds.status=? and (ds.change_time<? or (ds.change_time=? and ds.nanosecond<?))";
		List<DeviceStatus> deviceStatusList = domainDao.find(
				DeviceStatus.class, sql, status, changeTime, changeTime,
				nanosecond);
		return deviceStatusList;
	}

	public void updateDeviceStatus(Timestamp changeTime, int nanosecond) {
		jdbc.update(
				"update device_status set online_notify=true where status!='delete' and online_notify_sign=? and online_notify=false and online_change_time<? or (online_change_time=? and online_nanosecond<=?)",
				nanosecond, changeTime, changeTime, nanosecond);
		Ar.exesql(
				"update device_status set base_notify=true where status!='delete' and base_notify_sign=? and base_notify=false and change_time<? or (change_time=? and nanosecond<=?)",
				nanosecond, changeTime, changeTime, nanosecond);
		jdbc.update(
				"delete from device_status where status='delete' and base_notify_sign=? and base_notify=false and change_time<? or (change_time=? and nanosecond<=?)",
				nanosecond, changeTime, changeTime, nanosecond);
	}

	public void updateOrganStatus(Timestamp changeTime, int nanosecond) {
		jdbc.update(
				"update organ_status set base_notify=true where status!='delete'  and base_notify_sign=? and base_notify=false and change_time<? or (change_time=? and nanosecond<=?)",
				nanosecond, changeTime, changeTime, nanosecond);
		jdbc.update(
				"delete from organ_status where status='delete' and base_notify_sign=?  and base_notify=false and change_time<? or (change_time=? and nanosecond<=?)",
				nanosecond, changeTime, changeTime, nanosecond);
	}

	public void notifyDeviceOnline(Timestamp changeTime, int nanosecond) {
		String sql = "select ds.* from gb_share s inner join gb_device d on("
				+ HomeDao.getOrganPathSQL("d", "s")
				+ " or (s.resource_type='device' and s.resource_id=d.id)) inner join device_status ds on(ds.device_id=d.id and ds.platform_id=s.platform_id) "
				+ " where ds.online_notify is false and ds.status!=? and (ds.online_change_time<? or (ds.online_change_time=? and ds.online_nanosecond<?)) ";
		List<DeviceStatus> deviceStatusList =domainDao.find(DeviceStatus.class,sql,Status.delete.name(), changeTime, changeTime,nanosecond);
		for (DeviceStatus status : deviceStatusList) {
			gbUtils.onlineNotify(status, nanosecond);
		}
	}

}
