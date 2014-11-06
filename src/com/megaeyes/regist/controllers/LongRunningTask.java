package com.megaeyes.regist.controllers;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class LongRunningTask implements Callable<String> {
		 
	    public String call() {
	        // do stuff and return some String
	        try {
	            TimeUnit.SECONDS.sleep(2);
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
	        return Thread.currentThread().getName();
	    }
	}