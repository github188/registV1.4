package com.megaeyes.regist.aop;

import javax.servlet.RequestDispatcher;

import com.megaeyes.utils.Invocation;


public aspect LoginRequire {
	before(Invocation inv):(execution(public * com.megaeyes.regist.webservice.PlatformUtils.getCmsId(Invocation,..)) && args(inv,..)){
		try {
			String cmsId = (String) inv.getModelFromSession("platformCmsId");
			if(cmsId==null){
				RequestDispatcher dis = inv.getRequest().getRequestDispatcher("/login/index");
				try {
					dis.forward(inv.getRequest(), inv.getResponse());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
			
}
