package com.megaeyes.regist.webservice;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClearExpire{
	private final static AtomicBoolean noClearThread = new AtomicBoolean(true);
	private static final Executor exec = Executors.newSingleThreadExecutor();
	private volatile static ClearExpire uniqueInstance;
	private RegistCoreImpl regist;
	public static ClearExpire getInstance(RegistCoreImpl regist) {
		if (uniqueInstance == null) {
			synchronized (ClearExpire.class) {
				if (uniqueInstance == null) {
					uniqueInstance = new ClearExpire();
					uniqueInstance.regist = regist;
				}
			}
		}
		return uniqueInstance;
	}
	public  void startClearThread(){
		if (noClearThread.getAndSet(false)) {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					while(true){
						uniqueInstance.regist.clearExpireRecord();
						try {
							TimeUnit.SECONDS.sleep(60);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
			exec.execute(task);
		}
	}
	
}
