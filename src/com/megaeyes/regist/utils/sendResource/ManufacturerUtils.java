package com.megaeyes.regist.utils.sendResource;

import com.megaeyes.regist.bean.sendResource.Item;
import com.megaeyes.regist.controllers.GbOuterDeviceController;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.Organ;

public interface ManufacturerUtils {
	public String getOrganStdId(Organ organ);
	public String getPlatformStdId(String cmsId);
	public void queryOuterDevices(GbOuterDeviceController controller,GbPlatform gbPlatform,String stdId);
	public void setItemParentID(Item item,String parentId);
}
