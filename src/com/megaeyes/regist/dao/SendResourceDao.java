package com.megaeyes.regist.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.domain.DeviceStatus;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.OrganStatus;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.domain.ServerStatus;
import com.megaeyes.regist.utils.sendResource.SendParam;

public interface SendResourceDao {

	public long getSumNumByCmsIds(Collection<String> cmsIds, Class<?> clazz,
			RequestEntity entity);

	public String getSumNumByOrgan(SendParam param);

	public long getSumNumByCmsId(Collection<String> cmsIds, GbPlatform gbPlatform,
			RequestEntity entity);

	public List<DeviceStatus> getDeviceByServer(SendParam param);

	public List<Platform> getPlatformsByIds(RequestEntity entity, GbPlatform platform);

	public List<ServerStatus> getServersByOrganStdId(SendParam param);

	public List<OrganStatus> getOrgans(SendParam param);

	public List<OrganStatus> getOrganByParent(SendParam param);

	public List<ServerStatus> getServers(SendParam param);

	public List<ServerStatus> getServersByOrgan(SendParam param);

	public List<DeviceStatus> getDevices(SendParam param, int offset);

	public List<DeviceStatus> getDevicesByOrgan(SendParam param, int offset);

	public int getSumNumByServer(SendParam sendParam);

	public List<Platform> getPlatformsByParent(RequestEntity entity,
			GbPlatform gbplatform, Platform parent);


	public List<OrganStatus> getChildrenByParentOrgan(SendParam param, Organ parent);

	public List<OrganStatus> getTopOrganByCmsId(SendParam sendParam,
			Set<String> cmsIds);

	public List<ServerStatus> getServersByOrganWithId(SendParam sendParam);

	public List<DeviceStatus> getDevicesByOrganWithId(SendParam sendParam,int offset);

	public Map<String,List<DeviceStatus>> getDevicesOrderByServer(SendParam sendParam, int offset);

	public Map<String, List<ServerStatus>> getServersOrderByOrgan(
			SendParam sendParam, int offset);
	public Map<String,List<ServerStatus>> getServersGroupByOrganWithId(SendParam sendParam);

	public Map<String, List<DeviceStatus>> getDevicesGroupByServerWithOrganId(
			SendParam param, int offset);

	public void sendOrganMsgByCmsId(SendParam sendParam, Set<String> cmsIds);




}
