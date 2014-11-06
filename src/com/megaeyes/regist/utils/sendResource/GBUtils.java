package com.megaeyes.regist.utils.sendResource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.bean.SubscribeEntity;
import com.megaeyes.regist.bean.Subscribed;
import com.megaeyes.regist.bean.sendResource.NotifyEntity;
import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.controllers.AutoScanController;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.DeviceStatus;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.OrganStatus;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.domain.ServerStatus;
import com.megaeyes.regist.domain.SubscribeEvent;
import com.megaeyes.regist.tasks.SendResourceTask;
import com.megaeyes.regist.tasks.SendToAccessTask;
import com.megaeyes.regist.utils.HttpclientUtils;
import com.megaeyes.regist.webservice.PlatformUtils;
import com.megaeyes.utils.Ar;

@Component("GBUtils")
public class GBUtils {
	@Value("${self_platform_cms_id}")
	private String selfPlatformCmsId;

	@Value("${child_platform_regist_url}")
	private String childPlatformRegistUrl;

	@Value("${nosip}")
	private boolean nosip;

	@Value("${parent_platform_regist_url}")
	private String parentPlatformRegistUrl;

	@Value("${sendToSip}")
	private boolean sendToSip;

	@Value("${recordSendLog}")
	private boolean recordSendLog;

	@Value("${recordSendLog.dir}")
	private String recordSendLogDir;
	
	private ExecutorService executorService = Executors.newFixedThreadPool(100);

	public void sendSubscribe(String deviceID, String sn, String period,
			GbPlatform gbPlatform) {
		SubscribeEntity entity = new SubscribeEntity(deviceID, sn);
		Map<String, String> map = new HashMap<String, String>();
		map.put("message", PlatformUtils.getContent(entity.getQueryDoc()));
		map.put("expire", period.equals("-1")?"0":period);
		map.put("toPlatformId", gbPlatform.getCmsId());
		HttpPost outerDeviceByBGPost;
		if (nosip) {
			map.put("fromPlatformId", selfPlatformCmsId);
			map.put("subscribeId", "0000001");
			outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(map,
					childPlatformRegistUrl, "/regist/gbEvent/subscribe");
		} else {
			outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(map,
					gbPlatform.getSipServer(), "/access_server/subscribe");
		}

		HttpclientUtils.execute(map, outerDeviceByBGPost);
	}

	public void proxySendSubscribe(Subscribed subscribed, String period,
			GbPlatform gbPlatform) {
		SubscribeEntity entity = new SubscribeEntity(subscribed.getDeviceID(),
				subscribed.getSN());
		Map<String, String> map = new HashMap<String, String>();
		map.put("message", PlatformUtils.getContent(entity.getQueryDoc()));
		map.put("expire", period);
		map.put("toPlatformId", gbPlatform.getCmsId());

		HttpPost outerDeviceByBGPost;
		if (nosip) {
			map.put("fromPlatformId", subscribed.getDeviceID());
			map.put("subscribeId", "0000001");
			outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(map,
					childPlatformRegistUrl, "/regist/gbEvent/subscribe");
		} else {
			outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(map,
					gbPlatform.getSipServer(), "/access_server/subscribe");
		}
		HttpclientUtils.execute(map, outerDeviceByBGPost);
	}

	/**
	 * 
	 * @param sipServer
	 * @param context
	 * @param subscribeId
	 * @return
	 */
	public String notifyMsg(String sipServer, String content,
			String subscribeId, boolean sendToSip) {
		if (sendToSip) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("message", content);
			map.put("subscribeId", subscribeId);
			HttpPost outerDeviceByBGPost;
			if (nosip) {
				map.put("fromPlatformId", selfPlatformCmsId);
				outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(
						map, parentPlatformRegistUrl,
						"/regist/gbOuterDevice/updateCatalog");
			} else {
				outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(
						map, sipServer, "/access_server/notify");
			}
			HttpClient client = new DefaultHttpClient();
			InputStream in = null;
			try {
				HttpResponse response = client.execute(outerDeviceByBGPost);
				in = response.getEntity().getContent();
				in.close();
				return "OK";
			} catch (Exception e) {
				try {
					if (in != null) {
						in.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				e.printStackTrace();
			}
			return "ERROR";
		} else {
			return "OK";
		}
	}

	public String sendMsg(String sipServer, String context, String toPlatformId) {
		if (sendToSip && context != null) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("message", context);
			map.put("toPlatformId", toPlatformId);
			HttpPost outerDeviceByBGPost = null;
			if (nosip) {
				outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(
						map, parentPlatformRegistUrl,
						"/regist/gbOuterDevice/deviceRegist");
			} else {
				outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(
						map, sipServer, "/access_server/respondDevices");
			}
			HttpClient client = new DefaultHttpClient();
			InputStream in = null;
			try {
				HttpResponse response = client.execute(outerDeviceByBGPost);
				in = response.getEntity().getContent();
				in.close();
				return "OK";
			} catch (Exception e) {
				try {
					if (in != null) {
						in.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				e.printStackTrace();
			}
			return "ERROR";
		} else {
			return "OK";
		}
	}

	public String getServerContent(ServerStatus status, RequestEntity entity) {
		RequestEntity cloneEntity = entity.clone();
		cloneEntity.initEntityByServer(status);
		String content = PlatformUtils.getContent(cloneEntity.getDoc());
		if (recordSendLog && content != null)
			PlatformUtils.outputToFile(content, entity.getFromDeviceID(),
					entity.getSN(), recordSendLogDir + "/send");
		return content;
	}

	public String getPlatformContent(Platform platform, RequestEntity entity) {
		RequestEntity cloneEntity = entity.clone();
		cloneEntity.initEntityByPlatform(platform,platform.getParent());
		String content = PlatformUtils.getContent(cloneEntity.getDoc());
		if (recordSendLog) {
			PlatformUtils.outputToFile(content, entity.getFromDeviceID(),
					entity.getSN(), recordSendLogDir + "/send");
		}
		return content;
	}
	

	public String getOrganContent(Integer gbId, RequestEntity entity) {
		RequestEntity clonseEntity = entity.clone();
		clonseEntity.initEntityByOrgan(GbDomainMapFactory.getInstance().getGbOrganMap().get(gbId));
		String content = PlatformUtils.getContent(clonseEntity.getOrganDoc());
		if (recordSendLog && content != null) {
			PlatformUtils.outputToFile(content, entity.getFromDeviceID(),
					entity.getSN(), recordSendLogDir + "/send");
		}
		return content;
	}

	public String getDeviceContent(Device device,
			RequestEntity entity) {
		RequestEntity cloneEntity = entity.clone();
		cloneEntity.initEntityByDevice(device);
		String content = PlatformUtils.getContent(cloneEntity.getDoc());
		if (recordSendLog && content != null) {
			PlatformUtils.outputToFile(content, entity.getFromDeviceID(),
					entity.getSN(), recordSendLogDir + "/send");
		}
		return content;
	}
	
	public String getDeviceContent(List<DeviceStatus> deviceStatusList,
			RequestEntity entity,String parentStdId) {
		RequestEntity cloneEntity = entity.clone();
		cloneEntity.initEntityByDevices(deviceStatusList,parentStdId);
		String content = PlatformUtils.getContent(cloneEntity.getDoc());
		if (recordSendLog && content != null) {
			PlatformUtils.outputToFile(content, entity.getFromDeviceID(),
					entity.getSN(), recordSendLogDir + "/send");
		}
		return content;
	}

	public List<Callable<String>> createServerCallableList(
			RequestEntity entity, GbPlatform platform,
			List<ServerStatus> statusList) {
		List<Callable<String>> callables = new ArrayList<Callable<String>>();
		for (ServerStatus status : statusList) {
			SendResourceTask task = new SendResourceTask();
			task.setEntity(entity);
			task.setGbUtils(this);
			task.setPlatform(platform);
			task.setContent(getServerContent(status, entity));
			callables.add(task);
		}
		return callables;
	}

	public List<Callable<String>> createDeviceCallableList(
			RequestEntity entity, GbPlatform platform,
			List<Device> deviceList) {
		List<Callable<String>> callables = new ArrayList<Callable<String>>();
		for (Device device : deviceList) {
			SendResourceTask task = new SendResourceTask();
			task.setEntity(entity);
			task.setGbUtils(this);
			task.setPlatform(platform);
			task.setContent(getDeviceContent(device, entity));
			callables.add(task);
		}
		return callables;
	}

	public void createAccessServerCallableList(Map<String, String> accessIPMap,String masterAccessIP,Long currentFlush,AutoScanController ctrl) {
		for (String accessIP : accessIPMap.keySet()) {
			SendToAccessTask task = new SendToAccessTask();
			task.setAccessIPMap(accessIPMap);
			task.setAccessIP(accessIP);
			task.setCtrl(ctrl);
			task.setCurrentFlush(currentFlush);
			if(StringUtils.isBlank(masterAccessIP)){
				task.setMasterAccessIP(accessIP);
			}else{
				task.setMasterAccessIP(masterAccessIP);
			}
			executorService.execute(task);
		}
	}

	public String getPlatformContent(List<Platform> platforms,
			RequestEntity entity, Platform parent) {
		RequestEntity cloneEntity = entity.clone();
		cloneEntity.initEntityByPlatforms(platforms,parent);
		String content = PlatformUtils.getContent(cloneEntity.getOrganDoc());
		if (recordSendLog) {
			PlatformUtils.outputToFile(content, entity.getFromDeviceID(),
					entity.getSN(), recordSendLogDir + "/send");
		}
		return content;
	}
	public String getPlatformContent(Platform platform,
			RequestEntity entity, Platform parent) {
		RequestEntity cloneEntity = entity.clone();
		cloneEntity.initEntityByPlatform(platform,parent);
		String content = PlatformUtils.getContent(cloneEntity.getOrganDoc());
		if (recordSendLog) {
			PlatformUtils.outputToFile(content, entity.getFromDeviceID(),
					entity.getSN(), recordSendLogDir + "/send");
		}
		return content;
	}

	public String getServerContents(List<ServerStatus> serverStatusList,
			RequestEntity entity, String stdId) {
		RequestEntity cloneEntity = entity.clone();
		cloneEntity.initEntityByServers(serverStatusList,stdId);
		String content = PlatformUtils.getContent(cloneEntity.getDoc());
		if (recordSendLog && content != null)
			PlatformUtils.outputToFile(content, entity.getFromDeviceID(),
					entity.getSN(), recordSendLogDir + "/send");
		return content;
	}

	public String getDeviceContents(List<DeviceStatus> deviceStatusList,
			RequestEntity entity) {
		RequestEntity cloneEntity = entity.clone();
		cloneEntity.initEntityByDevices(deviceStatusList,"");
		String content = PlatformUtils.getContent(cloneEntity.getDoc());
		if (recordSendLog && content != null) {
			PlatformUtils.outputToFile(content, entity.getFromDeviceID(),
					entity.getSN(), recordSendLogDir + "/send");
		}
		return content;
	}
	
	public String getDeviceContents(List<DeviceStatus> deviceStatusList,
			RequestEntity entity,String serverStdId) {
		RequestEntity cloneEntity = entity.clone();
		cloneEntity.initEntityByDevices(deviceStatusList,serverStdId);
		String content = PlatformUtils.getContent(cloneEntity.getDoc());
		if (recordSendLog && content != null) {
			PlatformUtils.outputToFile(content, entity.getFromDeviceID(),
					entity.getSN(), recordSendLogDir + "/send");
		}
		return content;
	}
	
	public void notifyDevice(DeviceStatus status,int nanosecond) {
		SubscribeEvent subscribeEvent = Ar.of(SubscribeEvent.class).get(
				status.getSubscribeEventId());
		GbPlatform platform = Ar.of(GbPlatform.class).get(status.getPlatformId());
		try {
			NotifyEntity entity = NotifyEntity.getInstance(subscribeEvent,platform);
			entity.initItemByBaseDeviceStatus(status, subscribeEvent);
			String content = PlatformUtils.getContent(entity.getDoc());
			if (recordSendLog) {
				PlatformUtils.outputToFile(content, entity.getFromDeviceID(),
						entity.getSN(), recordSendLogDir + "/notify");
			}
			String result = this.notifyMsg(platform.getSipServer(), content,
					subscribeEvent.getSubscribeId(), sendToSip);
			if(result.equals("OK")){
				status.setBaseNotifySign(nanosecond);
				Ar.update(status);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void notifyOrgan(OrganStatus status,int nanosecond) {
		SubscribeEvent subscribeEvent = Ar.of(SubscribeEvent.class).get(
				status.getSubscribeEventId());
		GbPlatform platform = Ar.of(GbPlatform.class).get(
				status.getPlatformId());
		NotifyEntity entity = NotifyEntity.getInstance(subscribeEvent, platform);
		entity.setGbPlatform(platform);
		entity.initItemByBaseOrganStatus(status);
		if (entity.getItems().size() > 0) {
			String content = PlatformUtils.getContent(entity.getOrganDoc());
			if (recordSendLog) {
				PlatformUtils.outputToFile(content, entity.getFromDeviceID(),
						entity.getSN(), recordSendLogDir + "/notify");
			}
			String result = this.notifyMsg(platform.getSipServer(), content,
					subscribeEvent.getSubscribeId(), sendToSip);
			if(result.equals("OK")){
				status.setBaseNotifySign(nanosecond);
				Ar.update(status);
			}
		}
	}
	
	public void onlineNotify(DeviceStatus status,int nanosecond){
		SubscribeEvent subscribeEvent = Ar.of(SubscribeEvent.class).get(
				status.getSubscribeEventId());
		GbPlatform platform = status.getPlatform();
		NotifyEntity entity = NotifyEntity.getInstance(subscribeEvent, platform);
		entity.initItemByOnlineDeviceStatus(status);
		if (entity.getItems().size() > 0) {
			String content = PlatformUtils.getContent(entity.getDoc());
			if (recordSendLog) {
				PlatformUtils.outputToFile(content,
						entity.getFromDeviceID(), entity.getSN(),
						recordSendLogDir + "/notify");
			}
			String result = this.notifyMsg(platform.getSipServer(), content,
					subscribeEvent.getSubscribeId(), sendToSip);
			if(result.equals("OK")){
				status.setOnlineNotifySign(nanosecond);
				Ar.update(status);
			}
		}
	}

}
