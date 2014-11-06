package com.megaeyes.regist.webservice;

import java.util.concurrent.ConcurrentHashMap;

import com.megaeyes.regist.bean.MsgRecord;

public interface IFinish {
	public void finish(MsgRecord record,String cmsId);
	public String getSQL();
	public ConcurrentHashMap<String, MsgRecord> getMap();
}
