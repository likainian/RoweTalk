package com.example.rowetalk.util;

public class ThreadSleep {

	public static boolean Sleep(long ms){
		try {
			Thread.sleep(ms);
			return true;
		}catch(Exception e){
			//
			e.printStackTrace();
		}
		return false;
	}
}
