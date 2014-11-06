package com.megaeyes.regist.webservice;

import java.util.concurrent.ConcurrentHashMap;

import com.megaeyes.regist.bean.MsgRecord;

public class VisFinish implements IFinish {
	private RegistCoreImpl regist;

	public VisFinish(RegistCoreImpl regist) {
		this.regist = regist;
	}

	@Override
	public void finish(MsgRecord record, String cmsId) {
		regist.visFinish(record, cmsId);
	}

	@Override
	public ConcurrentHashMap<String, MsgRecord> getMap() {
		return RegistCoreImpl.serverRegistRecordMap;
	}

	@Override
	public String getSQL() {
		return regist.getInsertServerSQL();
	}

}
