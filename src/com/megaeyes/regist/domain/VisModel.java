package com.megaeyes.regist.domain;

public class VisModel {
	private String id;
	private long optimisticLock;
	private String name;
	private String type;
	private String encode;
	private String note;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getOptimisticLock() {
		return optimisticLock;
	}

	public void setOptimisticLock(long optimisticLock) {
		this.optimisticLock = optimisticLock;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getEncode() {
		return encode;
	}

	public void setEncode(String encode) {
		this.encode = encode;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
