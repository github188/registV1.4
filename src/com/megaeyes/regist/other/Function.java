package com.megaeyes.regist.other;

public enum Function {
	getUsers, getRoles;
	
	public static String getRoles() {
		return Function.getRoles.name();
	}

	public static String getUsers() {
		return Function.getUsers.name();
	}
}
