package com.megaeyes.regist.aop;

import java.lang.reflect.Method;

import javax.jws.WebService;

import net.hight.performance.annotation.Interceptor;
import net.hight.performance.filter.HPFilter;

import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.servlet.handler.DispatcherServletWebRequest;


public aspect ServiceAop {
	
	pointcut webServiceInteceptor() :execution(public * *..*(..)) && within(@WebService *) ;

	Object around() : webServiceInteceptor() {
		MethodSignature sig = (MethodSignature) thisJoinPointStaticPart
				.getSignature();
		Method m = sig.getMethod();
		Object object=null;
		boolean require = requireInterceptor(m, thisJoinPoint.getThis());
		if (require) {
			HPFilter.getInterceptor().preHandle(
					new DispatcherServletWebRequest(HPFilter.getInv()
							.getRequest()));
		}
		try {
			object = proceed();
			if (require) {
				HPFilter.getInterceptor().postHandle(
						new DispatcherServletWebRequest(HPFilter.getInv()
								.getRequest()), null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (require) {
				HPFilter.getInterceptor().afterCompletion(
						new DispatcherServletWebRequest(HPFilter.getInv()
								.getRequest()), null);
			}
		}
		return object;

	}

	private boolean requireInterceptor(Method m, Object o) {
		if (!HPFilter.isHasSessionFactory()) {
			return false;
		}

		Interceptor interceptor = m.getAnnotation(Interceptor.class);
		if (interceptor != null) {
			return interceptor.required();
		}

		interceptor = o.getClass().getAnnotation(Interceptor.class);
		if (interceptor != null) {
			return interceptor.required();
		}
		interceptor = null;
		return true;
	}
}
