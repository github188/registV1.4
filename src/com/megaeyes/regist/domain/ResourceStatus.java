package com.megaeyes.regist.domain;

import java.sql.Timestamp;

import com.megaeyes.regist.bean.NotifyItem;
import com.megaeyes.regist.bean.sendResource.Item;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.webservice.RegistUtil;

/**
 * mysql不支持纳秒,所以分成两字段存储
 * 
 * @author molc
 * 
 */
public interface ResourceStatus {
	public Integer getId();
	public void setId(Integer id);
	public boolean isSync();
	public void setSync(boolean sync);
	public String getStatus();
	public void setStatus(String status);
	public Timestamp getChangeTime();
	public void setChangeTime(Timestamp changeTime);
	public int getNanosecond();
	public void setNanosecond(int nanosecond);
	public String getPath();
	
	public ResourceStatus getResource(Item item,GbPlatform platform,RegisterDao registerDao);
	public ResourceStatus getInstance();
	public void setOnline(String online);
	public void updateProperties(Item item, GbPlatform platform,RegisterDao registerDao, ResourceStatus rs);
	public void parentRegist(RegistUtil registUtil, Integer id);
	public void setResourceStatus(ResourceStatus rs,RegisterDao registerDao);
	public void resetOrganPath(ResourceStatus rs,RegisterDao registerDao);
}
