package com.megaeyes.regist.webservice;

import java.util.concurrent.ConcurrentHashMap;

import com.megaeyes.regist.bean.MsgRecord;

public class DeviceFinish implements IFinish {
	private RegistCoreImpl regist;

	public DeviceFinish(RegistCoreImpl regist) {
		this.regist = regist;
	}

	@Override
	public void finish(MsgRecord record, String cmsId) {
		regist.deviceFinish(record, cmsId);
	}

	@Override
	public ConcurrentHashMap<String, MsgRecord> getMap() {
		return RegistCoreImpl.deviceRegistRecordMap;
	}

	@Override
	public String getSQL() {
		return regist.getInsertDevicesSQL();
	}

}
