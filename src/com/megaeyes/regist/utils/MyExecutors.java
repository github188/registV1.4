package com.megaeyes.regist.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.megaeyes.regist.bean.SendHelper;

public class MyExecutors extends ThreadPoolExecutor {
	public MyExecutors(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}
	
	public MyExecutors(int nThreads) {
		super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
	}
	
	private ThreadLocal<SendHelper> sendHelper = new ThreadLocal<SendHelper>();
	private ThreadLocal<String> threadInfo = new ThreadLocal<String>();
	private ThreadLocal<Long> beginTime = new ThreadLocal<Long>();
	
	protected void beforeExecute(Thread t, Runnable r) {
		threadInfo.set(""+t.getId());
		beginTime.set(System.currentTimeMillis());
		super.beforeExecute(t, r);
	}

	protected void afterExecute(Runnable r, Throwable t) {
		System.out.println("thread "+threadInfo.get()+" >>>>>>>>>take time:"+(System.currentTimeMillis()-beginTime.get())/1000f +"(sec)");
		super.afterExecute(r, t);
	}

	protected void terminated() {
		super.terminated();
	}

	public ThreadLocal<SendHelper> getSendHelper() {
		return sendHelper;
	}

	public void setSendHelper(ThreadLocal<SendHelper> sendHelper) {
		this.sendHelper = sendHelper;
	}
}
