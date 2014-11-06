package com.megaeyes.regist.other;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.codehaus.xfire.annotations.AnnotationServiceFactory;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;

import com.megaeyes.regist.webservice.RegistCore;
import com.megaeyes.regist.webservice.RegistCoreImpl;

public class RegistCallable implements Callable<String> {
	private Object[] objects;
	private Class<?>[] clazz;
	private String methodStr;
	private String url;
	
	public RegistCallable(String url){
		this.url = url;
	}
	
	@Override
	public String call() {
		Method method;
		RegistCore service;
		try {
			Service serviceModel = new AnnotationServiceFactory()
					.create(RegistCoreImpl.class);
			service = (RegistCore) new XFireProxyFactory().create(serviceModel,
					url + "/RegistCoreService");
			method = RegistCore.class.getMethod(methodStr, clazz);
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
		try {
			return (String) method.invoke(service, objects);
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
		
	}

	public String getMethodStr() {
		return methodStr;
	}

	public void setMethodStr(String methodStr) {
		this.methodStr = methodStr;
	}

	public Object[] getObjects() {
		return objects;
	}

	public void setObjects(Object[] objects) {
		this.objects = objects;
	}

	public Class<?>[] getClazz() {
		return clazz;
	}

	public void setClazz(Class<?>[] clazz) {
		this.clazz = clazz;
	}
}
