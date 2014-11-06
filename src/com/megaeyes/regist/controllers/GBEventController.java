package com.megaeyes.regist.controllers;

import java.util.Calendar;

import net.hight.performance.annotation.Param;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.bean.Subscribed;
import com.megaeyes.regist.dao.DomainDao;
import com.megaeyes.regist.domain.GbOrgan;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.SubscribeEvent;
import com.megaeyes.regist.exception.SubscribeException;
import com.megaeyes.regist.utils.MyExecutors;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("gbEvent")
public class GBEventController {

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private DomainDao domainDao;

	private static final MyExecutors exec = new MyExecutors(20);

	public String subscribe(Invocation inv, @Param("message") String message,
			@Param("fromPlatformId") String fromPlatformId,
			@Param("expire") String expire,
			@Param("subscribeId") final String subscribeId) {
		final Subscribed subscribe = Subscribed.getInstance(message);
		final GbPlatform gbPlatform = Ar.of(GbPlatform.class).one("cmsId",
				fromPlatformId);
		boolean isInitialSubscribe = false;
		try {
			SubscribeEvent subscribeEvent = Ar.of(SubscribeEvent.class).one(
					"from SubscribeEvent where platformId=? and deviceId=?",
					gbPlatform.getId(), subscribe.getDeviceID());
			if (expire.equals("0")) {
				// 上级取消订阅
				if (subscribeEvent != null) {
					jdbc.update(
							"delete from device_status where platform_id=? and subscribe_event_id=?",
							gbPlatform.getId(), subscribeEvent.getId());
					jdbc.update(
							"delete from organ_status where platform_id=? and subscribe_event_id=?",
							gbPlatform.getId(), subscribeEvent.getId());
					Ar.delete(subscribeEvent);
				}
			} else {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.SECOND, Integer.parseInt(expire));
				if (subscribeEvent == null) {
					// 初始订阅,生成订阅数据，包括生成device_status,organ_status,subscribe_event数据，上传处于订阅状态下的不在线设备
					isInitialSubscribe = true;
					GbOrgan organ = domainDao
							.one(GbOrgan.class,
									"select go.* from gb_organ go inner join organ o on(go.source_id=o.id and go.source_type='organ' and o.std_id=?) union select go.* from gb_organ go inner join platform p on(go.source_id=p.id and go.source_type='platform' and p.gb_platform_cms_id=?)",
									new Object[] { subscribe.getDeviceID(),
											subscribe.getDeviceID() });
					if (organ != null) {
						subscribeEvent = createSubscribeEvent(organ,
								gbPlatform.getId(), expire, subscribeId,
								subscribe);
						Ar.flush();
						jdbc.update(
								"insert ignore into device_status (base_notify,change_time,nanosecond,online,online_change_time,online_nanosecond,online_notify,status,device_id,platform_id,name,location,subscribe_event_id) select true,d.change_time,d.nanosecond,d.online,d.change_time,d.nanosecond,true,'normal',gbd.id,?,d.name,d.location,? from device d inner join gb_device gbd on(d.id=gbd.device_id and (gbd.path like ? or gbd.path=?))",
								new Object[] { subscribeEvent.getPlatformId(),
										subscribeEvent.getId(),
										organ.getPath() + "/%",organ.getPath() });
						jdbc.update(
									"insert ignore into organ_status (base_notify,change_time,nanosecond,status,organ_id,platform_id,name,block,subscribe_event_id) select true,o.change_time,o.nanosecond,'normal',gbo.id,?,o.name,o.block,? from organ o inner join gb_organ gbo on(o.id=gbo.source_id and (gbo.path like ? or gbo.path=?))",
									new Object[] { subscribeEvent.getPlatformId(),
											subscribeEvent.getId(),
											organ.getPath() + "/%",organ.getPath() });
					} else {
						// 没找到相关的机构，订阅失败
						inv.addModel("subscribe", subscribe);
						inv.addModel("result", "ERROR");
						return "/GB/subcribe-result";
					}

				} else {
					// 上级订阅刷新
					subscribeEvent.setExpireDate(cal.getTime());
					Ar.update(subscribeEvent);
					Ar.flush();
				}
			}
			inv.addModel("subscribe", subscribe);
			inv.addModel("result", "OK");
		} catch (SubscribeException e) {
			inv.addModel("result", "ERROR");
		} catch (Exception e) {
			inv.addModel("result", "ERROR");
			e.printStackTrace();
		}
		if (isInitialSubscribe) {
			try {
				Runnable target = new Runnable() {
					@Override
					public void run() {
//						 notifyInitialSubscribe(gbPlatform);
					}
				};
				exec.execute(target);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "/GB/subcribe-result";
	}

	private SubscribeEvent createSubscribeEvent(GbOrgan organ,
			Integer platformId, String expire, String subscribeId,
			Subscribed subscribe) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, Integer.parseInt(expire));
		SubscribeEvent subscribeEvent = new SubscribeEvent();
		subscribeEvent.setPath(organ.getPath());
		subscribeEvent.setExpireDate(cal.getTime());
		subscribeEvent.setDeviceId(subscribe.getDeviceID());
		subscribeEvent.setPlatformId(platformId);
		subscribeEvent.setSubscribeId(subscribeId);
		subscribeEvent.setOrganId(organ.getId());
		Ar.save(subscribeEvent);
		return subscribeEvent;
	}
}
