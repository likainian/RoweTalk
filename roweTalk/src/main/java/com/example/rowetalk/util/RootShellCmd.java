package com.example.rowetalk.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.util.Log;

public class RootShellCmd {
	private static final String TAG = "RootShellCmd";
	private BufferedWriter bw;
	private BufferedReader br;
	private Process suProcess;
	
	public static final int EV_ABS = 0x003;
	public static final int EV_KEY = 0x001;
	public static final int EV_SYN  = 0x000;
	
	public static final int ABS_MT_TOUCH_MAJOR = 0x0030;
	public static final int ABS_MT_TRACKING_ID = 0x0039;

	public static final int BTN_TOUCH = 0x014a;
	public static final int DOWN = 0x0001;
	public static final int UP = 0x0000;
	
	public static final int ABS_MT_POSITION_X  = 0x0035;
	public static final int ABS_MT_POSITION_Y  = 0x0036;
	
	public static final int SYN_MT_REPORT   = 0x0002;
	public static final int SYN_REPORT  = 0x0000;
	
	public static final String OK = "^OK$";
	public static final String FAIL = "^FAIL$";
	
	private static RootShellCmd instance;
	
	private SleepObject sleepObject;
	
	public static RootShellCmd getInstance() {
		if(instance == null){
			instance = new RootShellCmd();
		}
		return instance;
	}
	
	public void setSleepObject(SleepObject obj){
		this.sleepObject = obj;
	}
	
	private boolean sleep(long ms){
		if(sleepObject != null) return sleepObject.sleep(ms);
		else return ThreadSleep.Sleep(ms);
	}
	public boolean exec(String cmd) {
		return exec(cmd, null);
	}
	
	public boolean exec(String cmd, StringBuilder sbBuilder) {
		return exec(cmd, sbBuilder, true, true);
	}

	public boolean exec(String cmd, StringBuilder sbBuilder, boolean logContent, boolean logCmd) {
		if(logCmd) Logger.e(TAG, "exec: "+cmd); 
		if(StringUtil.isEmpty(cmd)) return false;
		boolean jump_out = false;
		for(int lp=0; lp<3 && !jump_out; lp++) {
			try {
				if (bw == null) 
				{ 
					//suProcess = Runtime.getRuntime().exec("sh"); //系统签名，push到app目录下用这个
					suProcess = Runtime.getRuntime().exec("s4ty"); 
					bw = new BufferedWriter(new OutputStreamWriter(suProcess.getOutputStream())); 
					br = new BufferedReader(new InputStreamReader(suProcess.getInputStream())); 
				}
				bw.write(String.format("(%s) && echo %s || echo %s\n", cmd, OK, FAIL)); 
				bw.flush(); 
				if(!sleep(1000)){
					jump_out = true;
					break;
				}
			
				boolean b = false;
				int loopCount = 0;
				while(loopCount<30){
					if(br.ready()) {
						String res = br.readLine();
						if(res == null) break;
						if(sbBuilder != null) {
							if(res.endsWith(OK)){
								sbBuilder.append(res.substring(0, res.length()-OK.length())).append("\n");
							}else if(res.endsWith(FAIL)) {
								sbBuilder.append(res.substring(0, res.length()-FAIL.length())).append("\n");
							}else{
								sbBuilder.append(res).append("\n");
							}
						}
						
						if(res.equals(OK)||res.endsWith(OK)) {
							if(res.length()>OK.length()){
								if(logContent) Logger.e(TAG, "=> "+res.substring(0, res.length()-OK.length())+"\n");
							}
							if(logCmd) Logger.e(TAG, "=> OK\n");
							b = true;
							break;
						}else if(res.equals(FAIL)||res.endsWith(FAIL)){
							if(res.length()>FAIL.length()){
								if(logContent) Logger.e(TAG, "=> "+res.substring(0, res.length()-FAIL.length())+"\n");
							}
							if(logCmd) Logger.e(TAG, "=> FAIL\n");
							b = false;
							break;
						}else{
							if(logContent) Logger.e(TAG, "=> "+res+"\n");
						}
					}else{
						if(loopCount > 10) {
							if(logCmd) Logger.e(TAG, "no response, wait 1s "+loopCount+(loopCount==11?" "+cmd:""));
						}
						if(!sleep(1000)){
							jump_out = true;
							break;
						}
						loopCount++;
					}
				}
				
				if(!b){
					try {
						if(bw != null){
							bw.close();
							bw = null;
						}
						if(br != null){
							br.close();
							br = null;
						}
						if(suProcess!= null){
							suProcess.destroy();
							suProcess = null;
						}
					}catch(Exception e1){}
					Logger.e(TAG, "shell no response, restart: "+lp);
					continue;
				}
				
				return b;
			} catch (Exception e) {
				Logger.e(TAG, "Exception: "+e); 
				Logger.e(TAG, "retry: "+lp);
				try {
					if(bw != null){
						bw.close();
						bw = null;
					}
					if(br != null){
						br.close();
						br = null;
					}
					if(suProcess!= null){
						suProcess.destroy();
						suProcess = null;
					}
				}catch(Exception e1){}
				if(!sleep(2000)){
					jump_out = true;
					break;
				}
				//return false;
			} 
		}
		return false;
	}
	
	public final String exec2(String cmd) {
		Logger.e(TAG, "exec: "+cmd); 
		try {
			Process su = Runtime.getRuntime().exec("su"); 
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(su.getOutputStream())); 
			BufferedReader r = new BufferedReader(new InputStreamReader(su.getInputStream())); 
			w.write(cmd + " && echo "+OK+"\n"); 
			w.flush(); 
			String res = r.readLine();
			if(res != null) {
				if(res.equals(OK)) {
					Logger.e(TAG, "exec => success");
				}else{
					Logger.e(TAG, "exec => "+res);
				}
			}
			w.close();
			r.close();
			su.destroy();
			return res;
		} catch (Exception e) {
			Logger.e(TAG, "Exception: "+e); 
			return null;
		} 
	}

	public boolean inputKey(int keyCode) {
		return exec("input keyevent " + keyCode); 
	}
	
	public boolean startApp(String package_name, String main_activity){
		return exec("am start -n "+package_name+"/"+main_activity ); 
	}
	public boolean killApp(String package_name){
		return exec("am force-stop "+package_name ); 
	}
	
	public boolean clearApp(String package_name){
		return exec("pm clear "+ package_name); 
	}
	
	public boolean installApp(String apkPath, String package_name){
		return exec("pm install -r -f -i "+ package_name +" " + apkPath); 
	}
	
	public boolean installApp(String apkPath){
		return exec("pm install -r -f " + apkPath); 
	}

	public boolean uninstallApp(String package_name) {
		return exec("pm uninstall " + package_name); 
	}
	
	public boolean removeDir(String dir) {
		// TODO Auto-generated method stub
		return exec("rm -rf " + dir); 
	}
	
	public boolean clearDir(String dir) {
		// TODO Auto-generated method stub
		return exec("rm -rf " + dir +"/*"); 
	}
	
	public boolean makeDir(String dir) {
		// TODO Auto-generated method stub
		return exec("mkdir -p " + dir); 
	}
	
	public boolean copyDir(String srcDir, String dir) {
		// TODO Auto-generated method stub
		return exec("cp -rf " + srcDir+ "/* "+dir);
	}
	
	public boolean copyFile(String absolutePath, String filePath) {
		// TODO Auto-generated method stub
		return exec("cp -p -f "+absolutePath +" "+filePath); 
	}

	public boolean rmFile(String absolutePath) {
		// TODO Auto-generated method stub
		return exec("rm -f "+absolutePath); 
	}
	
	public boolean setProp(String prop, String name){
		if(name!=null){
			name = name.trim();
			int i = name.indexOf(" ");
			if(i>0){
				name = "\""+name+"\"";
			}
			if(name.length()>0){
				return exec("setprop "+prop+" "+name);
			}
		}
		return false;
	}
	
	public boolean sleep_seconds(int secs){
		//return exec("sleep "+secs); 
		return sleep(secs*1000);
	}
	public boolean inputTap(int x, int y){
		boolean b= exec("input tap "+x+" "+y); 
		sleep_seconds(1);
		return b;
	}

	public boolean dial(String string) {
		// TODO Auto-generated method stub
	   exec("service call phone 1 s16 "+string); 
	   sleep_seconds(2);
	   return inputTap(355, 1143);
	}
	
	public boolean hangup() {
		// TODO Auto-generated method stub
	   return exec("service call phone 3"); 
	}
	
	public boolean setImeLatin() {
		return exec("ime set com.android.inputmethod.latin/.LatinIME"); 
	}
	
	public boolean setImeTy() {
		return exec("ime set com.ty.ime/.LatinIME"); 
	}
	
	public boolean inputTyText(String text, boolean go, boolean auto_wait){
		if(text.length()>0){
			if(auto_wait){
				if(!sleep(text.length()*600)) return false; // each char 0.6s
			}
			exec("am broadcast -a ADB_INPUT_TEXT --es msg '"+text+"'");
			if(go){
				if(!sleep(300)) return false;
				boolean b= exec("am broadcast -a ADB_EDITOR_CODE --ei code 2");
				if(!sleep(500)) return false;
				return b;
			}else {
				if(!sleep(1000)) return false;
			}
			return true;
		}
		return false;
	}

	public boolean checkApp(String package_name) {
		// TODO Auto-generated method stub
		return exec("pm list packages | grep " + package_name);
	}

	/*
	public boolean checkRoot() {
		// TODO Auto-generated method stub
		return exec("ls /data", null, false, true); 
	}*/
	
	public boolean checkIpRouteDeviceExist(String devName){
		StringBuilder sBuilder = new StringBuilder();
		boolean b = exec("ip ro", sBuilder, false, true);
		Log.e(TAG, "checkIpRouteDeviceExist: "+b+" "+sBuilder.toString());
		return b && sBuilder.toString().contains(devName);
	}
	
	
	
	/*
	 adb shell am broadcast 后面的参数有：

	[-a <ACTION>]
	[-d <DATA_URI>]
	[-t <MIME_TYPE>] 
	[-c <CATEGORY> [-c <CATEGORY>] ...] 
	[-e|--es <EXTRA_KEY> <EXTRA_STRING_VALUE> ...] 
	[--ez <EXTRA_KEY> <EXTRA_BOOLEAN_VALUE> ...] 
	[-e|--ei <EXTRA_KEY> <EXTRA_INT_VALUE> ...] 
	[-n <COMPONENT>]
	[-f <FLAGS>] [<URI>]
	
	
	例如：
	
	adb shell am broadcast -a com.android.test --es test_string "this is test string" --ei test_int 100 --ez test_boolean true
	
	
	说明：蓝色为key，红色为alue，分别为String类型，int类型，boolean类型
	am start -a android.intent.action.VIEW -d  http://www.baidu.com 
	am start -a android.intent.action.CALL -d tel:10086
	am startservice -n com.android.music/com.android.music.MediaPlaybackService
	 */
	
	public boolean openUrl(String url){
		return exec("am start -a android.intent.action.VIEW -d "+url);
	}
	public String getUsbSerial(){
		String result = null;
		try {
			Process su = Runtime.getRuntime().exec("su"); 
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(su.getOutputStream())); 
			BufferedReader r = new BufferedReader(new InputStreamReader(su.getInputStream())); 
			w.write("cat /sys/class/android_usb/android0/iSerial && echo "+OK+"\n"); 
			w.flush(); 
			String res = r.readLine();
			if(res != null) {
				if(res.endsWith(OK)){
					result = res.substring(0, res.length()-OK.length());
				}
			}
			w.close();
			r.close();
			su.destroy();
		} catch (Exception e) {
			Log.e(TAG, "Exception: "+e); 
		} 
		if(result == null) return "";
		else return result;
	}
	
	public boolean setFilePermission(String file_path, String permission) {
		// TODO Auto-generated method stub
		return exec(String.format("chmod %s %s", permission, file_path), null, false, false);
	}
	
	// check view server
	public boolean isViewServerRunning(){
		StringBuilder sb = new StringBuilder();
		boolean b = exec("service call window 3", sb);
		if(b){
			b = sb.toString().indexOf("00000000 00000001")>0;
		}
		return b;
	}
	
	public boolean startViewServer(){
		StringBuilder sb = new StringBuilder();
		boolean b = exec("service call window 1 i32 4939", sb);
		if(b){
			b = sb.toString().indexOf("00000000 00000001")>0;
		}
		return b;
	}
	
	public boolean stopViewServer(){
		StringBuilder sb = new StringBuilder();
		boolean b = exec("service call window 2", sb);
		if(b){
			b = sb.toString().indexOf("00000000 00000001")>0;
		}
		return b;
	}

	public String getSystemProp(String prop) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		boolean b = exec("getprop "+prop, sb);
		if(b){
			return sb.toString().trim();
		}
		
		return null;
	}
}
