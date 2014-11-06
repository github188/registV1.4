package com.megaeyes.regist.enump;

import java.util.HashSet;
import java.util.Set;

public class TerminalType {
	private static Set<String> types = new HashSet<String>();
	static{
		for(int i=131;i<199;i++){
			types.add(""+i);
		}
	}
	public static Set<String> getTypes(){
		return types;
	}
}
