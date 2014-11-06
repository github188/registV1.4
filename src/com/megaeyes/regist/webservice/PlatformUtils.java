package com.megaeyes.regist.webservice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.mega.jdom.Document;
import com.mega.jdom.Element;
import com.mega.jdom.input.SAXBuilder;
import com.mega.jdom.output.Format;
import com.mega.jdom.output.XMLOutputter;
import com.megaeyes.commons.megaprotocol.v257.client.MegaTemplate;
import com.megaeyes.commons.megaprotocol.v257.client.Result;
import com.megaeyes.regist.bean.MsgRecord;
import com.megaeyes.regist.bean.OnlineStatus;
import com.megaeyes.regist.bean.sendResource.Item;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.utils.sendResource.ConfigUtil;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

public class PlatformUtils {
	public static ConcurrentHashMap<Long,MsgRecord> msgRecordMap = new ConcurrentHashMap<Long, MsgRecord>();

	public static Platform getPlatformParent() {
		// 找出本平台作为父平台
		Platform parent = Ar.of(Platform.class).one("cmsId",
				ConfigUtil.OWNER_PLATFORM_CMS_ID);
		if (parent == null) {
			parent = createOwnerPlatform();
		}
		return parent;
	}

	private static Platform createOwnerPlatform() {
		Platform platform = new Platform();
		platform.setCmsId(ConfigUtil.OWNER_PLATFORM_CMS_ID);
		platform.setName(ConfigUtil.OWNER_PLATFORM_CMS_ID);
		platform.setOwner(true);
		platform.setSync(false);
		Ar.save(platform);
		return platform;
	}

	public static boolean isCityRegist() {
		return new Boolean(ConfigUtil.IS_CITY_REGIST);
	}

	public static String getWsdlUrl() {
		return ConfigUtil.PARENT_REGIST_URL;
	}

	public static boolean isSameRegist(String cmsIdOne, String cmsIdTwo) {
		Platform one = Ar.of(Platform.class).get(cmsIdOne);
		Platform two = Ar.of(Platform.class).get(cmsIdTwo);
		if (one != null && two != null) {
			return true;
		} else {
			return false;
		}
	}

	public static String getSharePath(String resource) {
		StringBuilder temp = new StringBuilder();
		temp.append("share/").append(resource);
		return temp.toString();
	}

	public static String getObtainPath(String resource) {
		StringBuilder temp = new StringBuilder();
		temp.append("obtain/").append(resource);
		return temp.toString();
	}

	public static String getGBPath(String resource) {
		StringBuilder temp = new StringBuilder();
		temp.append("GB/").append(resource);
		return temp.toString();
	}

	public static String getCmsId(Invocation inv) {
		return (String) inv.getModelFromSession("platformCmsId");
	}

	public static String getContent(Document doc) {
		if (doc == null) {
			return null;
		}
		ByteArrayOutputStream outXMLStream = new ByteArrayOutputStream();
		try {
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getCompactFormat().setEncoding("GBK"));
			if (doc != null) {
				outputter.output(doc, outXMLStream);
				return outputter.outputString(doc).replaceAll("QQQQQ", "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				outXMLStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return outXMLStream.toString().replaceAll("QQQQQ", "");
	}

	/***************************************** 为兼容旧版本注同服务器begin ***********************************/
	public static String getParentCmsId(String cmsId) {
		// 分析平台编号,确定是属于那一级的平台
		if (isProvince(cmsId)) {
			// 省级
			return "";
		}
		if (isCity(cmsId)) {
			// 市级
			return cmsId.substring(0, 2) + "0000";
		}
		if (isCounty(cmsId)) {
			// 县级
			return cmsId.substring(0, 4) + "00";
		}
		return "";
	}

	public static boolean isCounty(String cmsId) {
		return !cmsId.substring(4).equals("00");
	}

	public static boolean isCity(String cmsId) {
		return cmsId.substring(4).equals("00")
				&& !cmsId.substring(2, 4).equals("00");
	}

	public static boolean isProvince(String cmsId) {
		return cmsId.substring(2).equals("0000");
	}

	public static boolean isChildResource(String resourceCmsId, String cmsId) {
		if (isProvince(cmsId)) {
			if (resourceCmsId.substring(0, 2).equals(cmsId.substring(0, 2))) {
				return true;
			}
		} else if (isCity(cmsId)) {
			if (resourceCmsId.substring(0, 4).equals(cmsId.substring(0, 4))) {
				return true;
			}
		} else {
			return false;
		}
		return false;
	}

	/*********************************************** 为兼容旧版本注册服务器end *********************/

	public static String sendToAccess(Document doc, Map<String, Object> map,
			String... encoding) {
		String accessServerIp = (String) map.get("IP");
		int accessServerPort = (Integer) map.get("port");
		int messageId = (Integer) map.get("msgId");
		String destinationID = (String) map.get("destinationID");
		String sourceId = "";
		String content = null;
		ByteArrayOutputStream outXMLStream = new ByteArrayOutputStream();
		try {
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat()
					.setEncoding(encoding.length == 0 ? "GBK" : encoding[0]));
			if (doc != null) {
				outputter.output(doc, outXMLStream);
				content = outXMLStream.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				outXMLStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		MegaTemplate mega = new MegaTemplate(accessServerIp, accessServerPort);
		mega.setBigEndian(false); // 没有设置时为true表示使用大头传送网络字节；和接入服务器连接时设置要改为false
		// mega.setEncoding(args[3]); // 没有设置时，使用运行环境默认的编码
		Result result = null;
		try {
			mega.getSocketOptions().setSoTimeout(5000);
			System.out.println("messageId=======" + messageId
					+ ", sourceId=====" + sourceId + ", destinationID==="
					+ destinationID + ", content========" + content);
			result = mega.execute(messageId, sourceId, destinationID, content);
		} catch (Exception ex) {
			System.out.println("Error Access Server:" + (String) accessServerIp
					+ " INFO:" + ex.getMessage());
		}
		if (result != null) {
			System.out.println("AccessServer:" + (String) accessServerIp
					+ " Statu:" + result.getSuccess() + " Content:\n"
					+ result.getContent());
		}
		System.out.println("result==============" + result);
		return result.getContent();
	}

	public static Map<String, Object> getSendParams(String naming, int msgId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("IP", naming.split(":")[2]);
		map.put("destinationID", naming.split(":")[0]);
		map.put("port", 6003);
		map.put("msgId", msgId);
		map.put("sourceId", "");
		return map;
	}

	public static Map<String, Object> getSendParamsByServer(String accessIP,
			int msgId) {
		Map<String, Object> map = new HashMap<String, Object>();
		// map.put("IP", naming.split(":")[1]);
		// map.put("destinationID", naming.split(":")[0]);
		map.put("IP", accessIP);
		map.put("destinationID", "");
		map.put("port", 6001);
		map.put("msgId", msgId);
		map.put("sourceId", "");
		return map;
	}

	public static Map<String, Object> getSendParams(String naming, int msgId,
			String sessionId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("IP", naming.split(":")[2]);
		map.put("destinationID", naming.split(":")[0]);
		map.put("port", 6003);
		map.put("msgId", msgId);
		map.put("sourceId", sessionId);
		return map;
	}

	@SuppressWarnings("unchecked")
	public static OnlineStatus getOnlineStatus(String result, String accessIP,
			String cmsId) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(getInputStreamfromString(result));
			Element root = doc.getRootElement();
			List<Element> devicesEl = root.getChildren("Device");
			OnlineStatus status = new OnlineStatus();
			status.setAccessIp(accessIP);
			status.setStartTime(new Date());
			status.setOnline(true);
			for (Element el : devicesEl) {
				status.getDeviceIds().add(
						el.getAttributeValue("ID") + "_" + cmsId);
			}
			return status;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Set<String> getDeviceStatus(String result, String accessIP,
			String cmsId) {
		BufferedReader in = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			in = new BufferedReader(new StringReader(result));
			Document doc = builder.build(in);
			in.close();
			Element root = doc.getRootElement();
			List<Element> devicesEl = root.getChildren("Device");
			Set<String> serverIds = new HashSet<String>();
			for (Element el : devicesEl) {
				serverIds.add(el.getAttributeValue("ID"));
			}
			return serverIds;
		} catch (Exception e) {
			e.printStackTrace();
			involidIpMap.put(accessIP, new Date());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return new HashSet<String>();
	}

	public static final String ENCODING = "UTF8";

	private static InputStream getInputStreamfromString(String str)
			throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes(ENCODING);
		return new BufferedInputStream(new ByteArrayInputStream(bytes));
	}

	public static String sendToAccess(String content, Map<String, Object> map)
			throws Exception {
		resetInvolidIp();
		String accessServerIp = (String) map.get("IP");
		if (involidIpMap.containsKey(accessServerIp)) {
			return null;
		}
		int accessServerPort = (Integer) map.get("port");
		int messageId = (Integer) map.get("msgId");
		String destinationID = (String) map.get("destinationID");
		String sourceId = (String) map.get("sourceId");
		MegaTemplate mega = new MegaTemplate(accessServerIp, accessServerPort);
		mega.setBigEndian(false); // 没有设置时为true表示使用大头传送网络字节；和接入服务器连接时设置要改为false
		// mega.setEncoding(args[3]); // 没有设置时，使用运行环境默认的编码
		Result result = null;
		try {
			mega.getSocketOptions().setSoTimeout(5000);
			System.out.println("messageId=======" + messageId
					+ ", sourceId=====" + sourceId + ", destinationID==="
					+ destinationID + ", content========" + content);
			result = mega.execute(messageId, sourceId, destinationID, content);
		} catch (Exception ex) {
			involidIpMap.put(accessServerIp, new Date());
			System.out.println("Error Access Server:" + (String) accessServerIp
					+ " INFO:" + ex.getMessage());
			throw ex;
		}
		if (result != null) {
			System.out.println("AccessServer:" + (String) accessServerIp
					+ " Statu:" + result.getSuccess() + " Content:\n"
					+ result.getContent());
		}
		System.out.println("result==============" + result);
		return result.getContent();
	}

	private static Map<String, Date> involidIpMap = new HashMap<String, Date>();

	private static void resetInvolidIp() {
		Set<String> ips = new HashSet<String>();
		ips.addAll(involidIpMap.keySet());
		for (String involidIp : ips) {
			Date startTime = involidIpMap.get(involidIp);
			Calendar last = Calendar.getInstance();
			last.setTime(startTime);
			last.add(Calendar.MILLISECOND, 600000);

			Calendar now = Calendar.getInstance();
			if (last.getTimeInMillis() < now.getTimeInMillis()) {
				involidIpMap.remove(involidIp);
			}
		}
	}

	public static void addElByText(Element parent, Field[] fields,
			Object object, String fieldNames) {
		for (Field field : fields) {
			if (StringUtils.isNotBlank(fieldNames)) {
				if (fieldNames.indexOf(field.getName()) > -1) {
					continue;
				}
			}
			field.setAccessible(true);
			Element childEl = new Element(WordUtils.capitalize(field.getName()));
			try {
				if (field.get(object) != null) {
					if (StringUtils.isEmpty("" + field.get(object))) {
						childEl.setText("QQQQQ");
					} else {
						childEl.setText("" + field.get(object));
					}

					parent.addContent(childEl);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void addElByTextIgnoreEmpty(Element parent, Field[] fields,
			Object object, String fieldNames) {
		for (Field field : fields) {
			if (StringUtils.isNotBlank(fieldNames)) {
				if (fieldNames.indexOf(field.getName()) > -1) {
					continue;
				}
			}
			field.setAccessible(true);
			Element childEl = new Element(WordUtils.capitalize(field.getName()));
			try {
				if (StringUtils.isNotBlank((String) field.get(object))) {
					childEl.setText((String) field.get(object));
					parent.addContent(childEl);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void addElByTextOnlyFields(Element parent, Field[] fields,
			Object object, String fieldNames) {
		for (Field field : fields) {
			if (StringUtils.isNotBlank(fieldNames)) {
				if (fieldNames.indexOf(field.getName()) > -1) {
					field.setAccessible(true);
					Element childEl = new Element(WordUtils.capitalize(field
							.getName()));
					try {
						if (field.get(object) != null) {
							if (StringUtils.isEmpty("" + field.get(object))) {
								childEl.setText("QQQQQ");
							} else {
								childEl.setText("" + field.get(object));
							}
							parent.addContent(childEl);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public static void record(Long sn,String cmsId,String queryMsg) {
		MsgRecord msgRecord = new MsgRecord();
		msgRecord.setCmsId(cmsId);
		msgRecord.setQueryTime(new Date());
		msgRecord.setLastReveiveTime(new Date());
		msgRecord.setQueryMsg(queryMsg);
		msgRecordMap.put(sn, msgRecord);
	}

	private static final AtomicLong sn = new AtomicLong(0);
	public static long getSN() {
		return sn.incrementAndGet();
	}

	public static void outputToFile(String str, String fromDeviceID, String SN,
			String basePath) {
		BufferedOutputStream bos = null;
		try {
			StringBuilder strb = new StringBuilder(basePath);
			strb.append(fromDeviceID);
			strb.append("_");
			strb.append(SN);
			strb.append(".txt");
			bos = new BufferedOutputStream(new FileOutputStream(
					strb.toString(), true));
			bos.write(str.getBytes("GBK"));
		} catch (FileNotFoundException fnfe) {
			System.out.println("File not found" + fnfe);
		} catch (IOException ioe) {
			System.out.println("Error while writing to file" + ioe);
		} finally {
			try {
				if (bos != null) {
					bos.flush();
					bos.close();
				}
			} catch (Exception e) {
				System.out.println("Error while closing streams" + e);
			}

		}

	}

	public static void setCivilCode(Item item, Organ parent) {
		if (parent == null) {
			return;
		}
		if (parent.getParent() != null && isPoliceRegion(parent)) {
			setCivilCode(item, parent.getParent());
		} else {
			item.setCivilCode(parent.getStdId());
		}
	}

	public static boolean isPoliceRegion(Organ organ) {
		return organ.getBlock() != null && !organ.getBlock().endsWith("0000");
	}
}
