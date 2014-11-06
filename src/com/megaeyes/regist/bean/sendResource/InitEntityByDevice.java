package com.megaeyes.regist.bean.sendResource;

import java.util.List;

import com.megaeyes.regist.domain.DeviceStatus;

public interface InitEntityByDevice {
	public void initEntity(List<DeviceStatus> deviceStatusList,RequestEntity entity,String parentStdId);
	public void initEntity(DeviceStatus devictStatus,RequestEntity entity);
}
