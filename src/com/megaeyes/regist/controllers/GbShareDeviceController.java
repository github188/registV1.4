package com.megaeyes.regist.controllers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hight.performance.annotation.Param;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mega.jdom.Document;
import com.mega.jdom.Element;
import com.mega.jdom.input.SAXBuilder;
import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.PlatformStatus;
import com.megaeyes.regist.enump.DeviceIdType;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.GbPlatformType;
import com.megaeyes.regist.utils.HttpclientUtils;
import com.megaeyes.regist.utils.MyExecutors;
import com.megaeyes.regist.utils.Page;
import com.megaeyes.regist.webservice.PlatformUtils;
import com.megaeyes.regist.webservice.RegistUtil;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

/*
 * 作为下级使用的方法
 */
@Component("gbShareDevice")
public class GbShareDeviceController {

	@Autowired
	private RegistUtil registUtil;
	private static final MyExecutors exec = new MyExecutors(20);

	/**
	 * 接收对方alarmResponse信息，只提示，不做持久化
	 * 
	 * @return
	 */
	public String alarmResponse(Invocation inv, @Param("message") String message) {
		System.out.println("上级平台接收到报警信息：" + message);

		inv.addModel("msg", "OK");
		inv.addModel("result", "success");

		return PlatformUtils.getGBPath("result");
	}

	/*
	 * 更新当前报警设备的状态
	 */
	private void updateDeviceAlarmStatus(String deviceId, String status) {
		Device device = Ar.of(Device.class).one("stdId", deviceId);
		device.setAlarmStatu(status);
		Ar.saveOrUpdate(device);
	}

	/*
	 * 设置报警复位状态
	 */
	private void updateDeviceAlarmReset(String deviceId, String status) {
		Device device = Ar.of(Device.class).one("stdId", deviceId);
		device.setAlarmReset(status);
		Ar.saveOrUpdate(device);
	}

	/**
	 * 发送数据到Sip服务器
	 * 
	 * @param rootElement
	 *            : 文档节点
	 * @param partUrl
	 *            ：发送的URL
	 * @param platform
	 *            :当前平台
	 * @return
	 */
	private String sendResponse(Element rootElement, String partUrl,
			GbPlatform platform) {
		Document document = new Document();
		document.setRootElement(rootElement);

		// 发送数据
		InputStream in = null;
		try {
			Map<String, String> httpMap = new HashMap<String, String>();
			httpMap.put("message", PlatformUtils.getContent(document));
			System.out.println("gbShareDevice send alarm info:\r\n"
					+ httpMap.get("message"));
			System.out.println("send url is:" + "/access_server/" + partUrl);
			HttpPost outerDeviceByBGPost = HttpclientUtils
					.getOuterDeviceByBGPost(httpMap, platform.getSipServer(),
							"/access_server/" + partUrl);
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(outerDeviceByBGPost);
			System.out.println("send success\r\n");
			// in.close();
		} catch (Exception e) {
			System.out.println("exception url is:" + "/access_server/"
					+ partUrl);
			e.printStackTrace();
			return "error";
		}

		return "OK";
	}

	/**
	 * 查询设备详细信息
	 * 
	 * @param inv
	 * @param message
	 * @return
	 */
	public String queryDeviceDetail(Invocation inv,
			@Param("message") String message) {

		System.out.println("接收查询设备信息:" + message);

		Reader in = null;
		try {
			in = new BufferedReader(new StringReader(message));
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(in);

			Element queryElement = doc.getRootElement();
			final String deviceId = queryElement.getChildText("DeviceID");
			final String sn = queryElement.getChildText("SN");
			final GbPlatform platform = getGbPlatform(deviceId);

			Runnable target = new Runnable() {
				@Override
				public void run() {
					queryDeviceDetailResponse(deviceId, sn, platform); // TODO:发送数据
				}
			};
			exec.execute(target);
			inv.addModel("msg", "OK");
		} catch (Exception e) {
			inv.addModel("msg", "ERROR");
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return PlatformUtils.getGBPath("result");
	}

	private GbPlatform getGbPlatform(String deviceId) {
		GbPlatform platform = Ar.of(GbPlatform.class).one(
				"from GbPlatform where childCmsId like ? and type=?",
				"%" + deviceId.substring(0, 6) + "%", GbPlatformType.PARENT);
		return platform;
	}

	/*
	 * 响应上级的查询条件，返回对应查询信息
	 */
	private String queryDeviceDetailResponse(String deviceId, String sn,
			GbPlatform platform) {
		InputStream in = null;
		Device device = null;
		DeviceServer server = null;
		int maxCamers = 0;
		int maxAics = 0;

		System.out.println("-------sendDeviceInfo----------");
		try {
			// 设备详细信息
			if (DeviceIdType.isServer(deviceId)) {
				server = Ar.of(DeviceServer.class).one("stdId", deviceId);
				maxCamers = Ar.of(Device.class).count("serverId,type",
						server.getId(), "VIC");
				maxAics = Ar.of(Device.class).count("serverId,type",
						server.getId(), "AIC");
			} else {
				device = Ar.of(Device.class).one("stdId", deviceId);
				server = Ar.of(DeviceServer.class).one("id",
						device.getServerId());
			}

			Element rootElement = new Element("Response");
			addElByText(rootElement, "CmdType", "DeviceInfo");
			addElByText(rootElement, "SN", sn);
			addElByText(rootElement, "DeviceID", deviceId);
			addElByText(rootElement, "Result", "OK");
			if (device == null) {
				addElByText(rootElement, "DeviceType", server.getType());
			} else {
				addElByText(rootElement, "DeviceType", device.getType());
			}

			addElByText(rootElement, "Manufacturer", server.getManufacturer());
			addElByText(rootElement, "Model", server.getModel());
			addElByText(rootElement, "Firmware", "V2.3, build 101111");
			addElByText(rootElement, "MaxCamera", "" + maxCamers);
			addElByText(rootElement, "MaxAlarm", "" + maxAics);

			Document document = new Document();
			document.setRootElement(rootElement);

			// 发送数据
			Map<String, String> httpMap = new HashMap<String, String>();
			httpMap.put("message", PlatformUtils.getContent(document));
			System.out.println(httpMap.get("message"));
			HttpPost outerDeviceByBGPost = HttpclientUtils
					.getOuterDeviceByBGPost(httpMap, platform.getSipServer(),
							"/access_server/queryDeviceDetailResponse");
			in = HttpclientUtils.getResponse(httpMap, outerDeviceByBGPost)
					.getEntity().getContent();
		} catch (Exception e) {
			e.printStackTrace();
			return "@Error";
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return "@Ok";
	}

	/**
	 * 响应上级平台查询数据
	 * 
	 * @param inv
	 * @param message
	 * @return
	 */
	public String queryDevice(Invocation inv, final @Param("message") String message,final @Param("fromPlatformId")String fromPlatformId) {
		try {
			System.out.println("获得外平台查询目录信息：" + message);
			inv.getRequest().setCharacterEncoding("GBK");
			
			DetachedCriteria criteria = DetachedCriteria
					.forClass(GbPlatform.class);
			criteria.add(Restrictions.eq("type", GbPlatformType.PARENT));
			criteria.add(Restrictions.eq("cmsId", fromPlatformId)); //支持直接机构或服务器查询
			final GbPlatform platform = Ar.one(criteria);
			
			if (platform == null) {
				inv.addModel("msg", "ERROR");
				inv.addModel("result", "wrong deviceId");
			} else {
				Runnable target = new Runnable() {
					@Override
					public void run() {
							RequestEntity entity = new RequestEntity();
							entity.init(message,fromPlatformId);
							entity.setGbPlatform(platform);
							Timestamp changeTime = new Timestamp(System.currentTimeMillis());
							int nanosecond = changeTime.getNanos() / 1000;
							entity.setChangeTime(changeTime);
							entity.setNanosecond(nanosecond);
							registUtil.getSendResourceHelper(platform).sendResource(entity);
							if(platform.isAutoPush()){
								platform.setHasQuery(true);
								Ar.update(platform);
							}
					}
				};
				exec.execute(target);
				inv.addModel("msg", "OK");
				inv.addModel("result", "success");
				return PlatformUtils.getGBPath("result");
			}
		} catch (Exception e) {
			e.printStackTrace();
			inv.addModel("msg", "ERROR");
		}
		return PlatformUtils.getGBPath("result");
	}
	/**
	 * 只有作为下级才走这个方法
	 * 
	 * @param inv
	 * @param cmsId
	 *            :下级平台ID，20位
	 * @param upperId
	 *            :上级平台ID，20位
	 * @param upperName
	 *            :上级平台的名字
	 * @param sipServer
	 *            ：Sip服务器地址？是上级的，还是下级的？还是就是网关的？
	 * @param online
	 *            ：是否在线
	 * @param errorCode
	 * @param heartBeatCycle
	 * @param description
	 * @return
	 */
	@Transactional("txManager")
	public String setSipServer(Invocation inv, @Param("cmsId") String cmsId,
			@Param("upperId") String upperId,
			@Param("upperName") String upperName,
			@Param("sipServer") String sipServer,
			@Param("online") Boolean online,
			@Param("errorCode") String errorCode,
			@Param("expires") Integer heartBeatCycle,
			@Param("description") String description) {
		try {
			// cmsId是20位的本平台的id
			GbPlatform platform = Ar.of(GbPlatform.class).one("cmsId,type",
					upperId, GbPlatformType.PARENT);
			if (platform == null) {
				System.out.println("上级平台不存在，请先建立，否则的话可能有问题！！");

				inv.addModel("msg", "ERROR");
				inv.addModel("result", "platform not in create in database");
				return PlatformUtils.getGBPath("result");
			}

			// TODO:设置上级平台的信息，如果不存在的话，发送的时候是有问题的
			platform.setSipServer(sipServer);
//			platform.setName(upperName);
			Ar.saveOrUpdate(platform);

			// TODO:上级平台的状态信息
			PlatformStatus status = Ar.of(PlatformStatus.class).one("gbPlatform.id",platform.getId());
			if (status == null) {
				status = new PlatformStatus();
			}
			status.setDescription(description);
			status.setErrorCode(errorCode);
			status.setHeartBeatCycle(heartBeatCycle);
			status.setHeartbeatTime(new Date());
			status.setOnline(online);
			status.setGbPlatform(platform);
			Ar.saveOrUpdate(status);
			inv.addModel("msg", "OK");
		} catch (Exception e) {
			inv.addModel("msg", "ERROR");
			inv.addModel("result", "cms is invalid");
			e.printStackTrace();
			inv.addModel("result", e);

		}
		return PlatformUtils.getGBPath("result");
	}

	/**
	 * 获取共享设备的所有视频服务器ID
	 * 
	 * @param deviceId
	 * @return
	 */
	// private Set<String> getDeviceServerIds(String deviceId) {
	// String cmsId = DeviceIdType.getCmsId(deviceId);
	// DetachedCriteria criteria = DetachedCriteria.forClass(Share.class);
	// criteria.add(Restrictions.eq("platformCmsId", cmsId));
	// return null;
	// }

	public String showDevices(Invocation inv, @Param("page") Page page,
			@Param("type") String type, @Param("cmsId") String cmsId,
			@Param("name") String name, @Param("first") String first) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Device.class);
		if (StringUtils.isNotBlank(cmsId)) {
			criteria.add(Restrictions.eq("cmsId", cmsId));
		} else {
			cmsId = PlatformUtils.getCmsId(inv);
			inv.addModel("cmsId", cmsId);
			criteria.add(Restrictions.eq("cmsId", cmsId));
		}
		criteria.add(Restrictions.ne("status", Status.delete.name()));
		if (StringUtils.isNotBlank(type)) {
			criteria.add(Restrictions.eq("type", type));
		}
		if (StringUtils.isNotBlank(name)) {
			criteria.add(Restrictions.like("name", name, MatchMode.START));
		}
		page.setRecordCount(criteria);
		List<Device> devices = Ar.find(criteria, page.getFirstResult(),
				page.getPageSize());
		inv.addModel("devices", devices);
		if ("true".equals(first)) {
			return PlatformUtils.getGBPath("alarm-device-main");
		}
		return PlatformUtils.getGBPath("alarm-device-list");

	}

	private void addElByText(Element parent, String child, String text) {
		Element childEl = new Element(child);
		childEl.setText(text);
		parent.addContent(childEl);
	}
}
