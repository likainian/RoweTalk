package com.example.rowetalk.util;


import android.os.Environment;

public class Constant {
	
	public static final String TY_TAKE_SCREEN_SHOT = "com.ty.intent.take_screen_shot"; //hongbiao.zhang@2016.5.18: add
	
	private static final String SERVER_RELEASE = "http://120.26.129.37:8060";	//"http://192.168.199.100:8060";	
	private static final String SERVER_TEST = "http://192.168.2.100:8060";	//"http://192.168.199.100:8060";	
	private static String SERVER_HOST = SERVER_TEST;
	
	public static final String FTP_SERVER = "192.168.2.100";
	public static final String FTP_USER = "tyftp";
	public static final String FTP_PASSWORD = "ty#edc4f";
	
	public static int SERVER_MINIMUM_API = 1;
	
	public static String getSERVER_HOST() {
		return SERVER_HOST;
	}
	
	public static String getSERVER_TEST() {
		return SERVER_TEST;
	}
	public static String getSERVER_RELEASE() {
		return SERVER_RELEASE;
	}

	public static void setSERVER_HOST(String sERVER_HOST) {
		SERVER_HOST = sERVER_HOST;
	}

	public static String getURL_TEST_CONNECT() {
		return getSERVER_HOST()+"/misc/test";
	}
	public static String getURL_REGISTER_PHONE() {
		return getSERVER_HOST()+"/phone/register";
	}
	
	public static String getURL_REGISTER_DEVICE() {
		return getSERVER_HOST()+"/device/register";
	}
	
	public static String getURL_TASK_ALLOC() {
		return getSERVER_HOST()+"/task/alloc";
	}
	
	public static String getURL_TASK_REPORT() {
		return getSERVER_HOST()+"/task/report";
	}
	
	public static String getURL_SEND_IP() {
		return getSERVER_HOST()+"/task/send_ip";
	}
	
	public static String getURL_DOWNLOAD_TASK_SCRIPT() {
		return getSERVER_HOST()+"/common/download/script";
	}
	public static String getURL_DOWNLOAD_TASK_EXTRA() {
		return getSERVER_HOST()+"/common/download/extra_zip";
	}
	public static String getURL_DOWNLOAD_TASK_APK() {
		return getSERVER_HOST()+"/common/download/apk";
	}
	
	public static String getURL_DOWNLOAD_SHOT_DATA() {
		return getSERVER_HOST()+"/common/download/shot_data";
	}
	
	public static String getURL_QUERY_NUMBER() {
		return getSERVER_HOST()+"/misc/get_number";
	}
	
	public static String getTaskStoragePath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath()+"/rowetalk/task";
	}
	
	public static String getTempStoragePath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath()+"/rowetalk/tmp";
	}
	public static String getScriptPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath()+"/myScripts";
	}
	public static String getTaskScriptPath() {
		return getScriptPath()+"/task";
	}
	
	public static String getURL_UPDATE_CHECK_VERSION() {
		return getSERVER_HOST()+"/update/check_version";
	}
	
	public static String getURL_DOWNLOAD_APP_VERSION() {
		return getSERVER_HOST()+"/common/download/update_app";
	}
	public static String getURL_SEND_CRASH_LOG() {
		return getSERVER_HOST()+"/misc/send_crashlog";
	}
	public static String getURL_GET_SIM_SLOT() {
		return getSERVER_HOST()+"/misc/get_simslot";
	}
	public static String getURL_UPDATE_SIM_SLOT() {
		return getSERVER_HOST()+"/misc/update_simslot";
	}
	public static String getURL_UPDATE_SIM_RECENT_STATUS() {
		return getSERVER_HOST()+"/misc/update_sim_recent_status";
	}
	public static String getURL_ALLOC_VPN() {
		return getSERVER_HOST()+"/vpn/alloc_vpn";
	}
	public static String getURL_FREE_VPN() {
		return getSERVER_HOST()+"/vpn/free_vpn";
	}
	public static String getURL_TEST_VPN_CONNECT() {
		return getSERVER_HOST()+"/vpn/test_connect";
	}
	public static String getURL_REPORT_VPN_FAILED() {
		return getSERVER_HOST()+"/vpn/notify_failed";
	}
	public static String getURL_APP_GET_DATA() {
		return getSERVER_HOST()+"/account/getwebdata";
	}
	public static String getURL_APP_SUBMIT_DATA() {
		return getSERVER_HOST()+"/account/sumitwebdata";
	}
	public static String getURL_GET_TASk() {
		return getSERVER_HOST()+"/task/get_task";
	}
	public static String getURL_GET_MY_IP() {
		return getSERVER_RELEASE()+"/misc/get_my_ip";
	}
	public static String getURL_RANDOM_WORD() {
		return getSERVER_HOST()+"/misc/rand_word";
	}
	public static String getURL_RANDOM_PLATFORM() {
		return getSERVER_HOST()+"/misc/rand_platform";
	}
	public static String getURL_SYSTEM_CHECK_VERSION() {
		return getSERVER_HOST()+"/system_update/check_version";
	}
	public static String getURL_SYSTEM_DOWNLOAD_VERSION() {
		return getSERVER_HOST()+"/common/download/system_update";
	}
}
