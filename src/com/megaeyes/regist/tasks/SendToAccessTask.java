package com.megaeyes.regist.tasks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.megaeyes.regist.controllers.AutoScanController;
import com.megaeyes.regist.webservice.PlatformUtils;

public class SendToAccessTask implements Runnable{
	private Map<String, String> accessIPMap;
	private String accessIP;
	private String masterAccessIP;
	private AutoScanController ctrl;
	private Long currentFlush;
	
	@Override
	public void run(){
		String cmsId = accessIPMap.get(accessIP);
		Set<String> serverIds = getOnlineStatusSet(accessIP, cmsId,masterAccessIP);
		ctrl.updateDeviceStatus(cmsId, accessIP, serverIds);
		if(ctrl.getFinishFlushMap().get(currentFlush).getAndIncrement()+1 == accessIPMap.size()){
			ctrl.getFinishFlushMap().remove(currentFlush);
			ctrl.resetDeviceStatusByOnline();
		}
	}
	
	public Map<String, String> getAccessIPMap() {
		return accessIPMap;
	}
	public void setAccessIPMap(Map<String, String> accessIPMap) {
		this.accessIPMap = accessIPMap;
	}
	
	private static Set<String> getOnlineStatusSet(final String accessIP, final String cmsId,final String masterAccessIP) {
		// 访问接入服务器
		ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
		ScheduledFuture<String> future = null;
		try {
			Callable<String> task = new Callable<String>() {
				@Override
				public String call() throws Exception {
					Map<String, Object> header = PlatformUtils.getSendParamsByServer(
							masterAccessIP, 9202);
					StringBuilder content = new StringBuilder();
					content.append("<Message><Server ip=\"").append(accessIP).append("\" cmsId=\"").append(cmsId).append("\" /></Message>"); 
					return PlatformUtils.sendToAccess(content.toString(), header);
				}
			};
			future = scheduledThreadPool.schedule(task, 1, TimeUnit.SECONDS);
	        while(!scheduledThreadPool.isTerminated()){
	        	String result = future.get();
				if (result == null) {
					return new HashSet<String>();
				}
				return PlatformUtils.getDeviceStatus(result, accessIP, cmsId);
	        }
			
		}catch (Exception e) {
			future.cancel(true);
			e.printStackTrace();
		}
		return new HashSet<String>();
	}

	public String getAccessIP() {
		return accessIP;
	}

	public void setAccessIP(String accessIP) {
		this.accessIP = accessIP;
	}

	public String getMasterAccessIP() {
		return masterAccessIP;
	}

	public void setMasterAccessIP(String masterAccessIP) {
		this.masterAccessIP = masterAccessIP;
	}

	public AutoScanController getCtrl() {
		return ctrl;
	}

	public void setCtrl(AutoScanController ctrl) {
		this.ctrl = ctrl;
	}
	
	public Long getCurrentFlush() {
		return currentFlush;
	}

	public void setCurrentFlush(Long currentFlush) {
		this.currentFlush = currentFlush;
	}
}
