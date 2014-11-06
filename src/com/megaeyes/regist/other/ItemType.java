package com.megaeyes.regist.other;

public enum ItemType {
	Platform,
	Organ,
	Terminal,
	Camera,
	AlarmIn,
	AlarmOut;
	public static ItemType getType(String type) {
		if(Platform.name().equals(type)){
			return Platform;
		}
		if(Organ.name().equals(type)){
			return Organ;
		}
		if(Terminal.name().equals(type)){
			return Terminal;
		}
		if(Camera.name().equals(type)){
			return Camera;
		}
		if(AlarmIn.name().equals(type)){
			return AlarmIn;
		}
		if(AlarmOut.name().equals(type)){
			return AlarmOut;
		}
		return null;
	}
	
}
