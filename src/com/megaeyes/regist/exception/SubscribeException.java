package com.megaeyes.regist.exception;

@SuppressWarnings("serial")
public class SubscribeException extends RuntimeException{
	public SubscribeException(Throwable root) {
		super(root);
	}

	public SubscribeException(String string, Throwable root) {
		super(string, root);
	}

	public SubscribeException(String s) {
		super(s);
	}
}
