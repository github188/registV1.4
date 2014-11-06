package com.megaeyes.regist.utils.sendResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hight.performance.filter.HPFilter;

import com.megaeyes.regist.dao.DomainDao;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.GbDevice;
import com.megaeyes.regist.domain.GbOrgan;
import com.megaeyes.regist.domain.IGroup;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.Platform;

public class GbDomainMapFactory {
	private volatile static GbDomainMapFactory uniqueInstance;
	private DomainDao dao;
	
	private Map<Integer,Organ> organMap = new HashMap<Integer,Organ>();
	
	private Map<Integer,Platform> platformMap = new HashMap<Integer,Platform>();
	
	private Map<Integer,Device> deviceMap = new HashMap<Integer,Device>();
	
	private Map<String,DeviceServer> serverMap = new HashMap<String, DeviceServer>();
	
	private Map<Integer,GbOrgan> gbOrganMap =new HashMap<Integer,GbOrgan>();
	private Map<Integer,GbDevice> gbDeviceMap =new HashMap<Integer,GbDevice>();
	
	
	public static GbDomainMapFactory getInstance() {
		if (uniqueInstance == null) {
			synchronized (GbDomainMapFactory.class) {
				if (uniqueInstance == null) {
					uniqueInstance = new GbDomainMapFactory();
					uniqueInstance.dao = HPFilter.getContext().getBean(DomainDao.class);
					uniqueInstance.initMap();
				}
			}
		}
		return uniqueInstance;
	}
	
	public  void initMap() {
		initPlatform();
		initOrganMap();
		initDeviceMap();
		initServer();
		initGbOrganMap();
		initGbDeviceMap();
	}
	
	public void initGbDeviceMap(){
		gbDeviceMap.clear();
		List<GbDevice> devices = dao.find(GbDevice.class,"select * from gb_device where suspend=false");
		for(GbDevice device : devices){
			gbDeviceMap.put(device.getId(), device);
		}
	}
	
	public void initGbOrganMap(){
		gbOrganMap.clear();
		List<GbOrgan> organs = dao.find(GbOrgan.class, "select * from gb_organ where suspend=false");
		for(GbOrgan organ : organs){
			gbOrganMap.put(organ.getId(), organ);
		}
	}

	public void initOrganMap(){
		organMap.clear();
		List<Organ> list = dao.find(Organ.class);
		for(Organ organ : list){
			organMap.put(organ.getId(), organ);
		}
	}
	
	public void initPlatform(){
		platformMap.clear();
		List<Platform> list = dao.find(Platform.class);
		for(Platform platform : list){
			platformMap.put(platform.getId(), platform);
		}
	}
	
	public void initDeviceMap(){
		deviceMap.clear();
		List<Device> list = dao.find(Device.class);
		for(Device device: list){
			deviceMap.put(device.getId(), device);
		}
	}
	
	public void initServer(){
		serverMap.clear();
		List<DeviceServer> list = dao.find(DeviceServer.class);
		for(DeviceServer server : list){
			serverMap.put(server.getServerId(), server);
		}
	}
	
	public Map<Integer, Platform> getPlatformMap() {
		return platformMap;
	}

	
	public Map<Integer, Organ> getOrganMap() {
		return organMap;
	}

	public Map<Integer, Device> getDeviceMap() {
		return deviceMap;
	}

	public Map<String, DeviceServer> getServerMap() {
		return serverMap;
	}

	public static void main(String[] args) {
		long heapsize=Runtime.getRuntime().totalMemory();
	    System.out.println("heapsize is::"+heapsize);
	}

	
	public Map<Integer, GbOrgan> getGbOrganMap() {
		return gbOrganMap;
	}

	public void setGbOrganMap(Map<Integer, GbOrgan> gbOrganMap) {
		this.gbOrganMap = gbOrganMap;
	}

	public IGroup getOrganByGbOrgnId(Integer gbOrganId){
		return this.gbOrganMap.get(gbOrganId).getOrgan();
	}

	public Map<Integer, GbDevice> getGbDeviceMap() {
		return gbDeviceMap;
	}

	public void setGbDeviceMap(Map<Integer, GbDevice> gbDeviceMap) {
		this.gbDeviceMap = gbDeviceMap;
	}
	
	public Device getDeviceByGbDeviceId(Integer gbDeviceId){
		return this.getDeviceMap().get(this.getGbDeviceMap().get(gbDeviceId).getDeviceId());
	}
	
	public Device getDeviceForPush(Integer gbDeviceId){
		GbDevice gbDevice = GbDomainMapFactory.getInstance().getGbDeviceMap().get(gbDeviceId);
		Device device = GbDomainMapFactory.getInstance().getDeviceByGbDeviceId(gbDeviceId);
		device.setGbOrgan(GbDomainMapFactory.getInstance().getOrganByGbOrgnId(gbDevice.getOrganId()));
		device.setDeviceServer(GbDomainMapFactory.getInstance().getServerMap().get(device.getServerId()));
		return device;
	}
}