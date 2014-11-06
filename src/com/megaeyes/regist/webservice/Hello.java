package com.megaeyes.regist.webservice;
import javax.jws.WebResult;
import javax.jws.WebService;

@WebService
public interface Hello {
	public String world();

	@WebResult(name = "slimName")
	public String world2();

}