package com.example.rowetalk.util;

public class SleepObject {
	protected Object sleepObject = new Object();
	public boolean sleep(long ms){
		synchronized (sleepObject) {
			try {
				sleepObject.wait(ms);
				return true;
			}catch(Exception e){
				
			}
		}
		return false;
	}
	public void wake(){
		synchronized (sleepObject) {
			try {
				sleepObject.notifyAll();
			}catch(Exception e){
				
			}
		}
	}
}
