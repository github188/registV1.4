package com.megaeyes.regist.controllers;

import javax.servlet.RequestDispatcher;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.megaeyes.utils.Invocation;


@Aspect
@Component("loginRequireAop")
public class LoginRequireAopController {
	@Before("@annotation (com.megaeyes.regist.annotation.LoginRequire)&& args(inv,..)")
	public void require(Invocation inv) {
		String cmsId = (String) inv.getRequest().getSession()
				.getAttribute("platformCmsId");
		if (cmsId != null) {
			RequestDispatcher dis = inv.getRequest().getRequestDispatcher("/regist/login");
			try {
				dis.forward(inv.getRequest(), inv.getResponse());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
