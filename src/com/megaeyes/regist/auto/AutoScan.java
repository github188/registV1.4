package com.megaeyes.regist.auto;

import org.springframework.beans.factory.annotation.Autowired;

import com.megaeyes.regist.dao.RegisterDao;

public class AutoScan {
	@Autowired
	private RegisterDao registerDao;

	public void monitor() {
		registerDao.accessSelf(null, "/autoScanCtrl/monitor");
	}

	public void createStaticFile() {
		registerDao.accessSelf(null, "/autoScanCtrl/createStaticFile");
	}


	public void parentRegistOrgan(String cmsId) {
		registerDao.accessSelf(null, "/autoScanCtrl/parentRegistOrgan");
	}

	public void setOrganParent() {
		registerDao.accessSelf(null, "/autoScanCtrl/setOrganParent");
	}


	public void initAccessIp() {
		registerDao.accessSelf(null, "/autoScanCtrl/initAccessIp");
	}


	public void flushStatus() {
		registerDao.accessSelf(null, "/autoScanCtrl/flushStatus");
	}

	public void scanDeviceStatus() {
		registerDao.accessSelf(null, "/autoScanCtrl/scanDeviceStatus");
	}

	public void pushResource() throws InterruptedException {
		registerDao.accessSelf(null, "/autoScanCtrl/pushResource");
	}

	public void rectify() {
		registerDao.accessSelf(null, "/autoScanCtrl/rectify");
	}

	/**
	 * 作为上级，系统启台后初始刷新订阅
	 */
	public void initSubscribe() {
		registerDao.accessSelf(null, "/autoScanCtrl/initSubscribe");
	}
	
	public void pullAllData() {
		registerDao.accessSelf(null, "/autoScanCtrl/pullAllData");
	}
}
