package com.megaeyes.regist.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.megaeyes.regist.auto.AutoScan;

@Component("manager")
public class ManagerController {
	@Autowired
	private AutoScan autoScan;
	
	public String monitor(){
		autoScan.monitor();
		return "@success";
	}
	
	public String flushStatus(){
		autoScan.flushStatus();
		return "@success";
	}
	
	public String initAccessIp(){
		autoScan.initAccessIp();
		return "@success";
	}
	
	public String initResourceForPush(){
		autoScan.rectify();
		return "@success";
	}
	
	public String initSubscribe(){
		autoScan.initSubscribe();
		return "@success";
	}
	
	public String pushResource(){
		try {
			autoScan.pushResource();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "@success";
	}
	
	public String scanDeviceStatus(){
		autoScan.scanDeviceStatus();
		return "@success";
	}
	
}
