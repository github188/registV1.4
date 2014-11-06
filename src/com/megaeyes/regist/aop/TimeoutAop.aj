package com.megaeyes.regist.aop;

import java.util.concurrent.TimeoutException;

import javax.naming.TimeLimitExceededException;


public aspect TimeoutAop {
	pointcut webServiceInteceptor() :execution(public * com.megaeyes.regist.auto.AutoScan.main(..));
	
	Object around() : webServiceInteceptor() {
		try{
			Object object = proceed();
		}catch(TimeLimitExceededException e){
		}
		return null;
	}
}
