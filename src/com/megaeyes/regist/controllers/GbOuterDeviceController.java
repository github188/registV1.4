package com.megaeyes.regist.controllers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import net.hight.performance.annotation.Param;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mega.jdom.Document;
import com.mega.jdom.Element;
import com.mega.jdom.input.SAXBuilder;
import com.megaeyes.commons.megaprotocol.v257.client.MegaTemplate;
import com.megaeyes.commons.megaprotocol.v257.client.Result;
import com.megaeyes.regist.bean.IOrgan;
import com.megaeyes.regist.bean.MsgRecord;
import com.megaeyes.regist.bean.NotifyItem;
import com.megaeyes.regist.bean.OperatorResult;
import com.megaeyes.regist.bean.sendResource.Item;
import com.megaeyes.regist.bean.sendResource.NotifyEntity;
import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.dao.AutoScanDao;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.domain.PlatformStatus;
import com.megaeyes.regist.domain.Subscribe;
import com.megaeyes.regist.enump.CmdType;
import com.megaeyes.regist.enump.DeviceIdType;
import com.megaeyes.regist.enump.SubscribeStatus;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.GbPlatformType;
import com.megaeyes.regist.utils.HttpclientUtils;
import com.megaeyes.regist.utils.sendResource.GBUtils;
import com.megaeyes.regist.utils.sendResource.GbDomainMapFactory;
import com.megaeyes.regist.webservice.PlatformUtils;
import com.megaeyes.regist.webservice.RegistUtil;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

/**
 * 重构国标共享,获取
 * 
 * @author molc
 * 
 */

@Component("gbOuterDevice")
public class GbOuterDeviceController {
	private static final Executor exec = Executors.newCachedThreadPool();
	private static Log log = LogFactory.getLog(GbOuterDeviceController.class);

	private static List<OperatorResult> retList = new ArrayList<OperatorResult>();
	private static int SAVE_MAX_ITEM = 5;
	private static boolean IS_RECORD = false;
	private static Map<Integer, String> responseContent = new HashMap<Integer, String>();
	private ThreadLocal<Boolean> finishReceive = new ThreadLocal<Boolean>();
	private final static AtomicBoolean noClearThread = new AtomicBoolean(true);

	@Autowired
	private RegisterDao registerDao;

	@Autowired
	private AutoScanDao autoScanDao;

	public void setGrantedDao(RegisterDao registerDao) {
		this.registerDao = registerDao;
	}

	@Autowired
	private RegistUtil registUtil;

	public void setRegistUtil(RegistUtil registUtil) {
		this.registUtil = registUtil;
	}

	public RegistUtil getRegistUtil() {
		return registUtil;
	}

	@Value("${recordSendLog}")
	private boolean recordSendLog;

	@Value("${recordSendLog.dir}")
	private String recordSendLogDir;

	@Value("${self_platform_cms_id}")
	private String selfPlatformCmsId;

	@Value("${child_platform_regist_url}")
	private String childPlatformRegistUrl;

	@Value("${nosip}")
	private boolean nosip;

	@Value("${subscribe_period}")
	private Integer period;

	@Autowired
	private GBUtils gbUtils;

	@Autowired
	private JdbcTemplate jdbc;

	@Value("${insertDevicesSQL}")
	private String insertDevicesSQL;

	@Value("${insertServerSQL}")
	private String insertServerSQL;

	@Value("${insertOrganSQL}")
	private String insertOrganSQL;
	
	@Autowired
	private AutoScanController autoScanCtrl;

	/**
	 * 对方平台作为下级平台的password
	 * 
	 * @param inv
	 * @param cmsId
	 * @return
	 */
	public String getPasswordBycmsId(Invocation inv,
			@Param("logonName") String cmsId) {
		try {
			GbPlatform platform = Ar.of(GbPlatform.class).one("cmsId", cmsId);
			inv.addModel("result", platform.getPassword());
			inv.addModel("msg", "OK");
		} catch (Exception e) {
			e.printStackTrace();
			inv.addModel("msg", "ERROR");
			inv.addModel("result", "wrong logonName");
		}
		return PlatformUtils.getGBPath("result");
	}

	/**
	 * 通过设备标准ID,获取我们平台设备的naming
	 * 
	 * @param inv
	 * @param id
	 * @return
	 */
	public String getNamingById(Invocation inv, @Param("id") String id) {
		try {

			String naming = "";
			if (DeviceIdType.isServer(id)) {
				DeviceServer server = Ar.of(DeviceServer.class)
						.one("stdId", id);
				naming = server.getNaming();
			} else if (DeviceIdType.isTerminal(id)) {
				Device device = Ar.of(Device.class).one("stdId", id);
				naming = device.getNaming();
			}
			inv.addModel("result", naming);
			inv.addModel("msg", "OK");
		} catch (Exception e) {
			log.error("**********************" + e.getMessage());
			inv.addModel("msg", "ERROR");
			inv.addModel("result", "wrong id:" + id);
		}
		return PlatformUtils.getGBPath("result");
	}

	/**
	 * 通过我们平台设备naming,获取设备的标准ID
	 * 
	 * @param inv
	 * @param naming
	 * @return
	 */
	public String getIdByNaming(Invocation inv, @Param("naming") String naming) {
		try {
			// 可能是摄像头的naming,也可能是视频服务器的naming
			int splitLength = naming.split(":").length;
			String stdId = "";
			String platformId = "";
			if (splitLength == 4) {
				Device device = Ar.of(Device.class).one("naming", naming);
				platformId = device.getCmsId();
				stdId = device.getStdId();
			} else if (splitLength == 3) {
				DeviceServer server = Ar.of(DeviceServer.class).one("naming",
						naming);
				platformId = server.getCmsId();
				stdId = server.getStdId();
			}
			inv.addModel("result", stdId);
			inv.addModel("platformId", platformId);
			inv.addModel("msg", "OK");
		} catch (Exception e) {
			log.error("*****************************" + e.getMessage());
			inv.addModel("result", naming + " is wrong");
			inv.addModel("msg", "ERROR");
		}
		return PlatformUtils.getGBPath("result");
	}

	/**
	 * 我们平台作为上级查询下级国标平台设备
	 * 
	 * @param inv
	 * @param cmsId
	 * @param deviceId
	 * @param cmdType
	 * @return
	 */
	public String queryOuterDevices(Invocation inv,
			@Param("cmsId") String cmsId, @Param("deviceId") String deviceId,
			@Param("cmdType") String cmdType) {
		GbPlatform gbPlatform = Ar.of(GbPlatform.class).one("cmsId,type",
				cmsId, GbPlatformType.CHILD);
		if (gbPlatform != null
				&& (StringUtils.isNotBlank(gbPlatform.getSipServer()) || nosip)) {
			autoScanDao.beforeQueryOuterDevice(cmsId);
			Long sn = PlatformUtils.getSN();
			RequestEntity entity = new RequestEntity();
			entity.init(cmdType, deviceId, sn.toString(), cmsId);
			String queryMsg = PlatformUtils.getContent(entity.getQueryDoc());
			PlatformUtils.record(sn, cmsId, queryMsg);
			if (noClearThread.getAndSet(false)) {
				Runnable command = new Runnable() {
					@Override
					public void run() {
						while (true) {
							clearExpireRecord();
							try {
								TimeUnit.SECONDS.sleep(600);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				};
				exec.execute(command);
			}
			if ("OK".equals(getRemoteDevices(gbPlatform, entity, queryMsg))) {
				return "@" + sn;
			}
		}
		return "@ERROR";
	}

	/**
	 * 做为上级平台，从sip服务器中获得对应的sip服务器信息；下级平台必须提前创建，这个命令主要用来绑定对应的sip服务器
	 * 
	 * @param inv
	 * @param cmsId
	 * @param accessServer
	 * @param online
	 * @param errorCode
	 * @param heartBeatCycle
	 * @param description
	 * @return
	 */
	@Transactional("txManager")
	public String platformRegist(Invocation inv, @Param("cmsId") String cmsId,
			@Param("access_server") String sipServer,
			@Param("online") boolean online,
			@Param("errorCode") String errorCode,
			@Param("expires") Integer heartBeatCycle,
			@Param("description") String description) {
		log.debug("**********************cmsId=" + cmsId + ", online=" + online
				+ ", errorCode=" + errorCode + ", sipServer=" + sipServer);
		try {
			GbPlatform platform = Ar.of(GbPlatform.class).one("cmsId,type",
					cmsId, GbPlatformType.CHILD);
			if (StringUtils.isNotBlank(sipServer)
					&& !sipServer.equals(platform.getSipServer())) {
				if (StringUtils.isNotBlank(platform.getSipServer())) {
					updateDeviceByPlatform(platform, sipServer);// 如果sip地址发生变化，要更新相关设备的naming
				}
				platform.setSipServer(sipServer);
				Ar.update(platform);
				registUtil.getRS(registerDao).parentPlatform();
			}
			PlatformStatus status = Ar.of(PlatformStatus.class).one(
					"gbPlatform.id", platform.getId());
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
			e.printStackTrace();
			inv.addModel("msg", "ERROR");
		}
		return PlatformUtils.getGBPath("result");
	}

	/**
	 * 接收对方平台推送过来的设备
	 * 
	 * @param inv
	 * @param message
	 * @return
	 */
	static String resp = "@<?xml version=\"1.0\" encoding=\"GBK\" ?><message><result msg=\"";

	public String deviceRegist(Invocation inv, @Param("message") String message) {
		StringBuilder sb = new StringBuilder(resp);
		try {
			RequestEntity entity = new RequestEntity();
			entity.init(message);
			MsgRecord record = getMsgRecord(entity);
			analyzeEntity(entity, record);
			if (recordSendLog) {
				PlatformUtils.outputToFile(message, record.getCmsId(),
						entity.getSN(), recordSendLogDir + "/receive");
			}
			if (finishReceive.get()) {
				batchSaveDevice(record);
			}
			sb.append("OK");
		} catch (Exception e) {
			e.printStackTrace();
			sb.append("ERROR");
		}
		sb.append("\"></result></message>");
		return sb.toString();
	}

	private void batchSaveDevice(MsgRecord record) {
		GbPlatform platform = Ar.of(GbPlatform.class).one("cmsId",
				record.getCmsId());
		createVirtualDeviceServer(platform);
		List<Object[]> params = new ArrayList<Object[]>();
		Set<String> keys = record.getDeviceParams().keySet();
		for (String key : keys) {
			params.add(record.getDeviceParams().remove(key));
			if (params.size() > 100) {
				jdbc.batchUpdate(insertDevicesSQL, params);
				params.clear();
			}
		}
		jdbc.batchUpdate(insertDevicesSQL, params);
		params.clear();
		autoScanDao.rectifyByCmsId(record.getCmsId());
		record.setFinish(true);
	}

	private void createVirtualDeviceServer(GbPlatform gbPlatform) {
		DeviceServer server = Ar.of(DeviceServer.class).one("serverId,cmsId",
				DeviceIdType.Mega.sample(), gbPlatform.getCmsId());
		if (server == null) {
			DetachedCriteria criteria = DetachedCriteria
					.forClass(GbPlatform.class);
			criteria.add(Restrictions.eq("type", GbPlatformType.PARENT));
			registerDao.createVirtualDeviceServer(gbPlatform);
			registUtil.getRS(registerDao)
					.parentRegistVIS(gbPlatform.getCmsId());
			Ar.flush();
		}
	}

	private void clearExpireRecord() {
		Set<Long> snSet = new HashSet<Long>();
		for (Long sn : PlatformUtils.msgRecordMap.keySet()) {
			MsgRecord record = PlatformUtils.msgRecordMap.get(sn);
			Date startTime = record.getLastReveiveTime();
			Calendar last = Calendar.getInstance();
			last.setTime(startTime);
			last.add(Calendar.MILLISECOND, 600000);

			Calendar now = Calendar.getInstance();
			if (last.getTimeInMillis() < now.getTimeInMillis()) {
				snSet.add(sn);
			}
		}
		for (Long sn : snSet) {
			MsgRecord record = PlatformUtils.msgRecordMap.get(sn);
			if (record.getSumNum() != record.getCurrentSumNum().intValue()) {
				batchSaveDevice(record);
			}
			PlatformUtils.msgRecordMap.remove(sn);
		}
	}

	public String waitFinish(Invocation inv, @Param("sn") Long sn) {
		String result = "receiving";
		MsgRecord record = PlatformUtils.msgRecordMap.get(sn);
		if (record == null) {
			result = "finish";
		} else {
			if (record.isFinish()) {
				result = "finish";
			}
		}
		Long deviceCount = jdbc
				.queryForLong("select count(*) from device where cms_id=?",
						record.getCmsId());
		List<Map<String, Object>> deviceGroup = jdbc
				.queryForList(
						"select count(*) as total, type from device where cms_id=? group by type",
						record.getCmsId());
		Long organCount = jdbc.queryForLong(
				"select count(*) from organ where cms_id=?", record.getCmsId());
		Long serverCount = jdbc.queryForLong(
				"select count(*) from device_server where cms_id=?",
				record.getCmsId());
		inv.addModel("organCount", organCount);
		inv.addModel("serverCount", serverCount);
		inv.addModel("result", result);
		inv.addModel("deviceGroup", deviceGroup);
		inv.addModel("sumNum", record.getSumNum());
		inv.addModel("currentSumNum", record.getCurrentSumNum().intValue());
		inv.addModel("deviceCount", deviceCount);
		inv.addModel("total", deviceCount + organCount + serverCount);

		return PlatformUtils.getGBPath("receiving-result");
	}

	public String receiveDeviceDetail(Invocation inv) {
		return PlatformUtils.getGBPath("receive-device-detail");
	}

	public void analyzeEntity(RequestEntity entity, MsgRecord record) {
		try {
			String cmsId = record.getCmsId();
			GbPlatform platform = Ar.of(GbPlatform.class).one("cmsId,type",
					cmsId, GbPlatformType.CHILD);
			if (entity.getCmdType().equals(CmdType.Catalog.name())) {
				Device tempDevice = new Device();
				if (DeviceIdType.isCmsId(entity.getDeviceID())
						|| DeviceIdType.isOrgan(entity.getDeviceID())) {
					// deviceList可能是机构，也可能是视频服务器，也可能是摄像头
					List<Item> items = entity.getItemList();
					for (Item item : items) {
						if (DeviceIdType.isServer(item.getDeviceID())) {
							// 为视频服务器
							DeviceServer itemServer = registerDao
									.getDeviceServer(item, platform);
							jdbc.update(insertServerSQL, registerDao
									.getInsertServerParams(itemServer,false));
							registUtil.getManufacturerUtils(
									platform.getManufacturer())
									.queryOuterDevices(this, platform,
											item.getDeviceID());
						} else if (DeviceIdType.isTerminal(item.getDeviceID())) {
							// deviceList为终端设备摄像头
							if (item.getDeviceID().length() > 6) {
								saveItemDevice(item, platform, tempDevice,
										record);
							}
						} else if (DeviceIdType.isOrgan(item.getDeviceID())) {
							// deviceList为机构
							IOrgan itemOrgan = registerDao.getOrgan(item,
									platform);
							jdbc.update(insertOrganSQL,
									registerDao.getInsertOrganParams(itemOrgan));

							registUtil.getManufacturerUtils(
									platform.getManufacturer())
									.queryOuterDevices(this, platform,
											item.getDeviceID());
						}
					}

				} else if (DeviceIdType.isServer((entity.getDeviceID()))) {
					// deviceList可能是机构，也可能是视频服务器，也可能是摄像头
					List<Item> items = entity.getItemList();
					for (Item item : items) {
						if (DeviceIdType.isTerminal(item.getDeviceID())) {
							saveItemDevice(item, platform, tempDevice, record);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 显示国标平台列表
	 * 
	 * @param inv
	 * @param cmsId
	 * @return
	 */
	public String showPlatformList(Invocation inv) {
		List<GbPlatform> platforms = Ar.of(GbPlatform.class).find(
				"from GbPlatform");
		inv.addModel("gbPlatforms", platforms);
		inv.addModel("statusMap", getStatusMap());
		inv.addModel("subscribeMap", getSubscribeMap());
		return PlatformUtils.getGBPath("platform-main");

	}

	@Transactional
	public String deleteParentPlatform(Invocation inv, @Param("id") String id) {
		try {
			jdbc.update("delete from platform_status where gb_platform_id=?",
					Integer.valueOf(id));
			jdbc.update("delete from subscribe_event where platform_id=?",
					Integer.valueOf(id));
			jdbc.update("delete from gb_share where platform_id=?",
					Integer.valueOf(id));
			jdbc.update("delete from gb_platform where id=?",
					Integer.valueOf(id));
		} catch (Exception e) {
			e.printStackTrace();
			return "@error";
		}
		return "@success";
	}

	public String getPlatformStatus(Invocation inv) {
		Map<String, Boolean> map = getStatusMap();
		StringBuilder str = new StringBuilder();
		for (String cmsId : map.keySet()) {
			str.append(cmsId).append("_").append(map.get(cmsId)).append(",");
		}
		if (str.length() > 0) {
			str.deleteCharAt(str.lastIndexOf(","));
		}
		return "@" + str.toString();
	}

	private Map<String, Subscribe> getSubscribeMap() {
		Map<String, Subscribe> map = new HashMap<String, Subscribe>();
		List<Subscribe> subscribes = Ar.of(Subscribe.class).find();
		for (Subscribe subscribe : subscribes) {
			map.put(subscribe.getToPlatformId(), subscribe);
		}
		return map;
	}

	/**
	 * 创建国标上级平台
	 * 
	 * @param inv
	 * @param cmsId
	 * @return
	 */
	public String forCreateParentPlatform(Invocation inv) {
		return PlatformUtils.getGBPath("create-platform");
	}

	/**
	 * 创建国标上级平台
	 * 
	 * @param inv
	 * @param cmsId
	 * @return
	 */
	public String forCreateChildPlatform(Invocation inv) {
		return PlatformUtils.getGBPath("create-child-platform");
	}

	public String validGbPlatformCmsId(Invocation inv,
			@Param("cmsId") String cmsId) {
		GbPlatform platform = Ar.of(GbPlatform.class).one("cmsId", cmsId);
		if (platform == null) {
			return "@true";
		} else {
			return "@false";
		}
	}

	public String validChildCmsId(Invocation inv,
			@Param("childCmsId") String gbPlatformCmsId,
			@Param("platformId") Integer id) {
		if (id != null) {
			Platform platform = Ar.of(Platform.class).get(id);
			if (gbPlatformCmsId.equals(platform.getGbPlatformCmsId())) {
				return "@true";
			}
			if (StringUtils.isBlank(platform.getGbPlatformCmsId())) {
				Platform other = Ar.of(Platform.class).one("gbPlatformCmsId",
						gbPlatformCmsId);
				if (other != null) {
					return "@false";
				}
			}
		}
		return "@true";
	}

	/**
	 * 
	 * 创建一个作为我们平台的上级国标平台
	 */
	public String createParentPlatform(Invocation inv,
			@Param("platform") final GbPlatform gbPlatform,
			@Param("platformId") Integer platformId) {
		if (validChildCmsId(inv, gbPlatform.getChildCmsId(), platformId)
				.equals("@false")) {
			return "@error";
		}
		gbPlatform.setType(GbPlatformType.PARENT);
		gbPlatform.setSipServer("http://51.0.0.121:8085");
		Ar.save(gbPlatform);
		Platform platform = Ar.of(Platform.class).get(platformId);
		platform.setGbPlatformCmsId(gbPlatform.getChildCmsId());
		Ar.update(platform);
		return "@success";
	}

	/**
	 * 
	 * 创建一个作为我们平台的下级国标平台
	 */
	public String createChildPlatform(Invocation inv,
			@Param("platform") GbPlatform gbPlatform,
			@Param("gbPlatformCmsId") String gbPlatformCmsId,
			@Param("platformId") Integer platformId) {

		gbPlatform.setType(GbPlatformType.CHILD);
		gbPlatform.setSipServer("http://51.0.0.121:8085");
		Ar.save(gbPlatform);

		Platform parent = Ar.of(Platform.class).get(platformId);
		parent.setGbPlatformCmsId(gbPlatformCmsId);
		Ar.update(parent);

		Platform platform = new Platform();
		platform.setName(gbPlatform.getName());
		platform.setCmsId(gbPlatform.getCmsId());
		platform.setParentCmsId(parent.getCmsId());
		platform.setParent(parent);// 设置父平台
		platform.setStatus(Status.add);
		platform.setOwner(false);
		platform.setPassword(gbPlatform.getPassword());
		platform.setGbPlatformCmsId(gbPlatform.getCmsId());
		Ar.save(platform);

		registUtil.getRS(registerDao).parentPlatform();
		return "@success";
	}

	/**
	 * 
	 * 通过全球眼id获得国标ID
	 */
	public String getGbIdByMegaId(Invocation inv, @Param("id") String id) {
		Device device = Ar.of(Device.class).one("deviceId", id);
		if (device != null && device.getStdId() != null) {
			inv.addModel("msg", "OK");
			inv.addModel("platformId", device.getCmsId());
			inv.addModel("result", device.getStdId());
			return PlatformUtils.getGBPath("result");
		}

		inv.addModel("msg", "ERROR");
		return PlatformUtils.getGBPath("result");
	}

	public String getOperationContent(Invocation inv,
			@Param("deviceId") String deviceId) {
		Device device = Ar.of(Device.class).get(deviceId);
		inv.addModel("device", device);
		return PlatformUtils.getGBPath("operation-content");
	}

	/**
	 * 重启下级平台的摄像头
	 */
	public String rebootVIC(Invocation inv, @Param("naming") String naming) {
		String accessServerIp = naming.split(":")[2];
		int accessServerPort = 6001;
		int messageId = 5046;
		String sourceId = "";
		String destId = naming.split(":")[1];
		MegaTemplate mega = new MegaTemplate(accessServerIp, accessServerPort);
		mega.setBigEndian(false);

		Result result = null;

		String content = "<Message>" + "<Type>2</Type>" + "<Naming>" + naming
				+ "</Naming>" + "</Message>";

		try {
			mega.getSocketOptions().setSoTimeout(5000);
			result = mega.execute(messageId, sourceId, destId, content);
		} catch (Exception ex) {
			log.error("Error Access Server:" + (String) accessServerIp
					+ " INFO:" + ex.getMessage());
		}
		if (result != null) {
			if (result.getSuccess() == 0) {
				return "@OK";
			}
		}
		return "@FAIL";
	}

	/**
	 * 查询下级平台摄像头的状态
	 */
	public String queryVICStatus(Invocation inv, @Param("naming") String naming) {

		Device device = Ar.of(Device.class).one("naming", naming);
		GbPlatform platform = Ar.of(GbPlatform.class).one("cmsId,type",
				device.getCmsId(), GbPlatformType.CHILD);
		Long sn = PlatformUtils.getSN();

		// 发送数据
		String content = "<Query>" + "<CmdType>DeviceStatus</CmdType>" + "<SN>"
				+ sn + "</SN>" + "<DeviceID>" + device.getStdId()
				+ "</DeviceID>" + "</Query>";
		Map<String, String> map = new HashMap<String, String>();
		map.put("message", content);

		HttpPost outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(
				map, platform.getSipServer(),
				"/access_server/queryDeviceStatus");

		InputStream in = null;
		try {
			in = HttpclientUtils.getResponse(map, outerDeviceByBGPost)
					.getEntity().getContent();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "@" + sn;
	}

	/*
	 * 查询设备状态返回
	 */
	public String queryDeviceStatusResponse(Invocation inv,
			@Param("message") String message) {
		System.out.println(message);
		Reader in = null;
		try {
			in = new BufferedReader(new StringReader(message));
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(in);
			Element root = doc.getRootElement();
			String sn = root.getChildText("SN");
			responseContent.put(Integer.parseInt(sn), message);
		} catch (Exception e) {
			inv.addModel("msg", "ERROR");
		}
		inv.addModel("result", "");
		inv.addModel("msg", "OK");
		return PlatformUtils.getGBPath("result");
	}

	/**
	 * 查询下级平台设备信息
	 * 
	 * @param inv
	 * @param naming
	 * @return
	 */
	public String queryVicDetail(Invocation inv, @Param("naming") String naming) {
		Device device = Ar.of(Device.class).one("naming", naming);
		GbPlatform platform = Ar.of(GbPlatform.class).one("cmsId,type",
				device.getCmsId(), GbPlatformType.CHILD);
		Long sn = PlatformUtils.getSN();

		String content = "<Query>" + "<CmdType>DeviceInfo</CmdType>" + "<SN>"
				+ sn + "</SN>" + "<DeviceID>" + device.getStdId()
				+ "</DeviceID>" + "</Query>";

		Map<String, String> map = new HashMap<String, String>();
		map.put("message", content);

		HttpPost outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(
				map, platform.getSipServer(),
				"/access_server/queryDeviceDetail");
		InputStream in = null;
		try {
			in = HttpclientUtils.getResponse(map, outerDeviceByBGPost)
					.getEntity().getContent();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "@" + sn;
	}

	/**
	 * 查询设备信息返回
	 */
	public String queryDeviceDetailResponse(Invocation inv,
			@Param("message") String message) {
		System.out.println(message);
		Reader in = null;
		try {
			in = new BufferedReader(new StringReader(message));
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(in);
			Element root = doc.getRootElement();
			String sn = root.getChildText("SN");
			responseContent.put(Integer.parseInt(sn), message);
		} catch (Exception e) {
			inv.addModel("msg", "ERROR");
		}

		inv.addModel("result", "");
		inv.addModel("msg", "OK");
		return PlatformUtils.getGBPath("result");
	}

	public String showResponseContent(@Param("sn") Integer sn) {
		if (responseContent.containsKey(sn)) {
			String info = responseContent.get(sn);
			responseContent.remove(sn);
			return "@" + info;
		} else {
			return "@";
		}
	}

	/**
	 * @param inv
	 * @param message
	 * @return
	 */
	public String setGuard(Invocation inv, @Param("naming") String naming) {
		/*
		 * <?xml version="1.0"?> <Control> <CmdType>DeviceControl</CmdType>
		 * <SN>17294</SN> <DeviceID>34020200001340000001</DeviceID>
		 * <GuardCmd>SetGuard</GuardCmd> </Control>
		 */
		//
		Device device = Ar.of(Device.class).one("naming", naming);
		GbPlatform platform = Ar.of(GbPlatform.class).one("cmsId,type",
				device.getCmsId(), GbPlatformType.CHILD);
		long sn = System.currentTimeMillis() % 1000;

		String content = "<Control>" + "<CmdType>DeviceControl</CmdType>"
				+ "<SN>" + sn + "</SN>" + "<DeviceID>" + device.getStdId()
				+ "</DeviceID>" + "<GuardCmd>SetGuard</GuardCmd>"
				+ "</Control>";

		System.out.println("-----------发送SetGuard信息-----------------");
		System.out.println(content);
		Map<String, String> map = new HashMap<String, String>();
		map.put("message", content);

		HttpPost outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(
				map, platform.getSipServer(), "/access_server/setGuard");

		// HttpGet get = HttpclientUtils.accessSelf(map,
		// "/gbOuterDevice/getDevicesByUser");

		// InputStream in = null;
		try {
			HttpclientUtils.getResponse(map, outerDeviceByBGPost);
			// SAXBuilder builder = new SAXBuilder();
			// in = response.getEntity().getContent();
			// Document doc = builder.build(in);
			//
			// Element root = doc.getRootElement();
			// in.close();
		} catch (Exception e) {
			return "@failure";
		}

		return "@OK";

	}

	/**
	 * @param inv
	 * @param message
	 * @return
	 */
	public String setGuardResponse(Invocation inv,
			@Param("message") String message) {
		System.out.println("==========setGuardResponse==========");
		System.out.println(message);

		// 将返回数据放入到列表中
		addResponseToList(message);

		inv.addModel("result", "");
		inv.addModel("msg", "OK");
		return PlatformUtils.getGBPath("result");
	}

	/**
	 * @param inv
	 * @param message
	 * @return
	 */
	public String resetGuard(Invocation inv, @Param("naming") String naming) {
		Device device = Ar.of(Device.class).one("naming", naming);
		GbPlatform platform = Ar.of(GbPlatform.class).one("cmsId,type",
				device.getCmsId(), GbPlatformType.CHILD);
		long sn = System.currentTimeMillis() % 1000;

		String content = "<Control>" + "<CmdType>DeviceControl</CmdType>"
				+ "<SN>" + sn + "</SN>" + "<DeviceID>" + device.getStdId()
				+ "</DeviceID>" + "<GuardCmd>ResetGuard</GuardCmd>"
				+ "</Control>";

		System.out.println("-----------发送ResetGuard信息-----------------");
		System.out.println(content);
		Map<String, String> map = new HashMap<String, String>();
		map.put("message", content);

		HttpPost outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(
				map, platform.getSipServer(), "/access_server/resetGuard");
		HttpClient client = new DefaultHttpClient();

		InputStream in = null;
		try {
			HttpResponse response = client.execute(outerDeviceByBGPost);
			SAXBuilder builder = new SAXBuilder();
			in = response.getEntity().getContent();
			Document doc = builder.build(in);

			Element root = doc.getRootElement();
			in.close();
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

		return "@OK";
	}

	/**
	 * @param inv
	 * @param message
	 * @return
	 */
	public String resetGuardResponse(Invocation inv,
			@Param("message") String message) {
		System.out.println("==========resetGuardResponse==========");
		System.out.println(message);

		addResponseToList(message);

		inv.addModel("result", "");
		inv.addModel("msg", "OK");
		return PlatformUtils.getGBPath("result");
	}

	/**
	 * 报警复位设置
	 * 
	 * @param inv
	 * @param message
	 * @return
	 */
	public String resetAlarm(Invocation inv, @Param("naming") String naming) {
		Device device = Ar.of(Device.class).one("naming", naming);
		GbPlatform platform = Ar.of(GbPlatform.class).one("cmsId,type",
				device.getCmsId(), GbPlatformType.CHILD);
		long sn = System.currentTimeMillis() % 1000;

		/*
		 * <?xml version="1.0"?> <Control> <CmdType>DeviceControl</CmdType>
		 * <SN>17438</SN> <DeviceID>34020200001340000001</DeviceID>
		 * <AlarmCmd>ResetAlarm</AlarmCmd> <Info> <AlarmMethod>2</AlarmMethod>
		 * </Info> </Control>
		 */
		String content = "<Control>" + "<CmdType>DeviceControl</CmdType>"
				+ "<SN>" + sn + "</SN>" + "<DeviceID>" + device.getStdId()
				+ "</DeviceID>" + "<GuardCmd>ResetAlarm</GuardCmd>"
				+ "<Info><AlarmMethod>2</AlarmMethod></Info>" + "</Control>";

		System.out.println("-----------发送ResetAlarm信息-----------------");
		System.out.println(content);

		Map<String, String> map = new HashMap<String, String>();
		map.put("message", content);

		HttpPost outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(
				map, platform.getSipServer(), "/access_server/resetAlarm");
		HttpClient client = new DefaultHttpClient();

		InputStream in = null;
		try {
			HttpResponse response = client.execute(outerDeviceByBGPost);
			SAXBuilder builder = new SAXBuilder();
			in = response.getEntity().getContent();
			Document doc = builder.build(in);

			Element root = doc.getRootElement();
			in.close();
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

		return "@OK";
	}

	/**
	 * 报警复位回复
	 * 
	 * @param inv
	 * @param message
	 * @return
	 */
	public String resetAlarmResponse(Invocation inv,
			@Param("message") String message) {
		System.out.println("==========resetAlarmResponse==========");
		System.out.println(message);

		addResponseToList(message);

		inv.addModel("result", "");
		inv.addModel("msg", "OK");
		return PlatformUtils.getGBPath("result");
	}

	/**
	 * 接收下级平台报警
	 * 
	 * @param inv
	 * @param message
	 * @return
	 */
	public String alarm(Invocation inv, @Param("message") String message) {
		System.out.println("==========alarm==========");
		System.out.println(message);

		addResponseToList(message);

		Reader in = null;
		String tmpSn = "";
		String tmpDeviceId = "";

		try {
			inv.getRequest().setCharacterEncoding("GBK");
			in = new BufferedReader(new StringReader(message));
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(in);

			Element rootElement = doc.getRootElement();
			tmpSn = rootElement.getChildText("SN");
			tmpDeviceId = rootElement.getChildText("DeviceID");
			in.close();
			inv.addModel("msg", "OK");
		} catch (Exception e) {
			inv.addModel("msg", "ERROR");

			log.error("**********************" + e.getMessage());
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		final String sn = tmpSn;
		final String deviceId = tmpDeviceId;
		Runnable target = new Runnable() {
			@Override
			public void run() {
				// sendAlarmResponse
				sendAlarmResponse(sn, deviceId);
			}
		};
		exec.execute(target);

		inv.addModel("result", "");
		inv.addModel("msg", "OK");
		return PlatformUtils.getGBPath("result");
	}

	// 发送报警响应
	static private void sendAlarmResponse(String sn, String deviceId) {

		Device device = Ar.of(Device.class).one("stdId", deviceId);
		GbPlatform platform = Ar.of(GbPlatform.class).one("cmsId,type",
				device.getCmsId(), GbPlatformType.CHILD);

		/*
		 * <?xml version="1.0"?> <Response> <CmdType>Alarm</CmdType> <SN>1</SN>
		 * <DeviceID>64010000001340000101</DeviceID> <Result>OK</Result>
		 * </Response>
		 */
		String content = "<?xml version=\"1.0\"?><Response> <CmdType>Alarm</CmdType>"
				+ "<SN>"
				+ sn
				+ "</SN>"
				+ "<DeviceID>"
				+ deviceId
				+ "</DeviceID>" + "<Result>OK</Result> </Response>";

		Map<String, String> map = new HashMap<String, String>();
		map.put("message", content);

		HttpPost outerDeviceByBGPost = HttpclientUtils.getOuterDeviceByBGPost(
				map, platform.getSipServer(), "/access_server/alarmResponse");
		HttpClient client = new DefaultHttpClient();

		InputStream in = null;
		try {
			HttpResponse response = client.execute(outerDeviceByBGPost);
			SAXBuilder builder = new SAXBuilder();
			in = response.getEntity().getContent();
			Document doc = builder.build(in);
			Element root = doc.getRootElement();
			in.close();
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
	}

	private boolean isOnLine(String vicNaming, String onlineXml) {
		return true;
	}

	/**
	 * 查询DIV信息
	 * 
	 * @param inv
	 * @param message
	 * @return
	 */
	public String testDivRefresh(Invocation inv,
			@Param("message") String message) {
		if (retList.size() > SAVE_MAX_ITEM) {
			for (int i = 0; i < retList.size() - SAVE_MAX_ITEM; i++) {
				retList.remove(i);
			}
		}

		inv.addModel("retList", retList);

		return PlatformUtils.getGBPath("testfinalresult");
	}

	// 模拟发包处理

	/**
	 * 获得deviceId信息
	 * 
	 * @param message
	 * @return
	 */

	public OperatorResult getResultFromXml(String message) {
		Reader in = null;
		OperatorResult resultBean = new OperatorResult();

		try {
			System.out.println(message);
			in = new BufferedReader(new StringReader(message));
			SAXBuilder builder = new SAXBuilder();

			Document doc = builder.build(in);
			Element root = doc.getRootElement();

			// 设置相关参数
			resultBean.setDeviceId(root.getChildText("DeviceID"));
			resultBean.setResult(root.getChildText("Result"));
			resultBean.setRetTime(getCurrentDate());

			String cmdType = root.getChildText("CmdType");
			resultBean.setMethod(cmdType);
			if (cmdType.equalsIgnoreCase("Alarm")) {
				resultBean.setMethod(new String("报警".getBytes("GBK"), Charset
						.forName("UTF-8")));
			} else if (cmdType.equalsIgnoreCase("DeviceStatus")) {
				resultBean.setMethod(new String("查询设备状态".getBytes("GBK"),
						Charset.forName("UTF-8")));
			} else if (cmdType.equalsIgnoreCase("DeviceInfo")) {
				resultBean.setMethod(new String("查询设备详情".getBytes("GBK"),
						Charset.forName("UTF-8")));
			} else if (cmdType.equalsIgnoreCase("DeviceControl")) {
				Element guardElement = root.getChild("GuardCmd");
				if (guardElement != null) {
					cmdType = guardElement.getText();
					if (cmdType.equalsIgnoreCase("SetGuard")) {
						resultBean.setMethod(new String("布防".getBytes("GBK"),
								Charset.forName("UTF-8")));
					} else {
						resultBean.setMethod(new String("撤防".getBytes("GBK"),
								Charset.forName("UTF-8")));
					}
				}

				Element alarmElement = root.getChild("AlarmCmd");
				if (alarmElement != null) {
					cmdType = alarmElement.getText();
					if (cmdType.equalsIgnoreCase("Alarm")) {
						resultBean.setMethod(new String("设置报警".getBytes("GBK"),
								Charset.forName("UTF-8")));
					} else {
						resultBean.setMethod(new String("报警复位".getBytes("GBK"),
								Charset.forName("UTF-8")));
					}
				}

			}

			in.close();
		} catch (Exception e) {
			log.error("**********************" + e.getMessage());
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return resultBean;
	}

	/*
	 * 获得当前时间
	 */
	private String getCurrentDate() {
		SimpleDateFormat formatTest = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss", Locale.CHINA);
		Date testDate = new Date(System.currentTimeMillis());

		return formatTest.format(testDate);
	}

	/**
	 * 所有回复消息的入口
	 * 
	 * @param message
	 */
	private void addResponseToList(String message) {
		if (IS_RECORD) {
			retList.add(getResultFromXml(message));
		}
	}

	/**
	 * 测试程序
	 * 
	 * @param inv
	 * @param message
	 * @return
	 */
	public String testPostMessage(Invocation inv,
			@Param("message") String message) {
		String cmdType = message.substring(0, message.indexOf(":"));
		String deviceId = message.substring(message.indexOf(":") + 1,
				message.length());

		// String cmdType = message;
		OperatorResult resultBean = new OperatorResult();
		// resultBean.setDeviceId("34020000001310000051");
		resultBean.setDeviceId(deviceId);
		resultBean.setRetTime(getCurrentDate());

		try {
			// resultBean.setResult(new String("OK".getBytes("GBK"),
			// Charset.forName("UTF-8")));
			resultBean.setResult(new String("成功".getBytes("GBK"), Charset
					.forName("UTF-8")));
			if (cmdType.equalsIgnoreCase("Alarm")) {
				resultBean.setMethod(new String("报警".getBytes("GBK"), Charset
						.forName("UTF-8")));
			} else if (cmdType.equalsIgnoreCase("DeviceStatus")) {
				resultBean.setMethod(new String("查询设备状态".getBytes("GBK"),
						Charset.forName("UTF-8")));
			} else if (cmdType.equalsIgnoreCase("DeviceInfo")) {
				resultBean.setMethod(new String("查询设备详情".getBytes("GBK"),
						Charset.forName("UTF-8")));
			} else if (cmdType.equalsIgnoreCase("SetGuard")) {
				resultBean.setMethod(new String("布防".getBytes("GBK"), Charset
						.forName("UTF-8")));
			} else if (cmdType.equalsIgnoreCase("ReSetGuard")) {
				resultBean.setMethod(new String("撤防".getBytes("GBK"), Charset
						.forName("UTF-8")));
			} else if (cmdType.equalsIgnoreCase("Alarm")) {
				resultBean.setMethod(new String("设置报警".getBytes("GBK"), Charset
						.forName("UTF-8")));
			} else if (cmdType.equalsIgnoreCase("RestAlarm")) {
				resultBean.setMethod(new String("报警复位".getBytes("GBK"), Charset
						.forName("UTF-8")));
			} else if (cmdType.equalsIgnoreCase("Reboot")) {
				resultBean.setMethod(new String("重启设备".getBytes("GBK"), Charset
						.forName("UTF-8")));
			} else {
				resultBean.setMethod(cmdType);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!cmdType.equalsIgnoreCase("delete")) {
			retList.add(resultBean);
		} else {
			if (retList.size() > 0) {
				retList.remove(0);
			}
		}

		return "@OK";
	}

	private Map<String, Boolean> getStatusMap() {
		List<PlatformStatus> statuses = Ar.of(PlatformStatus.class).find();
		Map<String, Boolean> statusMap = new HashMap<String, Boolean>();
		for (PlatformStatus status : statuses) {
			boolean online = false;
			if (status.getErrorCode().equals("0") && status.isOnline()) {
				// 判断心跳
				Date heartBeat = status.getHeartbeatTime();
				Calendar last = Calendar.getInstance();
				last.setTime(heartBeat);
				last.add(Calendar.SECOND, status.getHeartBeatCycle());

				Calendar now = Calendar.getInstance();
				if (last.getTimeInMillis() > now.getTimeInMillis()) {
					online = true;
				}
			}
			statusMap.put(status.getGbPlatform().getCmsId(), online);
		}
		return statusMap;
	}

	private void saveItemDevice(Item item, GbPlatform platform,
			Device tempDevice, MsgRecord msgRecord) {
		Device dbDevice = registerDao.getDevice(item, platform);
		msgRecord.getDeviceParams().putIfAbsent(
				dbDevice.getDeviceId() + "_" + dbDevice.getOwnerId(),
				registerDao.getInsertDeviceSQLParams(dbDevice));
	}

	/**
	 * 根据SN得到相应的MsgRecord
	 * 
	 * @param sn
	 * @return
	 */
	private MsgRecord getMsgRecord(RequestEntity entity) {
		MsgRecord msgRecord = PlatformUtils.msgRecordMap.get(Long
				.valueOf(entity.getSN()));
		if (msgRecord == null) {
			throw new RuntimeException("cmsId is null,may be SN:"
					+ entity.getSN() + " is not exist");
		}
		msgRecord.setLastReveiveTime(new Date());
		if (msgRecord.getSumNum() == 0) {
			msgRecord.setSumNum(Integer.valueOf(entity.getSumNum()));
		}
		int currentSumNum = msgRecord.getCurrentSumNum(entity.getItemList()
				.size());
		if (msgRecord.getSumNum() == currentSumNum) {
			finishReceive.set(true);
		} else {
			finishReceive.set(false);
		}
		return msgRecord;
	}

	/**
	 * 变更相应的设备naming
	 * 
	 * @param platform
	 */
	private void updateDeviceByPlatform(GbPlatform platform, String sipServer) {
		String oldIP = RegisterDao.getSipServerIP(platform.getSipServer());
		String newIP = RegisterDao.getSipServerIP(sipServer);
		Ar.exesql(
				"update device set naming = replace(naming,?,?) where cms_id=?",
				oldIP, newIP, platform.getCmsId());
	}

	public String getRemoteDevices(GbPlatform platform, RequestEntity entity,
			String msg) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("message", msg);
		HttpPost outerDeviceByBGPost;
		if (nosip) {
			map.put("fromPlatformId", selfPlatformCmsId);
			outerDeviceByBGPost = HttpclientUtils
					.getOuterDeviceByBGPost(map, childPlatformRegistUrl,
							"/regist/gbShareDevice/queryDevice");
		} else {
			map.put("toPlatformId", entity.getFromDeviceID());
			outerDeviceByBGPost = HttpclientUtils
					.getOuterDeviceByBGPost(map, platform.getSipServer(),
							"/access_server/queryOuterDevices");
		}

		HttpClient client = new DefaultHttpClient();
		try {
			client.execute(outerDeviceByBGPost);
			return "OK";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "ERROR";
	}

	// 模拟相关程序发布
	// public static void main(String[] args) {
	// String message =
	// "<?xml version=\"1.0\"?><Response><CmdType>Alarm</CmdType><SN>1</SN><DeviceID>64010000001340000101</DeviceID><Result>OK</Result></Response>";
	// GbOuterDeviceController outDevice = new GbOuterDeviceController();
	// outDevice.getResultFromXml(message);
	// }

	public String getDeviceByUser() {
		return "@123123123";
	}

	private static final Map<String, Long> subscribeMap = new HashMap<String, Long>();

	// 发送订阅，period=0为订阅取消,period=-1为删除订阅
	public String sendSubscribe(@Param("toPlatformId") String cmsId,
			@Param("period") Integer period) {

		final String sendStdId = cmsId;
		final String deviceId = cmsId;

		final GbPlatform gbPlatform = Ar.of(GbPlatform.class).one("cmsId,type",
				cmsId, GbPlatformType.CHILD);
		Platform platform = Ar.of(Platform.class).one("gbPlatformCmsId",
				gbPlatform.getCmsId());
		final Long sn = PlatformUtils.getSN();
		final long flag = System.nanoTime() / 1000;
		subscribeMap.put(sendStdId, flag);
		if (period == null) {
			period = this.period;
		}
		final Integer myPeriod = period;
		// 判断是否需要插入记录
		Subscribe subscribe = Ar.of(Subscribe.class).one(
				"toPlatformId,fromPlatformId,deviceId",
				platform.getGbPlatformCmsId(),
				platform.getParent().getGbPlatformCmsId(), deviceId);
		try {
			if (period == 0) {
				if (subscribe != null) {
					gbUtils.sendSubscribe(sendStdId, "" + sn, "" + myPeriod,
							gbPlatform);
					Ar.delete(subscribe);
				}
			} else {
				if (subscribe == null) {
					subscribe = new Subscribe();
					subscribe.setDeviceId(deviceId);
					subscribe.setFromPlatformId(platform.getParent()
							.getGbPlatformCmsId());
					subscribe.setStatus(SubscribeStatus.NORMAL);
					subscribe.setSubscribeTime(new Timestamp(System
							.currentTimeMillis()));
					subscribe.setToPlatformId(gbPlatform.getCmsId());
					subscribe.setPeriod(myPeriod);
				} else {
					subscribe.setStatus(SubscribeStatus.NORMAL);
					subscribe.setPeriod(myPeriod);
					subscribe.setSubscribeTime(new Timestamp(System
							.currentTimeMillis()));
				}
				gbUtils.sendSubscribe(sendStdId, "" + sn, "" + myPeriod,
						gbPlatform);
				Ar.saveOrUpdate(subscribe);
			}
			flushSubscribe(sendStdId, flag, myPeriod, sn, gbPlatform);
		} catch (Exception e) {
			e.printStackTrace();
			return "@error";
		}
		return "@success";
	}

	private void flushSubscribe(final String sendStdId, final long flag,
			final Integer myPeriod, final long sn, final GbPlatform gbPlatform) {
		try {
			Runnable target = new Runnable() {
				@Override
				public void run() {
					while (true) {
						if (subscribeMap.get(sendStdId) != flag) {
							System.out.println(sendStdId + ":" + flag
									+ " break");
							subscribeMap.remove(sendStdId);
							break;
						}
						if (myPeriod > 0) {
							try {
								System.out.println(sendStdId + ":" + flag
										+ " continue");
								TimeUnit.SECONDS.sleep(myPeriod - 10);
							} catch (NumberFormatException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							gbUtils.sendSubscribe(sendStdId, "" + sn, ""
									+ myPeriod, gbPlatform);
						} else {
							System.out.println(sendStdId + ":" + flag
									+ " sub break");
							gbUtils.sendSubscribe(sendStdId, "" + sn, ""
									+ myPeriod, gbPlatform);
							break;
						}
					}
				}
			};
			exec.execute(target);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 只处理目录增加、更新、删除、设备在线和不在线；报警和故障部分暂时不处理
	public String updateCatalog(Invocation inv,
			@Param("message") String message,
			@Param("fromPlatformId") String fromPlatformId) {
		try {
			GbPlatform gbPlatform = Ar.of(GbPlatform.class).one("cmsId,type",
					fromPlatformId, GbPlatformType.CHILD);
			if (gbPlatform == null) {
				throw new Exception(
						"formPlatformId is null or error,please set request parameter!");
			}
			NotifyEntity entity = NotifyEntity.getInstance();
			entity.init(message, fromPlatformId);
			paserNotify(entity, gbPlatform);
			if (recordSendLog) {
				PlatformUtils.outputToFile(message, fromPlatformId,
						entity.getSN(), recordSendLogDir + "/receive_notity");
			}
			inv.addModel("msg", "OK");
			return PlatformUtils.getGBPath("result");
		} catch (Exception e) {
			inv.addModel("msg", "ERROR");
			log.error("**********************" + e.getMessage());
		}
		return PlatformUtils.getGBPath("result");
	}

	// 客户端获得对应摄像头状态
	public String queryDeviceStatus(Invocation inv,
			@Param("toPlatformId") String cmsId) {
		// 客户端自己去掉id的前25位
		if (cmsId.length() == 6) {
			String ip = getClientIP(inv);
			List<Device> deviceList = Ar.of(Device.class).find(
					"from Device where online = ? and  naming like ?", "ON",
					"%:" + ip + ":" + cmsId);
			inv.addModel("deviceList", deviceList);
		} else {
			PlatformStatus platformStatus = Ar.of(PlatformStatus.class).one("gbPlatform.cmsId=?",
					cmsId);
			if (platformStatus != null && platformStatus.isOnline()) {
				List<Device> deviceList = Ar.of(Device.class).find(
						"from Device where cmsId = ? and online = ?", cmsId,
						"ON");
				inv.addModel("deviceList", deviceList);
			} else {
				inv.addModel("deviceList", new ArrayList<Device>());
			}
		}
		inv.addModel("msg", "OK");
		return PlatformUtils.getGBPath("device-status");
	}

	private String getClientIP(Invocation inv) {
		String ip = inv.getRequest().getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = inv.getRequest().getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = inv.getRequest().getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = inv.getRequest().getRemoteAddr();
		}
		return ip;
	}

	// 订阅通知保存
	private void paserNotify(NotifyEntity entity, GbPlatform platform) {
		try {
			if (entity.getCmdType().equals(CmdType.Catalog.name())) {
				if (DeviceIdType.isCmsId(entity.getDeviceID())
						|| DeviceIdType.isOrgan(entity.getDeviceID())) {
					List<NotifyItem> items = entity.getItems();
					for (NotifyItem item : items) {
						if (DeviceIdType.isServer(item.getDeviceID())) {
							// 为视频服务器
							DeviceServer itemServer = registerDao
									.getDeviceServer(item, platform);
							if (isSaveOrUpdate(item)) {
								itemServer.setStatus(item.getEvent()
										.toLowerCase());
							} else if (item.getStatus().equals("DEL")) {
								itemServer.setStatus("delete");
							} else if (StringUtils.isNotBlank(getOnline(item))) {
								itemServer.setOnline(getOnline(item));
							}
							jdbc.update(insertServerSQL, registerDao
									.getInsertServerParams(itemServer,false));
							registUtil.getManufacturerUtils(
									platform.getManufacturer())
									.queryOuterDevices(this, platform,
											item.getDeviceID());
						} else if (DeviceIdType.isTerminal(item.getDeviceID())) {
							// deviceList为终端设备摄像头
							if (item.getDeviceID().length() > 6) {
								
								if (StringUtils
										.isNotBlank(getOnline(item))) {
									List<Device> devices = Ar.of(Device.class).find("from Device where deviceId=? and cmsId=?",DeviceIdType.getMegaId(item.getDeviceID()),platform.getCmsId());
									for(Device device : devices){
										device.setOnline(getOnline(item));
										jdbc.update(insertDevicesSQL, registerDao
												.getInsertDeviceSQLParams(device));
									}
									autoScanDao.resetDeviceStatusByOnline();
									autoScanCtrl.subscribeNotify();
								}else{
									if (isSaveOrUpdate(item)) {
										Device dbDevice = registerDao.getDevice(item,
												platform);
										dbDevice.setStatus(item.getEvent()
												.toLowerCase());
										jdbc.update(insertDevicesSQL, registerDao
												.getInsertDeviceSQLParams(dbDevice));
										autoScanDao.resetDeviceByCmsId(platform.getCmsId());
										autoScanDao.createGbDeviceByCmsId(platform.getCmsId());
										
									} else if (item.getEvent().equals("DEL")) {
										List<Device> devices = Ar.of(Device.class).find("from Device where deviceId=? and cmsId=?",DeviceIdType.getMegaId(item.getDeviceID()),platform.getCmsId());
										for(Device device : devices){
											device.setStatus("delete");
											device.setSync(false);
											jdbc.update(insertDevicesSQL, registerDao
													.getInsertDeviceSQLParams(device));
										}
									}
									autoScanDao.resetDeviceStatus();
									autoScanCtrl.subscribeNotify();
									registUtil.getRS(registerDao).parentRegistDevice(platform.getCmsId());
									GbDomainMapFactory.getInstance().initMap();
								}
								
							}
						} else if (DeviceIdType.isOrgan(item.getDeviceID())) {
							// deviceList为机构
							IOrgan itemOrgan = registerDao.getOrgan(item,
									platform);
							if (isSaveOrUpdate(item)) {
								itemOrgan.setStatus(item.getEvent()
										.toLowerCase());
							} else if (item.getStatus().equals("DEL")) {
								itemOrgan.setStatus("delete");
							}
							jdbc.update(insertOrganSQL,
									registerDao.getInsertOrganParams(itemOrgan));
							autoScanDao.resetOrganByCmsId(platform.getCmsId());
							autoScanDao.resetPlatformOrgan(platform.getCmsId());
							autoScanDao.createGbOrganByCmsId(platform.getCmsId());
							autoScanDao.resetOrganStatus();
							registUtil.getRS(registerDao).parentRegistOrgan(platform.getCmsId());
							GbDomainMapFactory.getInstance().initMap();
							registUtil.getManufacturerUtils(
									platform.getManufacturer())
									.queryOuterDevices(this, platform,
											item.getDeviceID());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private boolean isSaveOrUpdate(NotifyItem item) {
		return item.getEvent().equals("UPDATE")
				|| item.getEvent().equals("ADD");
	}

	public List<Organ> findOrgansByCmsId(GbPlatform platform) {
		List<Organ> organs = Ar.of(Organ.class).find(
				"from Organ where cmsId = ? and status = 2",
				platform.getCmsId());
		return organs;
	}

	private String getOnline(NotifyItem item) {
		if (item.getEvent().equals("VLOST") || item.getEvent().equals("DEFECT")
				|| item.getEvent().equals("ALARM")
				|| item.getEvent().equals("ON")) {
			return "ON";
		} else if (item.getEvent().equals("OFF")) {
			return "OFF";
		} else {
			return "";
		}
	}

	public String gbTest() {
		return PlatformUtils.getGBPath("receive_notify_test");
	}

	@Transactional
	public String deletePlatformResource(Invocation inv,
			@Param("cmsId") String cmsId) {
		try {
			jdbc.execute("set foreign_key_checks=0");
			jdbc.update(
					"delete gd from gb_device gd inner join device d on(d.id=gd.device_id and d.cms_id=?)",
					cmsId);
			jdbc.update(
					"delete po from platform_organ po inner join platform p on(po.platform_id=p.id and p.cms_id=?)",
					cmsId);
			jdbc.update(
					"delete go from gb_organ go inner join organ o on(o.id=go.source_id and go.source_type='organ' and  o.cms_id=?)",
					cmsId);

			jdbc.update(
					"delete go from gb_organ go inner join platform p on(go.source_id=p.id and p.id=? and go.source_type='platform')",
					cmsId);

			jdbc.update(
					"delete ds from device_status ds inner join device d on(d.id=ds.device_id) where d.cms_id=?",
					cmsId);

			jdbc.update("delete from device where cms_id=? ", cmsId);

			jdbc.update(
					"delete ss from server_status ss inner join device_server ds on(ds.id=ss.server_id) where ds.cms_id=?",
					cmsId);

			jdbc.update(
					"delete os from organ_server os inner join device_server ds on(ds.id=os.server_id) where ds.cms_id=?",
					cmsId);
			jdbc.update("delete from device_server where cms_id=? ", cmsId);

			jdbc.update(
					"delete os from organ_status os inner join organ o on(o.id=os.organ_id) where o.cms_id=?",
					cmsId);
			jdbc.update("delete from organ where cms_id=? ", cmsId);
			jdbc.execute("set foreign_key_checks=1");
			registerDao.clearInvaild();
			return "@success";
		} catch (Exception e) {
			e.printStackTrace();
			return "@error";
		}
	}

	public String deleteOrganResource(Invocation inv,
			@Param("cmsId") String cmsId, @Param("organId") String organId) {
		try {
			Organ organ = Ar.of(Organ.class).get(organId);
			String path = organ.getPath() + "%";
			Ar.exesql(
					"delete ss from server_status ss inner join device_server ds on(ds.id=ss.server_id) inner join device d on(d.server_id=ds.id and d.path like ? and d.cms_id=?) where ds.id!=?",
					organ.getPath(), cmsId, DeviceIdType.Mega.sample() + "_"
							+ cmsId);

			Ar.exesql(
					"delete ds from device_server ds inner join organ_server os on(os.server_id=ds.id and os.organ_id=?)",
					organId);

			Ar.exesql("delete os from organ_server os where os.organ_id=?",
					organId);

			Ar.exesql(
					"delete ds from device_status ds inner join device d on(d.id=ds.device_id and d.path like ? and d.cms_id=?)",
					path, cmsId);
			Ar.exesql("delete from device where path like ? and cms_id=? ",
					path, cmsId);

			Ar.exesql(
					"delete os from organ_status os inner join organ o on(o.id=os.organ_id and o.path like ? and o.cms_id=? and o.id!=?)",
					path, cmsId, organId);
			Ar.exesql("SET FOREIGN_KEY_CHECKS=0");
			Ar.exesql(
					"delete  from organ where path like ? and cms_id=? and id!=?",
					path, cmsId, organId);
			Ar.exesql("SET FOREIGN_KEY_CHECKS=1");
			Ar.flush();
			registerDao.clearInvaild();
			return "@success";
		} catch (Exception e) {
			e.printStackTrace();
			return "@error";
		}
	}

	public String getSubscribeList(Invocation inv) {
		List<Subscribe> subscribeList = Ar.of(Subscribe.class).find();
		List<GbPlatform> platforms = Ar.of(GbPlatform.class).find("type",
				GbPlatformType.CHILD);
		Map<String, GbPlatform> map = new HashMap<String, GbPlatform>();
		for (GbPlatform platform : platforms) {
			map.put(platform.getCmsId(), platform);
		}
		inv.addModel("subscribeList", subscribeList);
		inv.addModel("platformMap", map);
		return "/GB/subscribe-list";
	}
}
