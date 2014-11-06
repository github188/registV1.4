package com.megaeyes.regist.webservice;

import java.util.concurrent.ConcurrentHashMap;

import com.megaeyes.regist.bean.MsgRecord;

public class OrganFinish implements IFinish {
	private RegistCoreImpl regist;

	public OrganFinish(RegistCoreImpl regist) {
		this.regist = regist;
	}

	@Override
	public void finish(MsgRecord record, String cmsId) {
		regist.organFinish(record, cmsId);
	}

	@Override
	public ConcurrentHashMap<String, MsgRecord> getMap() {
		return RegistCoreImpl.organRegistRecordMap;
	}

	@Override
	public String getSQL() {
		return regist.getInsertOrganSQL();
	}

}
