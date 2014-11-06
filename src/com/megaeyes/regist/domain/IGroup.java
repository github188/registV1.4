package com.megaeyes.regist.domain;


public  interface IGroup {
	public Integer getId();

	public void setId(Integer id);

	public String getName() ;

	public void setName(String name);
	
	public String getStdId();
	
	public void setStdId(String stdId);
	
	public IGroup getParent();
	
	public Class<? extends IGroup> getMyClass();
}
