package com.megaeyes.regist.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.hight.performance.annotation.Param;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mega.jdom.Document;
import com.mega.jdom.Element;
import com.mega.jdom.input.SAXBuilder;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.other.DeviceType;
import com.megaeyes.regist.other.ItemType;
import com.megaeyes.regist.other.OperateType;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.webservice.RegistUtil;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("outerDevice")
public class OuterDeviceController {

	@Autowired
	private RegisterDao registerDao;

	public void setRegisterDao(RegisterDao registerDao) {
		this.registerDao = registerDao;
	}

	@Autowired
	private RegistUtil registUtil;

	public void setRegistUtil(RegistUtil registUtil) {
		this.registUtil = registUtil;
	}

	public String index(Invocation inv, @Param("AccessIP") String accessIP,
			@Param("PlatformID") String platformId,
			@Param("ParentCmsId") String parentCmsId,
			@Param("resource") String resource) {
		Platform platform = Ar.of(Platform.class).one("cmsId", platformId);
		// 判断该平台有没有注册过
		if (platform == null) {
			// 平台注册
			createPlatform(platformId, parentCmsId);
		}

		// 解释xml体
		parseResource(platformId, resource, accessIP);

		registUtil.getRS(registerDao).outerDeviceRegist(platformId, accessIP,
				parentCmsId, resource);
		inv.addModel("msg", "success");
		return "outer-device-msg";
	}

	@SuppressWarnings("unchecked")
	private void parseResource(String platformId, String resource,
			String accessIP) {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(getInputStreamfromString(resource));
			Element root = doc.getRootElement();
			Element el = root.getChild("ItemList");
			el.setAttribute("cmsId", platformId);
			el.setAttribute("AccessIP", accessIP);
			List<Element> children = el.getChildren("Item");
			for (Element item : children) {
				process(item, el);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void createPlatform(String platformId, String parentCmsId) {
		Platform platform = new Platform();
		Platform parent = Ar.of(Platform.class).get(parentCmsId);
		platform.setCmsId(platformId);
		platform.setName(platformId);
		platform.setParent(parent);
		platform.setStatus(Status.add);
		Ar.save(platform);
	}

	private static final String ENCODING = "UTF8";

	private InputStream getInputStreamfromString(String str)
			throws UnsupportedEncodingException {
		byte[] bytes = str.getBytes(ENCODING);
		return new ByteArrayInputStream(bytes);
	}

	public void process(Element item, Element root) {
		ItemType itemListType = ItemType
				.getType(root.getAttributeValue("Type"));
		OperateType operateType = OperateType.getType(item
				.getAttributeValue("Method"));
		ItemType itemType = ItemType.getType(item.getAttributeValue("Type"));
		switch (itemListType) {
		case Platform:
			switch (operateType) {
			case Add:
				organAdd(root, item, null);
				break;
			case Update:
				organUpdate(root, item, null);
				break;
			case Delete:
				organDelete(root, item);
				break;
			default:
				break;
			}
			break;
		case Organ:
			switch (itemType) {
			case Organ:
				switch (operateType) {
				case Add:
					organAdd(root, item, root.getAttributeValue("ID"));
					break;
				case Update:
					organUpdate(root, item, root.getAttributeValue("ID"));
					break;
				case Delete:
					organDelete(root, item);
					break;
				default:
					break;
				}
				break;
			case Terminal:
				switch (operateType) {
				case Add:
					deviceServerAdd(root, item);
					break;
				case Update:
					deviceServerUpdate(root, item);
					break;
				case Delete:
					deviceServerDelete(root, item);
					break;
				default:
					break;
				}
			default:
				break;
			}
		case Terminal:
			switch (itemType) {
			case Camera:
				switch (operateType) {
				case Add:
					deviceAdd(root, item, DeviceType.VIC);
					break;
				case Update:
					deviceUpdate(root, item, DeviceType.VIC);
					break;
				case Delete:
					deviceDelete(root, item, DeviceType.VIC);
					break;
				default:
					break;
				}
				break;
			case AlarmIn:
				switch (operateType) {
				case Add:
					deviceAdd(root, item, DeviceType.AIC);
					break;
				case Update:
					deviceUpdate(root, item, DeviceType.AIC);
					break;
				case Delete:
					deviceDelete(root, item, DeviceType.AIC);
					break;
				default:
					break;
				}
				break;
			case AlarmOut:
				switch (operateType) {
				case Add:
					deviceAdd(root, item, DeviceType.AOC);
					break;
				case Update:
					deviceUpdate(root, item, DeviceType.AOC);
					break;
				case Delete:
					deviceDelete(root, item, DeviceType.AOC);
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}

	private void deviceDelete(Element root, Element item, DeviceType type) {
		Device device = createDevice(root, item, type);
		device.setStatus(Status.delete.name());
		registerDao.delShareAuthorizationByDevice(device);
	}

	private void deviceUpdate(Element root, Element item, DeviceType type) {
		Device device = createDevice(root, item, type);
		device.setStatus(Status.update.name());
		Ar.saveOrUpdate(device);
	}

	private void deviceAdd(Element root, Element item, DeviceType type) {
		Device device = createDevice(root, item, type);
		try {
			Ar.saveOrUpdate(device);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deviceServerDelete(Element root, Element item) {
		String cmsId = root.getAttributeValue("cmsId");
		String id = getMegaId(item.getAttributeValue("ID"));
		DeviceServer deviceServer = Ar.of(DeviceServer.class).get(id);
		if (deviceServer != null) {
			registerDao.deleteDeviceByServer(cmsId, deviceServer.getServerId());
			Ar.delete(deviceServer);
		}
	}

	private void deviceServerAdd(Element root, Element item) {
		String cmsId = root.getAttributeValue("cmsId");
		String serverId = getMegaId(item.getAttributeValue("ID"));
		DeviceServer deviceServer = new DeviceServer();
		deviceServer.setServerId(serverId);
		deviceServer.setCmsId(cmsId);
		deviceServer.setIP(item.getChildText("IP"));
		deviceServer.setLocation(item.getChildText("Location"));
		deviceServer.setName(item.getAttributeValue("Name"));
		Element streamSupport = item.getChild("StreamSupport");
		deviceServer.setStreamSupport(streamSupport.getChildText("SubStream1")
				+ streamSupport.getChildText("SubStream2")
				+ streamSupport.getChildText("SubStream3"));
		deviceServer.setType("Terminal");
		Ar.saveOrUpdate(deviceServer);
	}

	private void deviceServerUpdate(Element root, Element item) {
		deviceServerAdd(root, item);
	}

	private Device createDevice(Element root, Element item, DeviceType type) {
		DeviceServer server = Ar.of(DeviceServer.class).get(
				getMegaId(root.getAttributeValue("ID")));
		Organ organ = Ar.of(Organ.class).get(server.getOrganId());
		Device pdevice = new Device();
		String id = getMegaId(item.getAttributeValue("ID") + "_"
				+ organ.getCmsId());
		pdevice.setDeviceId(id);
		pdevice.setType(type.name());
		pdevice.setLocation(item.getChildText("Location"));
		pdevice.setName(item.getAttributeValue("Name"));
		pdevice.setNaming(id + ":" + server.getId() + ":"
				+ root.getAttributeValue("AccessIP") + ":"
				+ root.getAttributeValue("cmsId"));
		pdevice.setPath(organ.getPath());
		pdevice.setStatus(Status.normal.name());
		if (type.equals(DeviceType.VIC)) {
			pdevice.setPermission("11" + item.getChildText("IsPtz"));
		} else {
			pdevice.setPermission("111");
		}
		Element GPS = item.getChild("GPS");
		pdevice.setLatitude(GPS.getChildText("Latitude"));
		pdevice.setLongitude(GPS.getChildText("Longitude"));
		pdevice.setSupportScheme(false);
		pdevice.setCmsId(root.getAttributeValue("cmsId"));
		return pdevice;
	}

	private void organDelete(Element root, Element item) {
		String cmsId = root.getAttributeValue("cmsId");
		String organId = getMegaId(item.getAttributeValue("ID"));
		Organ dbOrgan = Ar.of(Organ.class).get(organId + "_" + cmsId);
		if (dbOrgan != null) {
			Set<String> ids = new HashSet<String>();
//			registerDao.deleteOrgan(dbOrgan, ids);
		}
	}

	private void organUpdate(Element root, Element item, String parentId) {
		String cmsId = root.getAttributeValue("cmsId");
		String organId = getMegaId(item.getAttributeValue("ID"));
		Organ dbOrgan = Ar.of(Organ.class).get(organId + "_" + cmsId);
		if (dbOrgan != null) {
			dbOrgan.setName(item.getAttributeValue("Name"));
			Ar.update(dbOrgan);
		} else {
			organAdd(root, item, parentId);
		}
	}

	private void organAdd(Element root, Element item, String parentId) {
		String cmsId = root.getAttributeValue("cmsId");
		Organ parent = null;
		if (parentId != null) {
			parent = Ar.of(Organ.class).get(getMegaId(parentId) + "_" + cmsId);
		}
		String organId = getMegaId(item.getAttributeValue("ID"));
		Organ organ = Ar.of(Organ.class).get(organId + "_" + cmsId);
		if (organ != null) {
			organ.setName(item.getAttributeValue("Name"));
			Ar.update(organ);
		} else {
			organ = new Organ();
			organ.setCmsId(cmsId);
			organ.setOrganId(organId);
			organ.setName(item.getAttributeValue("Name"));
			if (parent != null) {
				organ.setParentOrganId(parent.getOrganId());
				organ.setParentOrganName(parent.getName());
				organ.setPath(parent.getPath() + "/" + organ.getOrganId());
			} else {
				organ.setPath("/" + organ.getOrganId());
			}
			organ.setStatus(Status.normal.name());
			Ar.saveOrUpdate(organ);
		}
	}

	private static String idString = "0000000000000000000000000000000";

	private String getMegaId(String id) {
		return idString.substring(0, idString.length() - id.length()) + id;
	}

	// 协议格式
	// <Message Version=”1.0” EventType=”Request_NotifyResource” Token=”身份认证信息”>
	// <ItemList Type=”类型(平台/机构/视频服务器)” ID="ID地址编码">
	// <Item Method="方法(插入，更新，删除)"
	// Type=”机构类型/视频服务器/摄像头/报警输入”
	// ID="ID地址编码" Name="名称">
	// 设备属性，具体定如下表
	// </Item>
	// ……
	// <Item Method="方法(插入，更新，删除)"
	// Type=”机构类型/视频服务器/摄像头/报警输入”
	// ID="ID地址编码" Name="名称">
	// 设备属性，具体定如下表
	// </Item>
	// </ItemList>
	// </ Message >
	//

	public String getDeviceDetail(Invocation inv,
			@Param("naming") String naming,
			@Param("resourceType") String resourceType) {
		String[] ids = naming.split(":");
		String id = ids[0];
		String serverId = ids[1];
		String cmsId = ids[3];
		Device device = Ar.of(Device.class).get(id + "_" + cmsId);
		device.setType(resourceType);
		DeviceServer server = Ar.of(DeviceServer.class).get(serverId);
		inv.addModel("device", device);
		inv.addModel("server", server);
		return "device-detail";
	}

	public String outerRegist() {
		return "outer-regist";
	}

}
