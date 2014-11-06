package com.megaeyes.regist.controllers;

import org.springframework.stereotype.Component;

import com.megaeyes.regist.annotation.NotRequire;

@Component("login")
public class LoginController {
	@NotRequire
	public String index(){
		return "login-require";
	}
}
