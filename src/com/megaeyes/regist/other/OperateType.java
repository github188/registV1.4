package com.megaeyes.regist.other;

public enum OperateType {
	Add, Update, Delete;

	public static OperateType getType(String type) {
		if (Add.name().equals(type)) {
			return Add;
		}
		if (Update.name().equals(type)) {
			return Update;
		}
		if (Delete.name().equals(type)) {
			return Delete;
		}
		return null;
	}
}
