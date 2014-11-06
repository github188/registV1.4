package com.megaeyes.regist.bean.sendResource;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.mega.jdom.Document;
import com.mega.jdom.Element;
import com.mega.jdom.input.SAXBuilder;
import com.megaeyes.regist.bean.NotifyItem;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.DeviceStatus;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.IGroup;
import com.megaeyes.regist.domain.NotifyStatus;
import com.megaeyes.regist.domain.OrganStatus;
import com.megaeyes.regist.domain.SubscribeEvent;
import com.megaeyes.regist.enump.DeviceIdType;
import com.megaeyes.regist.other.Status;
import com.megaeyes.regist.utils.sendResource.GbDomainMapFactory;
import com.megaeyes.regist.webservice.PlatformUtils;

public class NotifyEntity implements IEntity {
	private String SN;
	private String cmdType;
	private String deviceID;
	private String sumNum;
	private String note;
	private String fromDeviceID;
	private GbPlatform gbPlatform;
	private List<NotifyItem> items = new ArrayList<NotifyItem>();

	public String getSN() {
		return SN;
	}

	public void setSN(String SN) {
		this.SN = SN;
	}

	public String getCmdType() {
		return cmdType;
	}

	public void setCmdType(String cmdType) {
		this.cmdType = cmdType;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getSumNum() {
		return sumNum;
	}

	public void setSumNum(String sumNum) {
		this.sumNum = sumNum;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getFromDeviceID() {
		return fromDeviceID;
	}

	public void setFromDeviceID(String fromDeviceID) {
		this.fromDeviceID = fromDeviceID;
	}

	public List<NotifyItem> getItems() {
		return items;
	}

	public void setItems(List<NotifyItem> items) {
		this.items = items;
	}

	public Document getDoc() {
		Element root = new Element("Notify");
		PlatformUtils.addElByText(root, this.getClass().getDeclaredFields(),
				this, "items,fromDeviceID,note,registUtil,gbPlatform");
		Element deviceListEl = new Element("DeviceList");
		deviceListEl.setAttribute("Num", "" + this.items.size());
		root.addContent(deviceListEl);
		Field[] fields = NotifyItem.class.getDeclaredFields();
		Field[] itemFields = Item.class.getDeclaredFields();
		List<Field> list = new ArrayList<Field>();
		for (Field f : fields) {
			list.add(f);
		}
		for (Field f : itemFields) {
			list.add(f);
		}

		Field[] temp = new Field[list.size()];
		list.toArray(temp);
		for (NotifyItem item : this.items) {
			Element itemEl = new Element("Item");
			deviceListEl.addContent(itemEl);
			if ("ON,OFF,DEL".indexOf(item.getEvent()) > -1) {
				PlatformUtils.addElByTextOnlyFields(itemEl, temp, item,
						"deviceID,event");
			} else {
				PlatformUtils.addElByText(itemEl, temp, item, "");
			}

		}
		Document document = new Document();
		document.setRootElement(root);
		return document;
	}
	public Document getOrganDoc() {
		Element root = new Element("Notify");
		PlatformUtils.addElByText(root, this.getClass().getDeclaredFields(),
				this, "items,fromDeviceID,note,registUtil,gbPlatform");
		Element deviceListEl = new Element("DeviceList");
		deviceListEl.setAttribute("Num", "" + this.items.size());
		root.addContent(deviceListEl);
		Field[] fields = NotifyItem.class.getDeclaredFields();
		Field[] itemFields = Item.class.getDeclaredFields();
		List<Field> list = new ArrayList<Field>();
		for (Field f : fields) {
			list.add(f);
		}
		for (Field f : itemFields) {
			list.add(f);
		}
		
		Field[] temp = new Field[list.size()];
		list.toArray(temp);
		for (NotifyItem item : this.items) {
			Element itemEl = new Element("Item");
			deviceListEl.addContent(itemEl);
			if ("ON,OFF,DEL".indexOf(item.getEvent()) > -1) {
				PlatformUtils.addElByTextOnlyFields(itemEl, temp, item,
						"deviceID,event");
			} else {
				PlatformUtils.addElByTextOnlyFields(itemEl, temp, item,
						"deviceID,event,name");
			}
			
		}
		Document document = new Document();
		document.setRootElement(root);
		return document;
	}

	public void initItemByBaseDeviceStatus(DeviceStatus deviceStatus, SubscribeEvent subscribeEvent) {
		this.sumNum = "1";
		NotifyItem item = new NotifyItem();
		Device device = GbDomainMapFactory.getInstance().getDeviceForPush(deviceStatus.getDeviceId());
		item.setDeviceID(device.getStdId());
		if (deviceStatus.getStatus().equals(Status.delete.name())) {
			item.setEvent("DEL");
		} else {
			item.setEvent(deviceStatus.getStatus().toUpperCase());
			StringBuilder parentId = new StringBuilder();
			item.setDeviceID(device.getStdId());
			item.setName(device.getName());
			DeviceServer  server = device.getDeviceServer();
			if (server != null) {
				item.setAddress(server.getLocation());
				item.setManufacturer(server.getManufacturer());
				item.setModel(server.getModel());
				item.setIPAddress(server.getIP());
				item.setStatus(device.getOnline());
				item.setParental("0");
				
			}
			item.setCivilCode(DeviceIdType.getCivilCodeByDeviceId(device.getGbOrgan().getStdId()));
			if(DeviceIdType.isVirtrueOrgan(device.getGbOrgan().getStdId())){
				parentId.append(device.getGbOrgan().getStdId()).append("/");
			}
			parentId.append(this.getDeviceID());
			item.setParentID(parentId.toString());
			item.setLongitude(device.getLongitude());
			item.setLatitude(device.getLatitude());
		}
		this.getItems().add(item);
	}


	public void initItemByBaseOrganStatus(NotifyStatus notifyStatus) {
			this.sumNum = "1";
			OrganStatus status = (OrganStatus) notifyStatus;
			
			NotifyItem item = new NotifyItem();
			IGroup organ = GbDomainMapFactory.getInstance().getOrganByGbOrgnId(status.getOrganId());
			item.setDeviceID(organ.getStdId());
			if (status.getStatus().equals(Status.delete)) {
				item.setEvent("DEL");
			} else {
				item.setEvent(status.getStatus().toUpperCase());
				item.setName(organ.getName());
			}
			this.getItems().add(item);
	}

	public void initItemByOnlineDeviceStatus(DeviceStatus deviceStatus) {
		this.sumNum = "1";
		NotifyItem item = new NotifyItem();
		Device device = GbDomainMapFactory.getInstance().getDeviceForPush(deviceStatus.getDeviceId());
		item.setDeviceID(device.getStdId());
		if (deviceStatus.getOnline() == null) {
			deviceStatus.setOnline("OFF");
		}
		item.setEvent(deviceStatus.getOnline());
		this.getItems().add(item);
	}


	private NotifyEntity() {
	}

	public static NotifyEntity getInstance(SubscribeEvent subscribeEvent,GbPlatform gbPlatform) {
		NotifyEntity entity = new NotifyEntity();
		entity.setCmdType("Catalog");
		entity.setDeviceID(subscribeEvent.getDeviceId());
		entity.setSN("" + PlatformUtils.getSN());
		entity.setGbPlatform(gbPlatform);
		entity.setFromDeviceID(gbPlatform.getCmsId());
		return entity;
	}

	public static NotifyEntity getInstance() {
		NotifyEntity entity = new NotifyEntity();
		return entity;
	}

	@SuppressWarnings("unchecked")
	public void init(String message, String fromDeviceId) {
		NotifyEntity entity = this;
		Reader in = null;
		try {
			in = new BufferedReader(new StringReader(message));
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(in);
			Element root = doc.getRootElement();
			entity.setCmdType(root.getChildText("CmdType"));
			entity.setDeviceID(root.getChildText("DeviceID"));
			entity.setSN(root.getChildText("SN"));
			entity.setFromDeviceID(fromDeviceId);
			Element deviceListEl = root.getChild("DeviceList");
			if (deviceListEl != null) {
				List<Element> itemEl = deviceListEl.getChildren();
				for (Element el : itemEl) {
					NotifyItem item = new NotifyItem();
					item.initItem(el);
					item.setParent(entity);
					entity.getItems().add(item);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GbPlatform getGbPlatform() {
		return gbPlatform;
	}

	public void setGbPlatform(GbPlatform gbPlatform) {
		this.gbPlatform = gbPlatform;
	}
}
