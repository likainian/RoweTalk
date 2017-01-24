package com.example.rowetalk.util;

import java.util.Random;

import com.example.rowetalk.bean.Platform;

public class Generator {
	private static final String hexString="0123456789abcdef";
	private static final String hexStringUpper="0123456789ABCDEF";
	private static final String ascStringLower="0123456789abcdefghijklmnopqrstuvwxyz";
	private static final String ascStringUpper="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String numString="0123456789";

	private static final String alphaLower="abcdefghijklmnopqrstuvwxyz";
	private static final String alphaUpper="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	
	
	
	
	public static String RandomImei(Random r,Platform p){
		String imei=null;
		int sel = r.nextInt(100);
		if(sel<30){
			imei = "86";
		}else if(sel < 90){
			imei = "35";
		}else if(sel < 95){
			imei = "99";
		}else {
			imei = "01";
		}
		for(int i=2;i<15;i++){
			imei += String.valueOf(r.nextInt(10));
		}
		return imei;
	}
	public static String RandomMac(Random r,Platform p){
		String mac="";
		for(int i=0; i<6; i++) {
			mac+=hexString.charAt(r.nextInt(16));
			mac+=hexString.charAt(r.nextInt(16));
			if(i!=5) {
				mac+=":";
			}
		}
		return mac;
	}
	
	public static String RandomMacHalf(Random r){
		String mac="";
		for(int i=0; i<3; i++) {
			mac+=hexString.charAt(r.nextInt(16));
			mac+=hexString.charAt(r.nextInt(16));
			if(i!=2) {
				mac+=":";
			}
		}
		return mac;
	}
	
	public static String RandomAID(Random r, Platform p){
		return generateAscString(r, 16, true, false);
	}
	
	public static String RandomSerial(Random r,Platform p){
		int sel = r.nextInt(100);
		if(sel < 3)
			return "123456789ABCDEF";
		else if(sel < 10)
			return generateAscString(r, 8, true, false);
		else if(sel < 30)
			return generateAscString(r, 12, false, true);
		else if(sel < 50)
			return generateNumString(r, 12);
		else if(sel < 70)
			return generateAscString(r, 7, true, false);
		else if(sel < 80)
			return generateAscString(r, 16, true, false);
		else if(sel < 90)
			return generateAscString(r, 15, true, false);
		else
			return generateNumString(r, 15);
	}
	
	public static String generateAscString(Random r, int len, boolean hex, boolean upper){
		String s = "";
		for(int i=0; i<len; i++) {
			if(hex){
				s+=upper?hexStringUpper.charAt(r.nextInt(hexStringUpper.length())):hexString.charAt(r.nextInt(hexString.length()));
			}else{
				s+=upper?ascStringUpper.charAt(r.nextInt(ascStringUpper.length())):ascStringLower.charAt(r.nextInt(ascStringLower.length()));
			}
		}
		return s;
	}
	
	public static String generateNumString(Random r, int len){
		String s = "";
		for(int i=0; i<len; i++) {
			s+=numString.charAt(r.nextInt(numString.length()));
		}
		return s;
	}
	
	public static String RandomString(Random r, int len){
		String mac="";//String.valueOf(r.nextInt(9)+1);
		for(int i=0; i<len; i++) {
			mac+=hexString.charAt(r.nextInt(16));
		}
		return mac;
	}
	public static String RandomIpV4(Random r) {
		// TODO Auto-generated method stub
		String ip = "192.168";
		int i = r.nextInt(100);
		if(i<40){
			ip += ".0";
		}else if(i<60){
			ip += ".1";
		}else if(i<80){
			ip += ".2";
		}else if(i<90){
			ip += ".3";
		}else{
			ip += String.format(".%d", 4+r.nextInt(248));
		}
		ip += String.format(".%d", 1+r.nextInt(253));
		
		return ip;
	}
	public static String RandomIpV6(Random r) {
		// TODO Auto-generated method stub
		String ip = "fe80::";
		for(int i=0; i<4; i++) {
			ip+=hexString.charAt(r.nextInt(hexString.length()));
		}
		ip +=":";
		ip+=String.format("%02dff:fe", 10+r.nextInt(80));
		for(int i=0; i<2; i++) {
			ip+=hexString.charAt(r.nextInt(hexString.length()));
		}
		ip +=":";
		for(int i=0; i<4; i++) {
			ip+=hexString.charAt(r.nextInt(hexString.length()));
		}
		ip +="%wlan0"; //?
		
		return ip;
	}
	
	public static String RandomName(Random r,String head){
		if(head.startsWith("\"")){
			head = head.substring(1, head.length()-2);
		}
		String s = head;
		int count = r.nextInt(100);
		if(count < 50){
			s = s.substring(0, 1).toUpperCase() + s.substring(1);
		}
		count = r.nextInt(100);
		if(count < 50){
			s+="_";
		}
		count = r.nextInt(100);
		if(count<50){
			for(int i=0; i<4; i++) {
				s+=hexString.charAt(r.nextInt(hexString.length()));
			}
		}else if(count < 60){
			s += String.format("%04x", r.nextInt(65535));
		}else if(count < 63){
			s += String.format("%04X", r.nextInt(65535));
		}else if(count < 65){
			s += String.format("%x", r.nextInt(65535));
		}else if(count < 68){
			s += String.format("%X", r.nextInt(65535));
		}else if(count < 70){
			s += String.format("%04d", r.nextInt(65535));
		}else if(count < 75){
			s += String.format("%d", r.nextInt(65535));
		}else if(count < 80){
			s += String.format("%02d%02d", 1+r.nextInt(12), 1+r.nextInt(29));
		}else {
			s += NameUtil.getRandomEnglishName(r, 0);
		}
		return s;
	}
	/*
	 http://blog.csdn.net/phunxm/article/details/42174937/
	 iPhone 4  640x960   PPI 326
	 iPhone 4S 640x960   PPI 326
	 iPhone 5  640x1136   PPI 326
	 iPhone 5S 640x1136   PPI 326
	 iPhone 5C 640x1136   PPI 326
	 iPhone 6  750x1334   PPI 326
	 iPhone 6  Plus 1080x1920 （开发应按照1242x2208适配）   PPI 401
	 iPhone 6S 750x1334   PPI 326
	 iPhone 6S Plus 1080x1920 （开发应按照1242x2208适配）   PPI 401
	*/
	public static String RandomIosOpenUDID(Random r){
		String demo = "da0fd9abf16b6c8dade7e5228a6761b8adfc958b";
		String s = "";
		for(int i=0; i<demo.length(); i++) {
			s+=hexString.charAt(r.nextInt(hexString.length()));
		}
		return s;
	}
	
	public static String RandomIosIDFA(Random r) {
		//String demo = "1E2DFA89-496A-47FD-9941-DF1FC4E6484A";
		String s = "";
		for(int i=0; i<8; i++) {
			s+=hexStringUpper.charAt(r.nextInt(hexStringUpper.length()));
		}
		s+="-";
		for(int i=0; i<4; i++) {
			s+=hexStringUpper.charAt(r.nextInt(hexStringUpper.length()));
		}
		s+="-";
		for(int i=0; i<4; i++) {
			s+=hexStringUpper.charAt(r.nextInt(hexStringUpper.length()));
		}
		s+="-";
		for(int i=0; i<4; i++) {
			s+=hexStringUpper.charAt(r.nextInt(hexStringUpper.length()));
		}
		s+="-";
		for(int i=0; i<12; i++) {
			s+=hexStringUpper.charAt(r.nextInt(hexStringUpper.length()));
		}
		return s;
	}
	
	public static String RandomBdID(Random r) {
		String s = "";
		for(int i=0; i<3; i++) {
			s+=alphaUpper.charAt(r.nextInt(alphaUpper.length()));
		}
		for(int i=0; i<2; i++) {
			s+=numString.charAt(r.nextInt(numString.length()));
		}
		s+=alphaUpper.charAt(r.nextInt(alphaUpper.length()));
		return s;
	}
	public static String RandomBdUser(Random r) {
		// TODO Auto-generated method stub
		final String[] users={"user", "compiler", "sw", "xiaobo", "server",
				"ubuntu", "rd", "wt", "cc", "delta", "alpha", "beta", "jungle", "jugo", "walt"};
		String s = "";
		if(r.nextInt(100)<50) {
			s+=users[r.nextInt(users.length)];
		}else{
			for(int i=0; i<2; i++) {
				s+=alphaLower.charAt(r.nextInt(alphaLower.length()));
			}
		}
		if(r.nextInt(100)<50) {
			s+=numString.charAt(r.nextInt(numString.length()));
		}
		return s;
	}
	
}
