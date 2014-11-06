package com.megaeyes.regist.enump;

import java.util.HashSet;
import java.util.Set;

public class ServerType {
	private static Set<String> types = new HashSet<String>();
	static{
		for(int i=111;i<131;i++){
			types.add(""+i);
		}
	}
	public static Set<String> getTypes(){
		return types;
	}
}
