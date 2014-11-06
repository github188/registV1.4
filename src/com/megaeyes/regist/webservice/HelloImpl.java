package com.megaeyes.regist.webservice;
import javax.jws.WebService;

@WebService(serviceName = "HelloService", endpointInterface = "com.megaeyes.platform.regist.webservice.Hello")
public class HelloImpl implements Hello {
	public String world() {
		return "hello World!";
	}

	public String world2() {
		return "my name is slim shady";
	}
}
