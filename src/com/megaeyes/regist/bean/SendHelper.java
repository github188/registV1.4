package com.megaeyes.regist.bean;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.megaeyes.regist.bean.sendResource.RequestEntity;
import com.megaeyes.regist.domain.DeviceStatus;
import com.megaeyes.regist.domain.ServerStatus;
import com.megaeyes.utils.Ar;

public class SendHelper {
	private Queue<String> platformQueue = new ConcurrentLinkedQueue<String>();
	private Queue<String> organQueue = new ConcurrentLinkedQueue<String>();
	private Queue<String> serverQueue = new ConcurrentLinkedQueue<String>();
	private Map<String, List<DeviceStatus>> deviceMap;
	private Map<String, List<ServerStatus>> serverMap;
	private ExecutorService deviceExec = Executors.newSingleThreadExecutor();
	private ExecutorService organExec = Executors.newSingleThreadExecutor();
	private ExecutorService serverExec = Executors.newSingleThreadExecutor();
	private boolean deviceComplete;
	private boolean serverComplete;
	private boolean organComplete;
	private RequestEntity entity;

	private CompletionService<Boolean> deviceCompletionService = new ExecutorCompletionService<Boolean>(
			deviceExec);
	private CompletionService<Boolean> organCompletionService = new ExecutorCompletionService<Boolean>(
			organExec);
	private CompletionService<Boolean> serverCompletionService = new ExecutorCompletionService<Boolean>(
			serverExec);

	public void submitOrganTask(Runnable task, boolean tag) {
		Future<Boolean> result = organCompletionService.submit(task, tag);
		try {
			this.organComplete = result.get();
			if(isOrganComplete() && Integer.parseInt(entity.getSumNum())>0){
				updateOrganStatus(entity);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void submitDeviceTask(Runnable task, boolean tag) {
		Future<Boolean> result = deviceCompletionService.submit(task, tag);
		try {
			this.deviceComplete = result.get();
			if(this.isDeviceComplete() && Integer.parseInt(entity.getSumNum())>0){
				updateDeviceStatus(entity);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void submitServerTask(Runnable task, boolean tag) {
		Future<Boolean> result = serverCompletionService.submit(task, tag);
		try {
			this.serverComplete = result.get();
			if(this.isServerComplete() && Integer.parseInt(entity.getSumNum())>0){
				updateServerStatus(entity);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public Queue<String> getPlatformQueue() {
		return platformQueue;
	}

	public void setPlatformQueue(Queue<String> platformQueue) {
		this.platformQueue = platformQueue;

	}

	public Queue<String> getOrganQueue() {
		return organQueue;

	}

	public void setOrganQueue(Queue<String> organQueue) {
		this.organQueue = organQueue;
	}

	public Queue<String> getServerQueue() {
		return serverQueue;

	}

	public void setServerQueue(Queue<String> serverQueue) {
		this.serverQueue = serverQueue;

	}

	public ExecutorService getDeviceExec() {
		return deviceExec;
	}

	public void setDeviceExec(ExecutorService deviceExec) {
		this.deviceExec = deviceExec;
	}

	public ExecutorService getOrganExec() {
		return organExec;
	}

	public void setOrganExec(ExecutorService organExec) {
		this.organExec = organExec;
	}

	public ExecutorService getServerExec() {
		return serverExec;
	}

	public void setServerExec(ExecutorService serverExec) {
		this.serverExec = serverExec;
	}

	public Map<String, List<DeviceStatus>> getDeviceMap() {
		return deviceMap;
	}

	public void setDeviceMap(Map<String, List<DeviceStatus>> deviceMap) {
		this.deviceMap = deviceMap;
	}

	public Map<String, List<ServerStatus>> getServerMap() {
		return serverMap;
	}

	public void setServerMap(Map<String, List<ServerStatus>> serverMap) {
		this.serverMap = serverMap;
	}

	public boolean isDeviceComplete() {
		return deviceComplete;
	}

	public void setDeviceComplete(boolean deviceComplete) {
		this.deviceComplete = deviceComplete;
	}

	public boolean isServerComplete() {
		return serverComplete;
	}

	public void setServerComplete(boolean serverComplete) {
		this.serverComplete = serverComplete;
	}

	public boolean isOrganComplete() {
		return organComplete;
	}

	public void setOrganComplete(boolean organComplete) {
		this.organComplete = organComplete;
	}

	public RequestEntity getEntity() {
		return entity;
	}

	public void setEntity(RequestEntity entity) {
		this.entity = entity;
	}

	public void updateOrganStatus(RequestEntity entity) {
		Ar.exesql(
				"update organ_status os join task_status ts on(os.id=ts.object_id and ts.object_type='organ' and (ts.change_time<? or (ts.change_time=? and ts.nanosecond<=?)) and ts.sn=? and ts.platform_id=?) set base_notify=true",
				entity.getChangeTime(), entity.getChangeTime(), entity
						.getNanosecond(), entity.getSN(), entity
						.getGbPlatform().getId());
		Ar.exesql("delete from task_status  where object_type='organ' and (change_time<? or (change_time=? and nanosecond<=?)) and sn=? and platform_id=?", entity.getChangeTime(), entity.getChangeTime(), entity
				.getNanosecond(), entity.getSN(), entity
				.getGbPlatform().getId());
	}

	public void updateServerStatus(RequestEntity entity) {
		Ar.exesql(
				"update server_status ss join task_status ts on(ss.id=ts.object_id and ts.object_type='server' and (ts.change_time<? or (ts.change_time=? and ts.nanosecond<=?)) and ts.sn=? and ts.platform_id=?) set base_notify=true,online_notify=true",
				entity.getChangeTime(), entity.getChangeTime(), entity
						.getNanosecond(), entity.getSN(), entity
						.getGbPlatform().getId());
		Ar.exesql("delete from task_status  where object_type='server' and (change_time<? or (change_time=? and nanosecond<=?)) and sn=? and platform_id=?", entity.getChangeTime(), entity.getChangeTime(), entity
				.getNanosecond(), entity.getSN(), entity
				.getGbPlatform().getId());
	}

	public void updateDeviceStatus(RequestEntity entity) {
		Ar.exesql(
				"update device_status ds join task_status ts on(ds.id=ts.object_id and ts.object_type='device' and (ts.change_time<? or (ts.change_time=? and ts.nanosecond<=?)) and ts.sn=? and ts.platform_id=?) set base_notify=true,online_notify=true",
				entity.getChangeTime(), entity.getChangeTime(), entity
						.getNanosecond(), entity.getSN(), entity
						.getGbPlatform().getId());
		Ar.exesql("delete from task_status  where object_type='device' and (change_time<? or (change_time=? and nanosecond<=?)) and sn=? and platform_id=?", entity.getChangeTime(), entity.getChangeTime(), entity
				.getNanosecond(), entity.getSN(), entity
				.getGbPlatform().getId());
	}

}
