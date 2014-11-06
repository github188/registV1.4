package com.megaeyes.regist.utils.sendResource;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.dao.GbPushDao;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.GbOrgan;
import com.megaeyes.regist.domain.GbPlatform;

@Component("sendResourceUDPHelper")
public class SendResourceUDPHelper implements SendResourceHelper {

	@Autowired
	private GBUtils gbUtils;

	@Autowired
	private GbPushDao gbPushDao;

	private ExecutorService executorService = Executors.newFixedThreadPool(10);
	CompletionService<String> taskCompletionService = new ExecutorCompletionService<String>(
			executorService);

	public void sendResource(RequestEntity entity) {
		// 直接对接的平台
		GbPlatform platform = entity.getGbPlatform();
		SendParam sendParam = new SendParam(platform, entity);
		// 获取要共享数据的所有平台编号
		if (entity.getDeviceID().equals(platform.getChildCmsId())) {
			// 按顺序获取所有子平台信息,各平台机构,视频服务器,摄像头,发完一类,再发一类
			entity.setSumNum(gbPushDao.getSumNum(platform.getId()));
			List<Integer> gbOrganId = gbPushDao.getOrgans(platform.getId(), 1);
			sendOrgans(sendParam, gbOrganId);// 发送机构
			sendDevices(sendParam, entity, platform,1);// 发送终端设备
		} else {
			// 按顺序获取所有子平台信息,各平台机构,视频服务器,摄像头,发完一类,再发一类
			GbOrgan organ = gbPushDao.getGbOrganByStdId(entity.getDeviceID());
			if(organ != null){
				entity.setSumNum(gbPushDao.getSumNumByOrgan(platform.getId(), organ));
				List<Integer> gbOrganId = gbPushDao.getOrganByParent(organ,
						platform.getId(), 1);
				sendOrgans(sendParam, gbOrganId);// 发送机构
				sendDevicesByOrgan(sendParam, entity, platform, organ, 1);// 发送终端设备
			}
		}
	}

	private void sendDevicesByOrgan(SendParam sendParam, RequestEntity entity,
			GbPlatform platform, GbOrgan organ, int pageNO) {
		List<Device> deviceList = gbPushDao.getDevicesByOrgan(organ,
				platform.getId(), pageNO);
		if (deviceList.size() > 0) {
			executeSendDevicesTask(entity, platform, deviceList);
			sendDevicesByOrgan(sendParam, entity, platform, organ, ++pageNO);
		}
	}

	private void executeSendDevicesTask(RequestEntity entity,
			GbPlatform platform, List<Device> deviceList) {
		List<Callable<String>> deviceCallables = gbUtils
				.createDeviceCallableList(entity, platform, deviceList); // 创建发送设备的任务
		for (Callable<String> callable : deviceCallables) {
			taskCompletionService.submit(callable);
		}
		for (int i = 0; i < deviceCallables.size(); i++) {
			try {
				taskCompletionService.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendDevices(SendParam sendParam, RequestEntity entity,
			GbPlatform platform, int pageNO) {
		List<Device> deviceList = gbPushDao
				.getDevices(platform.getId(), pageNO);
		if (deviceList.size() > 0) {
			executeSendDevicesTask(entity, platform, deviceList);
			sendDevices(sendParam, entity, platform, ++pageNO);
		}
	}

	/**
	 * gbIds已经是有序的，先父后子，后兄弟
	 * 
	 */
	public void sendOrgans(SendParam sendParam, List<Integer> gbIds) {
		for (Integer id : gbIds) {
			gbUtils.sendMsg(sendParam.getPlatform().getSipServer(),
					gbUtils.getOrganContent(id, sendParam.getEntity()),
					sendParam.getEntity().getFromDeviceID());
		}
	}

}
