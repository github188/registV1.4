package com.megaeyes.regist.utils;

import java.util.Calendar;
import java.util.Random;

class MyRandom {
	private int max;
	private int min;
	private int num;
	Random rMax = new Random();
	Random rMin = new Random();

	public MyRandom(int min, int max) {
		this.max = max;
		this.min = min;
	}

	public int getNum() {
		do {
			num = rMax.nextInt(max);
		} while (num < min);
		return num;
	}
}

public class IdGen {

	public static void main(String[] args) {	
		for (int i = 0; i < 600; i++)
			System.out.println(getId("a"));
		System.out.println(getId("a").length());
	}

	private static int idSeq = 0;

	public static String getId(String prefix) {
		return getId(prefix, Calendar.getInstance());
	}

	public static String getId(String prefix, Calendar ca) {
		int year = ca.get(Calendar.YEAR);
		char yearChar1 = (char) ('a' + (year - 2001) / 26);
		char yearChar2 = (char) ('a' + (year - 2001) % 26);

		int month = ca.get(Calendar.MONTH);
		char monthChar = (char) ('a' + month % 12);

		int day = ca.get(Calendar.DAY_OF_MONTH);
		char dayChar = (char) ('a' + (day - 1) % 31);
		if (dayChar > 'z') {
			dayChar = (char) ('A' + dayChar - 'z' - 1);
		}

		int hour = ca.get(Calendar.HOUR_OF_DAY);
		char hourChar = (char) ('a' + hour % 24);

		int min = ca.get(Calendar.MINUTE);
		char minChar1 = (char) ('a' + min % 26);
		char minChar2 = (char) ('a' + min / 26);

		int sec = ca.get(Calendar.SECOND);
		char secChar1 = (char) ('a' + sec % 26);
		char secChar2 = (char) ('a' + sec / 26);

		int minsec = ca.get(Calendar.MILLISECOND);
		char minsecChar1 = (char) ('a' + minsec % 26);
		if (idSeq >= 100000000) {
			idSeq = 0;
		}
		return prefix + yearChar1 + yearChar2 + monthChar + dayChar + hourChar
				+ minChar1 + minChar2 + secChar1 + secChar2+minsecChar1
				+ "000000000".substring(0, 9 - ("" + idSeq).length()) + idSeq++;
	}
}

class IdGen2 {
	public static String getId(char p) {
		String id;
		// 数字和小写字母分布的2个区域,'-'这个比较特殊,暂时留用
		MyRandom random1 = new MyRandom(48, 57);
		MyRandom random2 = new MyRandom(97, 122);
		while (true) {
			// 随机选择进入这二个区域
			Random ranTmp = new Random();
			int tmp = 0;
			byte[] b = new byte[10];
			b[0] = (byte) p;
			for (int i = 1; i < 10; i++) {
				tmp = ranTmp.nextInt(2);
				switch (tmp) {
				case 0:
					b[i] = (byte) random1.getNum();
					break;
				case 1:
					b[i] = (byte) random2.getNum();
					break;
				}
			}
			id = new String(b);
			return id;
		}
	}
}
