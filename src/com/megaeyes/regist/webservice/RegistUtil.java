package com.megaeyes.regist.webservice;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import net.hight.performance.filter.HPFilter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.megaeyes.regist.bean.NotifyEntityDeviceId;
import com.megaeyes.regist.bean.sendResource.InitEntityByDevice;
import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.dao.SendResourceDao;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.enump.DeviceIdType;
import com.megaeyes.regist.utils.sendResource.ManufacturerUtils;
import com.megaeyes.regist.utils.sendResource.SendResourceHelper;

public class RegistUtil {
	private static Map<String, ManufacturerUtils> map = new HashMap<String, ManufacturerUtils>();
	private static Map<String, SendResourceDao> daoMap = new HashMap<String, SendResourceDao>();
	private static Map<String, InitEntityByDevice> initEntityByDeviceMap = new HashMap<String, InitEntityByDevice>();
	private static Map<String, NotifyEntityDeviceId> notifyEntityDeviceIdMap = new HashMap<String, NotifyEntityDeviceId>();
	private static Log logger = LogFactory.getLog(RegistUtil.class);

	@Resource(name = "cmsIdsMap")
	private Map<String, String> cmsIdsMap;

	@Autowired
	private RegisterDao registerDao;

	public ManufacturerUtils getManufacturerUtils(String manufacturer) {
		if (map.isEmpty()) {
			map = HPFilter.getContext().getBeansOfType(ManufacturerUtils.class);
		}
		try {
			if (StringUtils.isBlank(manufacturer)) {
				throw new Exception();
			}
			return map.get(manufacturer + "Utils");
		} catch (Exception e) {
			logger.warn("gbplatform has not corresponding manufacturer,so return YSUtils");
			return map.get("YSUtils");
		}

	}

	public SendResourceDao getSendResourceDao(GbPlatform platform) {
		if (daoMap.isEmpty()) {
			daoMap = HPFilter.getContext().getBeansOfType(SendResourceDao.class);
		}
		try {
			StringBuilder key = new StringBuilder();
			key.append(platform.getStandardType()).append("_").append(platform.getManufacturer()).append("_").append(platform.getShareType()).append("_SendResourceDao");
			if(daoMap.containsKey(key.toString())){
				return daoMap.get(key.toString());
			}else{
				logger.warn("gbplatform has not corresponding manufacturer,so return GB_YS_PART_SendResourceDao");
				return daoMap.get("GB_YS_PART_SendResourceDao");
			}
		} catch (Exception e) {
			logger.warn("gbplatform has not corresponding manufacturer,so return GB_YS_PART_SendResourceDao");
			return daoMap.get("GB_YS_PART_SendResourceDao");
		}
		
	}
	
	public SendResourceHelper getSendResourceHelper(GbPlatform platform) {
		Map<String, SendResourceHelper> helperMap = HPFilter.getContext().getBeansOfType(SendResourceHelper.class);
		try {
			if("tcp".equals(platform.getProtocol())){
				return helperMap.get("sendResourceTCPHelper");
			}
			return helperMap.get("sendResourceUDPHelper");
		} catch (Exception e) {
			logger.warn("gbplatform has not corresponding protocol,so return default sendResourceUDPHelper");
			return helperMap.get("sendResourceUDPHelper");
		}
		
	}

	public RequestEntity getRequestEntity(String standardType) {
		Map<String, RequestEntity> map = HPFilter.getContext().getBeansOfType(
				RequestEntity.class);
		try {
			return map.get(standardType + "_RequestEntityImpl");
		} catch (Exception e) {
			logger.warn("gbplatform has not corresponding manufacturer,so return GB_RequestEntityImpl");
			return map.get("GB_RequestEntityImpl");
		}
	}

	public InitEntityByDevice getInitEntityByDevice(String standardTye,
			String sendType) {
		if (initEntityByDeviceMap.isEmpty()) {
			initEntityByDeviceMap = HPFilter.getContext().getBeansOfType(
					InitEntityByDevice.class);
		}
		try {
			return initEntityByDeviceMap.get(standardTye + "_" + sendType
					+ "_InitEntityByDevice");
		} catch (Exception e) {
			logger.warn("gbplatform has not corresponding manufacturer,so return GB_OST_InitEntityByDeviceImpl");
			return initEntityByDeviceMap.get("GB_OST_InitEntityByDeviceImpl");
		}
	}

	public NotifyEntityDeviceId getNotifyEntityDeviceId(String sendType) {
		if (notifyEntityDeviceIdMap.isEmpty()) {
			notifyEntityDeviceIdMap = HPFilter.getContext().getBeansOfType(
					NotifyEntityDeviceId.class);
		}
		try {
			return notifyEntityDeviceIdMap.get(sendType
					+ "_NotifyEntityDeviceId");
		} catch (Exception e) {
			logger.warn("gbplatform has not corresponding manufacturer,so return OST_NotifyEntityDeviceId");
			return notifyEntityDeviceIdMap.get("OST_NotifyEntityDeviceId");
		}
	}

	public Regist getRS(RegisterDao dao) {
		if (PlatformUtils.isCityRegist()) {
			return new CityRegistUtil(dao);
		} else {
			return new ProvinceRegistUtil(dao);
		}
	}

	public Regist getRS() {
		return getRS(this.registerDao);
	}

	public String getNamingCmsId(String cmsId) {
		String id = cmsIdsMap.get(cmsId);
		if (StringUtils.isNotBlank(id)) {
			return id;
		} else {
			return DeviceIdType.getCmsId(cmsId);
		}
	}

	public String getMessage(String key) {
		return cmsIdsMap.get(key);
	}

}
