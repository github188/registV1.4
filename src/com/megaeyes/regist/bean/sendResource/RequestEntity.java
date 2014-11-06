package com.megaeyes.regist.bean.sendResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import com.mega.jdom.Document;
import com.mega.jdom.Element;
import com.mega.jdom.input.SAXBuilder;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.DeviceStatus;
import com.megaeyes.regist.domain.GbOrgan;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.IGroup;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.domain.ServerStatus;
import com.megaeyes.regist.enump.DeviceIdType;
import com.megaeyes.regist.utils.sendResource.GbDomainMapFactory;
import com.megaeyes.regist.webservice.PlatformUtils;
import com.megaeyes.regist.webservice.RegistUtil;

public class RequestEntity implements Cloneable, IEntity {
	private String SN;
	private String cmdType;
	private String deviceID;
	private String sumNum;
	private String note;
	private String fromDeviceID;
	private List<Item> itemList = new ArrayList<Item>();
	private List<OrganItem> organList = new ArrayList<OrganItem>();
	private GbPlatform gbPlatform;
	private RegistUtil registUtil;
	private Timestamp changeTime;
	private int nanosecond;
	private RegisterDao registerDao;

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

	public List<Item> getItemList() {
		return itemList;
	}

	public void setItemList(List<Item> itemList) {
		this.itemList = itemList;
	}

	public void init(String cmdType, String deviceId, String sn,
			String fromDeviceId) {
		RequestEntity entity = this;
		entity.setCmdType(cmdType);
		entity.setDeviceID(deviceId);
		entity.setSN(sn);
		entity.setFromDeviceID(fromDeviceId);
		entity.setRegistUtil(registUtil);
	}
	
	@SuppressWarnings("unchecked")
	public void init(String message, String fromDeviceId) {
		RequestEntity entity = this;
		entity.setRegistUtil(registUtil);
		Reader in = null;
		try {
			in = new BufferedReader(new StringReader(message));
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(in);
			in.close();
			Element root = doc.getRootElement();
			entity.setCmdType(root.getChildText("CmdType"));
			entity.setDeviceID(root.getChildText("DeviceID"));
			entity.setSN(root.getChildText("SN"));
			entity.setFromDeviceID(fromDeviceId);
			Element deviceListEl = root.getChild("DeviceList");
			if (deviceListEl != null) {
				List<Element> itemEl = deviceListEl.getChildren();
				for (Element el : itemEl) {
					Item item = new Item();
					item.initItem(el);
					item.setParent(entity);
					entity.getItemList().add(item);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void init(String msg) {
		BufferedReader readerIn = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			readerIn = new BufferedReader(new StringReader(msg));
			Document doc = builder.build(readerIn);
			readerIn.close();
			Element repsonse = doc.getRootElement();
			
			this.setRegistUtil(registUtil);
			this.setCmdType(repsonse.getChildText("CmdType"));
			this.setDeviceID(repsonse.getChildText("DeviceID"));
			this.setSN(repsonse.getChildText("SN"));
			this.setSumNum(repsonse.getChildText("SumNum"));
			Element deviceListEl = repsonse.getChild("DeviceList");
			if (deviceListEl != null) {
				List<Element> itemEl = deviceListEl.getChildren();
				for (Element el : itemEl) {
					Item item = new Item();
					item.initItem(el);
					item.setParent(this);
					this.getItemList().add(item);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (readerIn != null) {
					readerIn.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	

	public RequestEntity clone(){
		RequestEntity clone = new RequestEntity();
		BeanUtils.copyProperties(this, clone, new String[]{"itemList","organList"});
		return clone;
	}

	public Document getQueryDoc() {
		Element root = new Element("Query");
		Element cmdTypeEl = new Element("CmdType");
		cmdTypeEl.setText(this.getCmdType());
		root.addContent(cmdTypeEl);

		Element SNEl = new Element("SN");
		SNEl.setText(this.getSN());
		root.addContent(SNEl);

		Element DeviceIDEl = new Element("DeviceID");
		DeviceIDEl.setText(this.getDeviceID());
		root.addContent(DeviceIDEl);

		Document doc = new Document(root);
		return doc;
	}

	public Document getDoc() {
		Element root = new Element("Response");
		PlatformUtils
				.addElByText(
						root,
						RequestEntity.class.getDeclaredFields(),
						this,
						"itemList,onlineStatusSet,note,registUtil,fromDeviceID,gbPlatform,changeTime,nanosecond,registerDao,organList");
		Element deviceListEl = new Element("DeviceList");
		deviceListEl.setAttribute("Num", "" + this.getItemList().size());
		root.addContent(deviceListEl);
		for (Item item : this.getItemList()) {
			Element itemEl = new Element("Item");
			deviceListEl.addContent(itemEl);
			PlatformUtils.addElByText(itemEl, item.getClass()
					.getDeclaredFields(), item, "parent,itemInfo");
		}
		Document document = new Document();
		document.setRootElement(root);
		return document;
	}
	
	public Document getOrganDoc() {
		Element root = new Element("Response");
		PlatformUtils
		.addElByText(
				root,
				RequestEntity.class.getDeclaredFields(),
				this,
				"itemList,organList,onlineStatusSet,note,registUtil,fromDeviceID,gbPlatform,changeTime,nanosecond,registerDao");
		Element deviceListEl = new Element("DeviceList");
		deviceListEl.setAttribute("Num", "" + this.getOrganList().size());
		root.addContent(deviceListEl);
		for (OrganItem item : this.getOrganList()) {
			Element itemEl = new Element("Item");
			deviceListEl.addContent(itemEl);
			try {
				PlatformUtils.addElByTextIgnoreEmpty(itemEl,item.getClass().getDeclaredFields(), item, "parent");
			} catch (SecurityException e) {
				e.printStackTrace();
			} 
		}
		Document document = new Document();
		document.setRootElement(root);
		return document;
	}

	public void initEntityByDevices(List<DeviceStatus> deviceStatusList,String parentStdId) {
		registUtil.getInitEntityByDevice(
				this.getGbPlatform().getStandardType(),
				this.getGbPlatform().getSendType()).initEntity(
				deviceStatusList, this,parentStdId);
	}

	public void initEntityByDevice(Device device) {
		StringBuilder parentId = new StringBuilder();
		Item item = new Item();
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
		this.getItemList().add(item);
	}

	public void initEntityByServer(ServerStatus status) {
//		this.deviceID = status.getServer().getOrganStdId();
		Item item = new Item();
		item.setDeviceID(status.getServer().getStdId());
		item.setName(status.getServer().getName());
		item.setManufacturer(status.getServer().getManufacturer());
		item.setModel(status.getServer().getModel());
		item.setCivilCode(status.getServer().getOrganStdId());
		item.setAddress(status.getServer().getLocation());
		item.setIPAddress(status.getServer().getIP());
		item.setStatus(status.getOnline());
		registUtil.getManufacturerUtils(this.getGbPlatform().getManufacturer())
				.setItemParentID(item, this.getDeviceID());
		this.getItemList().add(item);
	}

	public String getPlatformStdId(Platform platform) {
		String platformStdId = null;
		if (StringUtils.isNotBlank(platform.getGbPlatformCmsId())) {
			platformStdId = platform.getGbPlatformCmsId();
		} else {
			platformStdId =platform.getCmsId();
		}
		return platformStdId;
	}

	public void initEntityByPlatforms(List<Platform> platforms, Platform parent) {
		for (Platform platform : platforms) {
			String platformStdId = getPlatformStdId(platform);
			Item item = new Item();
			item.setDeviceID(platformStdId);
			item.setName(platform.getName());
			item.setCivilCode(item.getDeviceID());
			item.setAddress(platform.getName());
			registUtil.getManufacturerUtils(
					this.getGbPlatform().getManufacturer()).setItemParentID(
					item, this.getDeviceID());
			item.setParentID(getPlatformStdId(parent));
			this.getItemList().add(item);
		}
	}

	public void initEntityByPlatform(Platform platform,Platform parent) {
		String platformStdId = getPlatformStdId(platform);
		Item item = new Item();
		item.setDeviceID(platformStdId);
		item.setName(platform.getName());
		item.setCivilCode(item.getDeviceID());
		item.setAddress(platform.getName());
		registUtil.getManufacturerUtils(this.getGbPlatform().getManufacturer())
				.setItemParentID(item, this.getDeviceID());
		item.setParentID(getPlatformStdId(parent));
		this.getItemList().add(item);
	}

	public void initEntityByOrgan(GbOrgan organ) {
		initByOrgan(organ);
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

	public GbPlatform getGbPlatform() {
		return gbPlatform;
	}

	public void setGbPlatform(GbPlatform gbPlatform) {
		this.gbPlatform = gbPlatform;
	}

	public RegistUtil getRegistUtil() {
		return registUtil;
	}

	public void setRegistUtil(RegistUtil registUtil) {
		this.registUtil = registUtil;
	}

	public Timestamp getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(Timestamp changeTime) {
		this.changeTime = changeTime;
	}

	public int getNanosecond() {
		return nanosecond;
	}

	public void setNanosecond(int nanosecond) {
		this.nanosecond = nanosecond;
	}

	public RegisterDao getRegisterDao() {
		return registerDao;
	}

	public void setRegisterDao(RegisterDao registerDao) {
		this.registerDao = registerDao;
	}
	
	private void initByOrgan(GbOrgan organ){
		IGroup  group = organ.getOrgan();
		OrganItem item = new OrganItem();
		item.setDeviceID(group.getStdId());
		item.setName(group.getName());
		if(DeviceIdType.isVirtrueOrgan(group.getStdId())){
			item.setBusinessGroupID(getBusinessGroupIDByOrgan(organ));
			item.setParentID(getParentIDByOrgan(organ));
		}
		this.organList.add(item);
	}

	private String getParentIDByOrgan(GbOrgan organ) {
		GbOrgan parent = organ.getParent();
		if(parent!=null){
			if(DeviceIdType.isVirtrueOrgan(parent.getOrgan().getStdId())){
				return parent.getOrgan().getStdId();
			}else{
				return getParentIDByOrgan(parent);
			}
		}else{
			return this.getDeviceID();
		}
	}

	private String getBusinessGroupIDByOrgan(GbOrgan organ) {
		GbOrgan parent = GbDomainMapFactory.getInstance().getGbOrganMap().get(organ.getParentId());
		if(parent!=null){
			if(DeviceIdType.isBusinessOrgan(parent.getOrgan().getStdId())){
				return parent.getOrgan().getStdId();
			}else{
				return getBusinessGroupIDByOrgan(parent);
			}
		}else{
			return "";
		}
	}

	public void initEntityByServers(List<ServerStatus> serverStatusList,
			String stdId) {
		for(ServerStatus status : serverStatusList){
			Item item = new Item();
			item.setDeviceID(status.getServer().getStdId());
			item.setName(status.getServer().getName());
			item.setManufacturer(status.getServer().getManufacturer());
			item.setModel(status.getServer().getModel());
			item.setCivilCode(status.getServer().getOrganStdId());
			item.setAddress(status.getServer().getLocation());
			item.setIPAddress(status.getServer().getIP());
			item.setStatus(status.getOnline());
			registUtil.getManufacturerUtils(this.getGbPlatform().getManufacturer())
					.setItemParentID(item, this.getDeviceID());
			this.getItemList().add(item);
		}
		
	}

	public List<OrganItem> getOrganList() {
		return organList;
	}

	public void setOrganList(List<OrganItem> organList) {
		this.organList = organList;
	}

}
