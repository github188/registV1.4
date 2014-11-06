package com.megaeyes.regist.utils.sendResource;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.bean.sendResource.Item;
import com.megaeyes.regist.controllers.GbOuterDeviceController;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.utils.Ar;

/**
 * 华三特性工具类
 * @author molc
 * 
 */
@Component("H3CUtils")
public class H3CUtils implements ManufacturerUtils{

	/**
	 * 获取机构的标准ID
	 * 
	 * @param organ
	 * @return
	 */
	@Override
	public String getOrganStdId(Organ organ) {
		if (StringUtils.isBlank(organ.getStdId())) {
			organ.setStdId(getOrganStdIdByCmsId(organ.getCmsId())+"002000000000");
//			Ar.update(organ);
		}
		return organ.getStdId();
	}
	
	@SuppressWarnings("rawtypes")
	private synchronized String getOrganStdIdByCmsId(String cmsId) {
		List list = Ar
				.sql("select max(substr(std_id,7,2)) from organ where cms_id=?",
						cmsId);
		if (list.get(0) == null) {
			return cmsId.substring(0,6) + "01";
		} else {
			String maxIdStr = (String) list.get(0);
			int maxId = Integer.parseInt(maxIdStr) + 1;
			if (maxId >= 10) {
				return cmsId.substring(0,6) + maxId;
			} else {
				return cmsId.substring(0,6) + "0" + maxId;
			}
		}

	}
	
	@Override
	public String getPlatformStdId(String cmsId) {
		return cmsId+"00002000000000";
	}
	
	@Override
	public void queryOuterDevices(GbOuterDeviceController controller,
			GbPlatform gbPlatform, String stdId) {
	}
	
	@Override
	public void setItemParentID(Item item,String parentId) {
		// TODO Auto-generated method stub
	}

}
