package com.example.rowetalk.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.rowetalk.activity.MainActivity;
import com.example.rowetalk.bean.VpnAccount;
import com.example.rowetalk.bean.VpnProfile;
import com.example.rowetalk.thread.BaseThread;
import com.example.rowetalk.thread.UpdateThread;
import com.example.rowetalk.thread.UpdateThread.UpdateInfo;
import com.example.rowetalk.thread.WorkThread;
import com.example.rowetalk.util.Config;
import com.example.rowetalk.util.Constant;
import com.example.rowetalk.util.DateUtil;
import com.example.rowetalk.util.FileUtil;
import com.example.rowetalk.util.Logger;
import com.example.rowetalk.util.NetworkUtil;
import com.example.rowetalk.util.RootShellCmd;
import com.example.rowetalk.util.SleepObject;
import com.example.rowetalk.util.StringUtil;
import com.example.rowetalk.util.SystemUtil;
import com.liblua.LuaEnv;
import com.luajava.LuaState;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.ServiceState;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class MainService extends Service {
	private static final String TAG = MainService.class.getName();
	
	public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";  
	public static final String ACTION_SERVICE_STATE_CHANGED = "android.intent.action.SERVICE_STATE";
	public static final String ACTION_SWITCH_APN = "ty.intent.action.switch_apn";

	public static final String ACTION_UI_MSG = "com.rowetalk.intent.uimsg";

	public static final String ACTION_START_AUTOBOT = "ty.intent.action.start_autobot";		

	public static final String ACTION_ROWETALK_UPDATE_CHECK = "ty.intent.rowetalk.update_check";
	public static final String ACTION_ROWETALK_START_SERVICE = "ty.intent.rowetalk.start_service";
	public static final String ACTION_ROWETALK_STOP_SERVICE = "ty.intent.rowetalk.stop_service";
	public static final String ACTION_ROWETALK_TEST_SCRIPT = "ty.intent.rowetalk.test_script";
	public static final String ACTION_ROWETALK_DEBUG_NEXT_STEP = "ty.intent.rowetalk.next_step";
	public static final String ACTION_ROWETALK_VIEW_CMD = "ty.intent.rowetalk.view_cmd";
	

	public static final String ACTION_IME_INPUT_CONFIRM = "ADB_IME_CONFIRM";
	
	public static int mSimState = TelephonyManager.SIM_STATE_UNKNOWN;
	public static int mServiceState= ServiceState.STATE_POWER_OFF;
	public static int mCallState = TelephonyManager.CALL_STATE_IDLE;
	

	public static final int MSG_SCRIPT_FINISHED = 999;
	public static final int MSG_UPDATE_APP = 1000;	
	public static final int MSG_START_TASK = 1001;
	public static final int MSG_UPDATE_THREAD_STATUS = 1002;

	private boolean init = false;
	
	private WakeLock wakeLock;
	
	private WifiManager mWifiManager;
	private TelephonyManager mTelephonyManager;
	private BluetoothAdapter mBluetoothAdapter;

	private BaseThread mThread = null;
	private Thread mTestScriptThread = null;
	
	private String mBtName, mBtAddr;
	private String mWifiName, mWifiAddr;
	private String mMifiName, mMifiAddr;
	
	private int mSimStart;
	private int mPhoneSeq = 0;
	private int mPoolSeq = 0;
	private String mSlotSeqs;
	private int mNoSimVdevieNum;
	private int mNoSimStart;
	
	private String mVpnName;
	private String mVpnCity;
	private String mVpnServer;
	private String mVpnUsername;
	private String mVpnPassword;
	private String mVpnDns;
	private boolean mVpnPpe;

	private boolean cbEnableMifi;
	private boolean cbEnable4G;
	private boolean cbEnableTest;
	private boolean cbAutoStart;
	private boolean cbEnableVPN;
	private boolean cbSpecifyVPN;
	private boolean cbTestVPN;
	private boolean cbNoSim;
	private boolean cbLogFile;
	//private boolean cbOverseas;
	private boolean cbReportIP;
	
	private String mServerHost;
	private UpdateThread mUpdateThread;
	public static List<Message> mReservedUIMsgs = new ArrayList<Message>();

	private SleepObject sleepObject = new SleepObject();
	
	public MainService() {
	}
	
	private void init(){
		if(init) return;
		Logger.e(TAG, "init");
		setServerRunning(false);
		init = true;
		
		RootShellCmd.getInstance().setSleepObject(sleepObject);

		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE); 
        mTelephonyManager = (TelephonyManager)getSystemService(Service.TELEPHONY_SERVICE); 
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);  
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK   | PowerManager.ON_AFTER_RELEASE, "DPA");  


        IntentFilter intentFilter = new IntentFilter();  
        intentFilter.addAction(ACTION_SMS_RECEIVED);
        intentFilter.addAction(LuaEnv.SMS_SENT);  
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);  
        //intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);  
        intentFilter.addAction(ACTION_SIM_STATE_CHANGED);
        //intentFilter.addAction(ACTION_SERVICE_STATE_CHANGED);
        //intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        //intentFilter.addAction("ty.intent.test_input");
        intentFilter.addAction(ACTION_IME_INPUT_CONFIRM);
        // add new
        intentFilter.setPriority(Integer.MAX_VALUE);  
        registerReceiver(mBroadcastReceiver, intentFilter); 
        
        
        

        mUpdateThread = new UpdateThread(this, mHandler);
        mUpdateThread.start();
        
        synchronized (mReservedUIMsgs) {
        	mReservedUIMsgs.clear();
		}
	}
	
	private void deinit(){
		if(init) {
			init = false;
			unregisterReceiver(mBroadcastReceiver);  
		}
	}
	
	private void setServerRunning(boolean b){
		Config.putBoolean("server_running", b);
		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/rowetalk/running";
		FileUtil.saveTxtFile(filePath, b?"1":"0");
	}
	
	private boolean startTask(){
		String err = startTaskInternal();
		if(err == null){
			StringBuilder sb = new StringBuilder();
			sb.append("// bt\n");
			sb.append("bt_name="+mBtName+"\n");
			sb.append("bt_addr="+mBtAddr+"\n");
			sb.append("// mifi\n");
			sb.append("mifi_name="+mMifiName+"\n");
			sb.append("mifi_addr="+mMifiAddr+"\n");
			sb.append("// wifi\n");
			sb.append("wifi_name="+mWifiName+"\n");
			sb.append("wifi_addr="+mWifiAddr+"\n");
			sb.append("// phone seq\n");
			sb.append("phone_seq="+mPhoneSeq+"\n");
			sb.append("pool_seq="+mPoolSeq+"\n");
			sb.append("slot_seqs="+mSlotSeqs+"\n");
			sb.append("sim_start="+mSimStart+"\n");
			sb.append("// vpn\n");
			sb.append("vpn_name="+mVpnName+"\n");
			sb.append("vpn_city="+mVpnCity+"\n");
			sb.append("vpn_server="+mVpnServer+"\n");
			sb.append("vpn_username="+mVpnUsername+"\n");
			sb.append("vpn_password="+mVpnPassword+"\n");
			sb.append("vpn_dns="+mVpnDns+"\n");
			sb.append("vpn_ppe="+mVpnPpe+"\n");
			sb.append("// checkbox\n");

			sb.append("enable_mifi="+cbEnableMifi+"\n");
			sb.append("enable_4g="+cbEnable4G+"\n");
			sb.append("enable_vpn="+cbEnableVPN+"\n");
			sb.append("no_sim="+cbNoSim+"\n");
			sb.append("enable_test="+cbEnableTest+"\n");
			sb.append("auto_start="+cbAutoStart+"\n");
			sb.append("log_file="+cbLogFile+"\n");
			sb.append("test_vpn="+cbTestVPN+"\n");
			sb.append("specify_vpn="+cbSpecifyVPN+"\n");
			//sb.append("overseas="+cbOverseas+"\n");
			sb.append("report_ip="+cbReportIP+"\n");
			//server

			sb.append("// server\n");

			sb.append("server_host="+mServerHost+"\n");


			String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/rowetalk/msg";
			FileUtil.saveTxtFile(filePath, "");
		    filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/rowetalk/config";
			FileUtil.saveTxtFile(filePath, sb.toString());
		}else{
			String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/rowetalk/msg";
			FileUtil.saveTxtFile(filePath, err);
		}
		return err == null;
	}
	
	private String startTaskInternal(){
		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(this, "蓝牙未开启！", Toast.LENGTH_SHORT).show();
			return "蓝牙未开启！";
		}
		sendBroadcast(new Intent(ACTION_START_AUTOBOT)); // notify autobot
		// init bt
		mBtName = Config.getString("bt_name", null);
        mBtAddr = Config.getString("bt_addr", null);
    	//tvBtDev.setText(mBtName!=null?mBtName+" "+mBtAddr:"");
    	// init mifi
    	mMifiName = Config.getString("mifi_name", null);
        mMifiAddr = Config.getString("mifi_addr", null);
		//tvMifiDev.setText(mMifiName!=null?mMifiName+" "+mMifiAddr:"");
		// init wifi
		mWifiName = Config.getString("wifi_name", null);
        mWifiAddr = Config.getString("wifi_addr", null);
        //tvWifiDev.setText(mWifiName!=null?mWifiName+" "+mWifiAddr:"");
        // init params
		mPhoneSeq = Config.getInt("phone_seq", 0);
		mPoolSeq = Config.getInt("pool_seq", 0);
		mSlotSeqs = Config.getString("slot_seqs", "");
		mSimStart = Config.getInt("sim_start", 0);
		mNoSimVdevieNum = Config.getInt("nosim_vdevice_num", 0);
		mNoSimStart = Config.getInt("nosim_start", 0);
		//setupParams(false);
		// init vpn
		mVpnName = Config.getString("vpn_name", null);
        mVpnCity = Config.getString("vpn_city", null);
        mVpnServer = Config.getString("vpn_server", null);
        mVpnUsername = Config.getString("vpn_username", null);
        mVpnPassword = Config.getString("vpn_password", null);
        mVpnDns = Config.getString("vpn_dns", "8.8.8.8");
        mVpnPpe = Config.getBoolean("vpn_ppe", true);
        //setupVpn(false);
        // init checkbox
		cbEnableMifi = Config.getBoolean("enable_mifi", false);
        cbEnable4G = Config.getBoolean("enable_4g", false);
        cbEnableVPN = Config.getBoolean("enable_vpn", false);
        cbNoSim = Config.getBoolean("no_sim", false);
        cbEnableTest = Config.getBoolean("enable_test", true);
        cbAutoStart = Config.getBoolean("auto_start", false);
        cbLogFile = Config.getBoolean("log_file", false);
        cbTestVPN = Config.getBoolean("test_vpn", false);
        cbSpecifyVPN = Config.getBoolean("specify_vpn", false);
        //cbOverseas = Config.getBoolean("overseas", false);
        cbReportIP = Config.getBoolean("report_ip", true);
        Log.e(TAG, "startTaskInternal: cbTestVPN="+cbTestVPN);


        mServerHost = Config.getString("server_host", null);
        // start 
        String workDir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/rowetalk/";
		File d = new File(workDir);
		if(!d.exists()){
			d.mkdirs();
		}
		d = new File(workDir);
		if(!d.exists()|| !d.isDirectory()){
			Toast.makeText(this, "无法创建工作目录:"+workDir, Toast.LENGTH_SHORT).show();
			return "无法创建工作目录:"+workDir;
		}
		if(!SystemUtil.isTyImeEnabled(this)){
			Toast.makeText(this, "TyIME 输入法未激活！", Toast.LENGTH_SHORT).show();
			return "TyIME 输入法未激活！";
		}

        if(cbEnableTest){
        	Constant.setSERVER_HOST(Constant.getSERVER_TEST());
        }else {
    		mServerHost = Constant.getSERVER_RELEASE();
    		Config.putString("server_host", mServerHost);
        	Constant.setSERVER_HOST(mServerHost);
        }
        
        if(mWifiAddr == null) {
			Toast.makeText(this, "Wifi设备未选定!", Toast.LENGTH_SHORT).show();
			return "Wifi设备未选定!";
		}
		
		if(!NetworkUtil.isWifiConnected(this)){
			Toast.makeText(this, "Wifi未连接!", Toast.LENGTH_SHORT).show();
			return "Wifi未连接!";
		}
		
		if(mPhoneSeq <= 1000) {
			Toast.makeText(this, "手机编号未正确设置!", Toast.LENGTH_SHORT).show();
			return "手机编号未正确设置!";
		}
		if(mNoSimVdevieNum < 100){
			Toast.makeText(this, "无卡虚拟设备数目设置不正确!", Toast.LENGTH_SHORT).show();
			return "无卡虚拟设备数目设置不正确!";
		}
		
		if(!cbNoSim) {
			if(cbEnable4G){
				Toast.makeText(this, "无SIM卡时无法激活4G!", Toast.LENGTH_SHORT).show();
				return "无SIM卡时无法激活4G!";
			}
			if(mPoolSeq <= 1000 || StringUtil.isEmpty(mSlotSeqs)){
				Toast.makeText(this, "卡槽参数未正确设置!", Toast.LENGTH_SHORT).show();
				return "卡槽参数未正确设置!";
			}
			if(mBtAddr == null){
				Toast.makeText(this, "蓝牙设备未选定!", Toast.LENGTH_SHORT).show();
				return "蓝牙设备未选定!";
			}
			
			
	     
		}
		int checked = 0;
		if(cbEnableMifi){ checked++; }
		if(cbEnable4G){ checked++; }
		if(cbEnableVPN){ checked++; }
		if(checked>=2){
			Toast.makeText(this, "启用Mifi,启用4G,启用VPN 只能三选一!", Toast.LENGTH_SHORT).show();
			return "启用Mifi,启用4G,启用VPN 只能三选一!";
		}

		

		if(cbEnableMifi){
			if(mMifiAddr == null) {
				Toast.makeText(this, "Mifi设备未选定!", Toast.LENGTH_SHORT).show();
				return "Mifi设备未选定!";
			}
			
			
			if(mWifiAddr.equals(mMifiAddr)){
				Toast.makeText(this, " WiFi 和 MiFi 地址相同!", Toast.LENGTH_SHORT).show();
				return " WiFi 和 MiFi 地址相同!";
			}
			
			if(!mWifiManager.getConnectionInfo().getRawBSSID().equals(mWifiAddr)) {
				if(!NetworkUtil.wifiConnectTo(this, mWifiName, mWifiAddr, null)) {
					Toast.makeText(this, "连接到 WiFi 失败！", Toast.LENGTH_SHORT).show();
					return "连接到 WiFi 失败！";
				}
			}
			
		}

        if(cbSpecifyVPN && StringUtil.isEmptyOrNull(mVpnServer)){
        	Toast.makeText(this, "指定VPN未正确设置!", Toast.LENGTH_SHORT).show();
			return "指定VPN未正确设置!";
        }

		startWorkThread();
		
		return null;
	}
	
	private void stopTask(){
		if(mThread != null){
			mThread.cancel(false);
		}
	}
	
	private void startWorkThread() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("sim_start", mSimStart);
		map.put("bt_addr", mBtAddr);
		map.put("enable_mifi", cbEnableMifi);
		map.put("enable_4g", cbEnable4G);
		map.put("enable_vpn", cbEnableVPN);
		map.put("test_vpn", cbTestVPN);
		map.put("enable_test", cbEnableTest);
		map.put("specify_vpn", cbSpecifyVPN);
		map.put("mifi_addr", mMifiAddr);
		map.put("wifi_addr", mWifiAddr);
		map.put("mifi_name", mMifiName);
		map.put("wifi_name", mWifiName);
		map.put("phone_seq", mPhoneSeq);
		map.put("pool_seq",mPoolSeq);
		map.put("slot_seqs", mSlotSeqs);
		map.put("nosim_vdevice_num", mNoSimVdevieNum);
		map.put("nosim_start", mNoSimStart);
		if(cbSpecifyVPN){
			VpnProfile v = new VpnProfile();
			v.account = new VpnAccount();
			v.account.setUsername(mVpnUsername);
			v.account.setPassword(mVpnPassword);
			v.account.setCity(mVpnCity);
			v.account.setServer(mVpnServer);
			v.account.setDescription(mVpnName+" "+mVpnServer);
			v.account.setDns(mVpnDns);
			v.account.setPpe((byte)(mVpnPpe?1:0));
			v.specified = true;
			map.put("vpn_profile", v);		
		}
		map.put("no_sim", cbNoSim);
		//map.put("overseas", cbOverseas);
		map.put("report_ip", cbReportIP);

		setWakeLock(true);
		Logger.setLogToFile(cbLogFile);
		
		mThread = new WorkThread(this, mHandler, sleepObject, map);
		mThread.start();
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver () {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(action == null) return;
			Log.e(TAG, "mBroadcastReceiver: onReceive "+intent.getAction());
			if (action.equals(LuaEnv.SMS_SENT)) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Logger.e(TAG, "SMS_SENT: resultCode=RESULT_OK");
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Logger.e(TAG, "SMS_SENT: resultCode=RESULT_ERROR_GENERIC_FAILURE");
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Logger.e(TAG, "SMS_SENT: resultCode=RESULT_ERROR_NO_SERVICE");
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Logger.e(TAG, "SMS_SENT: resultCode=RESULT_ERROR_NULL_PDU");
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Logger.e(TAG, "SMS_SENT: resultCode=RESULT_ERROR_RADIO_OFF");
					break;
				default:
					return;
				}
				LuaEnv.getInstance().notify("sendSms", getResultCode());
			} else if(action.equals(ACTION_SMS_RECEIVED)) {
				Bundle bundle = intent.getExtras();
				SmsMessage msg = null;  
		        if (null != bundle) {  
		            Object[] smsObj = (Object[]) bundle.get("pdus");  
		            List<SmsMessage> l = new ArrayList<SmsMessage>();
		            for (Object object : smsObj) {  
		                msg = SmsMessage.createFromPdu((byte[]) object);  
		                if(msg != null){
		                	l.add(msg);
		                }
		                Log.e(TAG, "recvsms: "+msg.getOriginatingAddress()+","+msg.getDisplayMessageBody());
		                /*Date date = new Date(msg.getTimestampMillis());//时间  
		                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		                String receiveTime = format.format(date);  
		                System.out.println("number:" + msg.getOriginatingAddress()  
		                + "   body:" + msg.getDisplayMessageBody() + "  time:"  
		                        + msg.getTimestampMillis());  
		                  
		                //在这里写自己的逻辑  
		                if (msg.getOriginatingAddress().equals("10086")) {  
		                    //TODO  
		                      
		                }  
		                */
		            }  
		            if(l.size()>0){
		            	LuaEnv.getInstance().notify("recvSms", Activity.RESULT_OK, l);
		            }
		        }  
			} else if(action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
				String phoneNumber = intent.getStringExtra(  
					TelephonyManager.EXTRA_INCOMING_NUMBER);  
				TelephonyManager telephony =   
						(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);  
				mCallState = telephony.getCallState();  
		        Logger.e(TAG, "ACTION_PHONE_STATE_CHANGED: mCallState="+mCallState+(mCallState==TelephonyManager.CALL_STATE_RINGING?", incomingNumber="+phoneNumber:""));
	            if(mCallState==TelephonyManager.CALL_STATE_RINGING && mThread != null) {
	            	mThread.notify_response("incoming_call", 0, phoneNumber);
	            }
		       
			}else if (action.equals(ACTION_SIM_STATE_CHANGED)) {
	            int state = mTelephonyManager.getSimState();
	            switch (state) {
	                case TelephonyManager.SIM_STATE_READY :
	                    //simState = SIM_VALID;
	                    break;
	                case TelephonyManager.SIM_STATE_UNKNOWN :
	                case TelephonyManager.SIM_STATE_ABSENT :
	                case TelephonyManager.SIM_STATE_PIN_REQUIRED :
	                case TelephonyManager.SIM_STATE_PUK_REQUIRED :
	                case TelephonyManager.SIM_STATE_NETWORK_LOCKED :
	                default:
	                    //simState = SIM_INVALID;
	                    break;
	            }
	            mSimState = state;
	            Logger.e(TAG, "ACTION_SIM_STATE_CHANGED: "+state);
				if(state==TelephonyManager.SIM_STATE_READY && mThread!=null){
					mThread.notify_response("sim_reset", state, null);
				}
			
			}else if(action.equals(ACTION_IME_INPUT_CONFIRM)){
				LuaEnv.getInstance().notify("ime_confirm", Activity.RESULT_OK, null);
			}
		}
		
	};
	
	public void updateStatus(final String status){
		postUIMsg(MSG_UPDATE_THREAD_STATUS, status);
	}
	private void postUIMsg(int what, String str){
		Logger.e(TAG, "postUIMsg: what="+what+",msg="+str);
		if(MainActivity.exist) {
			Intent intent  = new Intent(ACTION_UI_MSG);
			intent.putExtra("what", what);
			intent.putExtra("msg", str);
			sendBroadcast(intent);
		}else{
			synchronized (mReservedUIMsgs) {
				if(mReservedUIMsgs.size()>100) mReservedUIMsgs.remove(0);
				Date d = new Date();
				Message msg = new Message();
				msg.what = what;
				msg.obj = str;
				msg.arg1 = (int)d.getTime();
				msg.arg2 = (int)(d.getTime() << 32);
				mReservedUIMsgs.add(msg);
			}
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//super.handleMessage(msg);
			switch(msg.what){
			case BaseThread.MSG_THREAD_STARTED:
				setServerRunning(true);
				postUIMsg(msg.what, "thread started: "+NetworkUtil.getLocalIpAddress(MainService.this));
				break;
			case BaseThread.MSG_THREAD_ENDED:
				setServerRunning(false);
				Logger.setLogToFile(false);
				postUIMsg(msg.what, "thread ended: "+NetworkUtil.getLocalIpAddress(MainService.this));
				mThread = null;
				setWakeLock(false);
				//NetworkUtil.switchVPN(MainService.this, false, null);
				boolean wifiConnected = false;
				if(!NetworkUtil.connectedWifi(MainService.this, mWifiAddr)){
					if(NetworkUtil.wifiConnectTo(MainService.this, mWifiName, mWifiAddr, null)) {
						wifiConnected = true;
					}
				}else if(NetworkUtil.isWifiConnected(MainService.this)){
					wifiConnected = true;
				}
				if(cbAutoStart && wifiConnected){
					mHandler.sendEmptyMessageDelayed(MSG_START_TASK, 5*60*1000); // restart after 5 minutes
				}
				break;
			case BaseThread.MSG_THREAD_INFO:
				postUIMsg(msg.what, (String)msg.obj);
				break;
			case BaseThread.MSG_DIAL_NUMBER:
				//showMessageLog("info: dial "+edtPhoneNumber.getText().toString());
				//RootShellCmd.getInstance().dial(edtPhoneNumber.getText().toString());
				break;
			case BaseThread.MSG_FALSE_SLOTS:
				postUIMsg(msg.what, (String)msg.obj);
				break;
			case BaseThread.MSG_FALSE_TASKS:
				postUIMsg(msg.what, (String)msg.obj);
				break;
			case BaseThread.MSG_CARD_IN_JOB:
				postUIMsg(msg.what, (String)msg.obj);
				break;
			case MSG_START_TASK:
				startTask();
				break;
			case BaseThread.MSG_VPN_INFO:
				postUIMsg(msg.what, (String)msg.obj);
				break;
			case MSG_SCRIPT_FINISHED:
				Log.e(TAG, "script finished: "+(String)msg.obj);
				postUIMsg(msg.what, (String)msg.obj);
				Toast.makeText(MainService.this, (String)msg.obj, Toast.LENGTH_SHORT).show();
				mTestScriptThread = null;
				break;
			case MSG_UPDATE_APP:
				UpdateInfo info = (UpdateInfo)msg.obj;
				if(info != null){
					if(mThread != null){
						mThread.cancel(false);
						Config.putBoolean("service_running_interrupted", true);
					}
					Intent i = new Intent(MainActivity.ACTION_UPDATE_APP);
					i.putExtra("file_path", info.filePath);
					i.putExtra("package_name", info.packageName);
					i.putExtra("launcher_activity", info.launcherActivity);
					sendBroadcast(i);
				}
				break;
			}
		}
		
	};
	
	private void setWakeLock(boolean b) {
		Logger.e(TAG, "setWakeLock: "+b);
		if(b){
	        wakeLock.acquire();
	        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}else{
			wakeLock.release();
	        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		//super.onStart(intent, startId);
		Logger.e(TAG, "onStart");
		init();
		if(intent == null) return;
		String action = intent.getAction();
		if(action == null) {
			if(Config.getBoolean("service_running_interrupted", false)){
				startTask();
				Config.putBoolean("service_running_interrupted", false);
			}
			return;
		}
		if(action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			Logger.e(TAG, "onStart: action=ACTION_BOOT_COMPLETED");
			startTask();
		}else if(action.equals(ACTION_ROWETALK_UPDATE_CHECK)){
			Logger.e(TAG, "onStart: action=ACTION_ROWETALK_UPDATE_CHECK");
			if(mUpdateThread != null && mUpdateThread.isWaiting()){
				mUpdateThread.immediate_check_update();
			}
		}else if(action.equals(ACTION_ROWETALK_START_SERVICE)){
			Logger.e(TAG, "onStart: action=ACTION_ROWETALK_START_SERVICE");
			if(mThread==null && startTask()) {
				Logger.e(TAG, "startTask success");
			}else{
				postUIMsg(BaseThread.MSG_THREAD_ENDED, "start task failed. somewhare error!");
				Logger.e(TAG, "startTask failed. somewhare error!");
			}
		}else if(action.equals(ACTION_ROWETALK_STOP_SERVICE)){
			Logger.e(TAG, "onStart: action=ACTION_ROWETALK_STOP_SERVICE");
			int keepapp = intent.getIntExtra("keep_app", 0);
			if(mThread != null){
				mThread.cancel(keepapp == 1);
			}else{
				postUIMsg(BaseThread.MSG_THREAD_ENDED, "service not working");
			}
		}else if(action.equals(ACTION_ROWETALK_DEBUG_NEXT_STEP)) {
			LuaEnv.getInstance().putParam("next_step", "true");
		}else if(action.equals(ACTION_ROWETALK_TEST_SCRIPT)) {
			if(mThread != null){
				Logger.e(TAG, "task is running, ignore");
			}else {
				if(mTestScriptThread != null){
					Logger.e(TAG, "script thread is running, ignore");
					LuaEnv.getInstance().endScript(true);
					LuaEnv.getInstance().deinitOcr();
					mTestScriptThread = null;
				}
				if(!RootShellCmd.getInstance().setImeTy()){
					Toast.makeText(MainService.this, "setImeTy failed.", Toast.LENGTH_SHORT).show();
					return;
				}
				/*
				if(!RootShellCmd.getInstance().checkRoot()){
					Toast.makeText(MainService.this, "系统尚未 ROOT!", Toast.LENGTH_SHORT).show();
				}
				*/
				
				String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tmp/test.apk";
		        if(!new File(apkPath).exists()){
		        	Toast.makeText(MainService.this, "Apk file not found: "+apkPath, Toast.LENGTH_SHORT).show();
		        }

		        final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tmp/test.lua";
		        if(!new File(filePath).exists()){
		        	Toast.makeText(MainService.this, "Lua file not found. "+filePath, Toast.LENGTH_SHORT).show();
		            return;
		        }
		        

		        LuaEnv.getInstance().initOcr();
		        LuaEnv.getInstance().startScript(sleepObject);
		        
		        final int debug_step = intent.getIntExtra("debug_step", 0);
		        final boolean retry_when_fail = intent.getIntExtra("retry_when_fail", 0)!=0;
		        Log.e(TAG, "retry_when_fail: "+retry_when_fail);
		        mTestScriptThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						LuaState L = LuaEnv.getInstance().L;
				        long t1 = System.currentTimeMillis();
				        //LuaEnv.getInstance().doAsset("test_apk_weixin.lua");
				        LuaEnv.getInstance().putParam("test_script", "true");
				        
				        LuaEnv.getInstance().putParam("phone_seq", String.valueOf(Config.getInt("phone_seq",0)));
				        LuaEnv.getInstance().putParam("debug_step", debug_step>0?"true":"false");
				        int retry_coutn= 0;
				        while(true) {
					        boolean success = LuaEnv.getInstance().doFile(filePath);
							long t2 = System.currentTimeMillis();
							if(success) {
								L.getGlobal("return_status");
								boolean result = L.toBoolean(-1);
								if(result){
									L.getGlobal("return_result");
									String message = L.toString(-1);
									mHandler.obtainMessage(MSG_SCRIPT_FINISHED, 
											"testScript success:"+(message!=null?message:"")+" duration "+DateUtil.secondToTime((int) ((t2-t1)/1000))).sendToTarget();	
								}else {
									L.getGlobal("return_result");
									String message = L.toString(-1);
									mHandler.obtainMessage(MSG_SCRIPT_FINISHED, "testScript failed: "+message).sendToTarget();	
								}
								break;
							}else{
								if(!retry_when_fail){
									mHandler.obtainMessage(MSG_SCRIPT_FINISHED, "testScript failed2: "+L.toString(-1)).sendToTarget();	
									break;
								}
								else {
									Log.e(TAG, "testScript failed2: "+L.toString(-1));
									Log.e(TAG, "retry_when_fail: "+retry_coutn);
									retry_coutn++;
								}
							}
				        }
					}
				});
		        mTestScriptThread.start();
			}
		}else if(action.equals(ACTION_ROWETALK_VIEW_CMD)){
			int op = intent.getIntExtra("control", -1);
			if(op == 0){
				boolean b = RootShellCmd.getInstance().isViewServerRunning();
				Logger.e(TAG, "viewServer running: "+b);
			}else if(op == 1){
				boolean b = RootShellCmd.getInstance().startViewServer();
				b = RootShellCmd.getInstance().isViewServerRunning();
				Logger.e(TAG, "viewServer running: "+b);
			}else if(op == 2){
				boolean b = RootShellCmd.getInstance().stopViewServer();
				b = RootShellCmd.getInstance().isViewServerRunning();
				Logger.e(TAG, "viewServer running: "+b);
			}
			final String cmd = intent.getStringExtra("cmd");
			if(cmd == null) return;
			if(cmd.startsWith("CAPTURE")){
				new Thread(new Runnable() {
					public void run() {
						sendViewCaptureCmd(cmd);
					}
				}).start();
				
			}else {
				// LIST, SERVER, PROTOCOL, DUMP www, PROFILE www YYY, INVALIDATE www, REQUEST_LAYOUT www, OUTPUT_DISPLAYLIST www
				new Thread(new Runnable() {
					public void run() {
						sendViewCmd(cmd, null);  
					}
				}).start();
				
				
			}
		}
	}

	private boolean sendViewCaptureCmd(String cmd){
		Logger.e(TAG, "sendViewCmd: "+cmd);
		Socket socket = null;
		BufferedWriter out = null;
		BufferedInputStream in = null;
		boolean b = false;
		try{
		    socket = new Socket();
		    socket.connect(new InetSocketAddress(InetAddress.getLocalHost(),4939),40000);
		    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		    in = new BufferedInputStream(socket.getInputStream());
		    out.write(cmd);
		    out.newLine();
		    out.flush();

		    Bitmap bmp = BitmapFactory.decodeStream(in);
		    if(bmp != null){
	    		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+
						"/tmp/cap_"+DateUtil.getCurrentDate("yyyyMMdd_HHmmss")+".png";
	    		File file = new File(filePath);

				if(file.exists()){
	        		file.delete();
	        	}else{
	        		File d = new File(file.getParent());
	        		if(!d.exists()) d.mkdirs();
	        	}
				file.createNewFile();
		        FileOutputStream out2 = new FileOutputStream(file);
		        bmp.compress(Bitmap.CompressFormat. PNG, 100, out2);
		        Logger.e(TAG, "File saved: "+filePath);
		        b = true;
		    	
		        bmp.recycle();
		    }
		    
		} catch (Exception e) {
            // Empty
			e.printStackTrace();
			Logger.e(TAG, "sendViewCaptureCmd Exception: "+e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
		return b;
	}
	private boolean sendViewCmd(String cmd, StringBuilder sb){
		Logger.e(TAG, "sendViewCmd: "+cmd);
		Socket socket = null;
		BufferedWriter out = null;
		BufferedReader in = null;
		boolean b = false;
		try{
			//Logger.e(TAG, "cmd: "+cmd);
		    socket = new Socket();
		    socket.connect(new InetSocketAddress(InetAddress.getLocalHost(),4939),40000);
		    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		    in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
		    out.write(cmd);
		    out.newLine();
		    out.flush();
		   
		  //http://blog.csdn.net/dalianmaoblog/article/details/11098751
		  //receive response from viewserver
		  //Output: 21d12790 com.example.testimsi/com.example.testimsi.MainActivity

		   String line;
		   while((line = in.readLine()) != null) {
			   if("DONE.".equalsIgnoreCase(line))
			   { //$NON-NLS-1$
		         break;
		       }
			   Logger.e(TAG, "Output: "+line);
			   if(sb != null){
				   sb.append(line+"\n");
			   }
		   }
		  
		   b = true;
		} catch (Exception e) {
            // Empty
			e.printStackTrace();
			Logger.e(TAG, "sendViewCmd Exception: "+e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
		return b;
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		stopTask();
		super.onDestroy();
	}
	
	protected void postInfo(String msg) {
		Logger.e(TAG, "postInfo: "+msg);
		mHandler.obtainMessage(BaseThread.MSG_THREAD_INFO, msg).sendToTarget();
	}
	
	
}
