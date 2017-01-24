package com.example.rowetalk.thread;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.example.rowetalk.bean.Device;
import com.example.rowetalk.bean.EnumType;
import com.example.rowetalk.bean.EnvProfile;
import com.example.rowetalk.bean.Platform;
import com.example.rowetalk.bean.SimSlot;
import com.example.rowetalk.bean.VpnAccount;
import com.example.rowetalk.bean.VpnProfile;
import com.example.rowetalk.util.Constant;
import com.example.rowetalk.util.FileUtil;
import com.example.rowetalk.util.Generator;
import com.example.rowetalk.util.HttpUtil;
import com.example.rowetalk.util.Logger;
import com.example.rowetalk.util.NetworkUtil;
import com.example.rowetalk.util.PlatformUtil;
import com.example.rowetalk.util.RootShellCmd;
import com.example.rowetalk.util.ServerUtil;
import com.example.rowetalk.util.SimpleLock;
import com.example.rowetalk.util.SleepObject;
import com.example.rowetalk.util.StringUtil;
import com.liblua.LuaEnv;

import android.R.raw;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BaseThread extends Thread {
	private static final String TAG = BaseThread.class.getName();
	protected Context mContext;
	protected WeakReference<Handler> mHandler;
	protected boolean isRunning = false;


	protected SimpleLock mLock = new SimpleLock();
	protected final static int RESULT_INTERRUPT =  -9998;
	protected final static int RESULT_DEFAULT =  -9999;
	
	public static final int MSG_THREAD_STARTED = 100;
	public static final int MSG_THREAD_ENDED = 101;
	public static final int MSG_THREAD_INFO = 102;
	public static final int MSG_DIAL_NUMBER = 104;
	public static final int MSG_BT_CONNECT_FAILED = 105;
	public static final int MSG_BT_CONNECT_SUCCESS = 106;
	public static final int MSG_FALSE_SLOTS = 107;
	public static final int MSG_FALSE_TASKS = 108;
	public static final int MSG_CARD_IN_JOB = 109;
	public static final int MSG_WORKER_SLEEP = 110;
	public static final int MSG_VPN_INFO = 111;
	public static final int MSG_SUCCESS_TASKS = 112;
	
	protected BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	protected MyPhoneStateListener myListener;
	protected TelephonyManager mTelephonyManager;

	protected int mSignalStength = 0;
	protected int mServiceState= ServiceState.STATE_POWER_OFF;
	protected int mCallState = TelephonyManager.CALL_STATE_IDLE;
	
	protected String mPhoneId;
	protected EnvProfile mEnvProfile;

	
	protected RootShellCmd shell = RootShellCmd.getInstance();
	
	protected int mSimRangeStart;
	protected int mSimRangeEnd;
	protected int mSimStart;
	protected int mSimCardId;
	protected int mNoSimVdeviceId;
	protected int mNoSimVdevieNum;
	protected static final int SIM_CARD_CONT_FAIL_MAX = 1;

	protected String mImsi = null;
	
	protected boolean mEnableMifi;
	protected String mMifiAddr;
	protected String mWifiAddr;
	protected String mMifiName;
	protected String mWifiName;
	protected WifiManager mWifiManager;
	protected boolean mEnable4G;
	protected boolean mEnableVPN;
	//protected boolean mEnableTest;
	protected boolean mSpecifyVPN;
	protected boolean mTestVPN;
	protected boolean mNoSimPool;
	
	protected int mPhoneSeq;
	protected int mPoolSeq;
	protected String mSlotSeqs;
	protected ArrayList<Integer> mSlotSeqList;
	
	protected String failedSlots;
	protected String failedTasks;
	protected String successTasks;
	
	protected VpnProfile mVpnProfile;
	//protected boolean mOverseas;
	protected boolean mReportIp;
	private static BtThread mBtThread;
	private BluetoothDevice mBluetoothDevice;
	private String mBtAddr;
	protected SleepObject sleepObject;

    protected String mMyIp;
	
	public int getCurrentSimcardId() {
		return mSimCardId;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	


	public BaseThread(Context context, Handler handler, SleepObject obj, Map<String, Object> map){
		mContext = context;
		mHandler = new WeakReference<Handler>(handler);
		sleepObject = obj;
		mTelephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		myListener = new MyPhoneStateListener(this);
		
		mSimStart = (map.containsKey("sim_start")?(Integer)map.get("sim_start"):0);
		mSimCardId = mSimStart;
		mEnableMifi = (map.containsKey("enable_mifi")?(Boolean)map.get("enable_mifi"):false);
		mMifiAddr = (String)map.get("mifi_addr");
		mWifiAddr = (String)map.get("wifi_addr");
		mMifiName = (String)map.get("mifi_name");
		mWifiName = (String)map.get("wifi_name");
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
		mEnable4G = (map.containsKey("enable_4g")?(Boolean)map.get("enable_4g"):false);
		mEnableVPN = (map.containsKey("enable_vpn")?(Boolean)map.get("enable_vpn"):false);
		//mEnableTest = (Boolean)map.get("enable_test");
		mSpecifyVPN = (map.containsKey("specify_vpn")?(Boolean)map.get("specify_vpn"):false);
		mTestVPN = (map.containsKey("test_vpn")?(Boolean)map.get("test_vpn"):false);

		mPhoneSeq = (map.containsKey("phone_seq")?(Integer)map.get("phone_seq"):0);
		mPoolSeq = (map.containsKey("pool_seq")?(Integer)map.get("pool_seq"):0);
		mSlotSeqs = (String)map.get("slot_seqs");
		mSlotSeqList = new ArrayList<Integer>();
		mNoSimVdevieNum = (map.containsKey("nosim_vdevice_num")?(Integer)map.get("nosim_vdevice_num"):0);
		mNoSimVdeviceId = (map.containsKey("nosim_start")?(Integer)map.get("nosim_start"):0);
		if(mNoSimVdeviceId<0){
			mNoSimVdeviceId = 0;
		}else if(mNoSimVdeviceId>=mNoSimVdevieNum){
			mNoSimVdeviceId=mNoSimVdevieNum-1;
		}
		
		failedSlots = (String)map.get("false_slots");
		failedTasks = (String)map.get("false_taks");
		successTasks = (String)map.get("success_taks");
		if(failedSlots == null){
			failedSlots ="";
		}
		if(failedTasks == null){
			failedTasks ="";
		}
		if(successTasks == null){
			successTasks ="";
		}
		if(mSpecifyVPN) {
			mVpnProfile = (VpnProfile)map.get("vpn_profile");
		}
		mNoSimPool = (map.containsKey("no_sim")?(Boolean)map.get("no_sim"):false);
		//mOverseas = (map.containsKey("overseas")?(Boolean)map.get("overseas"):false);
		mReportIp = (map.containsKey("report_ip")?(Boolean)map.get("report_ip"):true);
		mBtAddr = (String)map.get("bt_addr");
		
	}
	public void cancel(boolean keep_app_state) {
		isRunning = false;
		LuaEnv.getInstance().endScript(keep_app_state);
		wakeSleep();
		interrupt();
	}
	
	protected boolean init(){
		if(!turnOffVPN()){
			postInfo("turnOffVPN failed!");
			return false;
		}
		if(mSpecifyVPN && mVpnProfile == null) {
			return false;
		}
		postInfo("init luaEnv");
		LuaEnv.getInstance().initOcr();//???
        LuaEnv.getInstance().startScript(sleepObject);
        postInfo("init luaEnv done.");
        
		mSlotSeqList.clear();
		if(!mNoSimPool) {
			//bluetooth
			Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
	        // If there are paired devices, add each one to the ArrayAdapter
			mBluetoothDevice = null;
	        if (pairedDevices.size() > 0) {
	            for (BluetoothDevice bd : pairedDevices) {
	                if(bd.getAddress().equals(mBtAddr)){
	                	mBluetoothDevice = bd;
	                	break;
	                }
	            }
	        }
	        if(mBluetoothDevice == null){
	        	postInfo("BT设备找不到!");
				return false;
	        }
	        
			if(!initSlotSeqList(mSlotSeqList, mSlotSeqs)){
				postInfo("initSlotSeqList failed.");
				return false;
			}
			mSimRangeStart = -1;
			mSimRangeEnd = -1;
			for(int i=0;i<mSlotSeqList.size();i++){
				postInfo(String.format("Slot %d = %d", i, mSlotSeqList.get(i)));
				int num = mSlotSeqList.get(i);
				if(num != 0){
					if(mSimRangeStart < 0){
						mSimRangeStart = i*16;
					}
					mSimRangeEnd = (i+1) * 16;
				}
			}
	
			if(mSimRangeStart < 0 || mSimRangeEnd <1){
				postInfo("initSlotSeqList: bad sim range: "+String.format("%d,%d", mSimRangeStart, mSimRangeEnd));		
				return false;
			}
			if(mSimStart >= mSimRangeEnd){
				mSimStart = mSimRangeEnd-1;
			}
			if(mSimStart < mSimRangeStart){
				mSimStart = mSimRangeStart;
			}

			mSimCardId = mSimStart;
			postInfo("initSlotSeqList: "+String.format("sim range (%d,%d) start %d", mSimRangeStart, mSimRangeEnd, mSimStart));
			
		}
		while(isRunning){
			mMyIp = ServerUtil.getMyIp();
			if(mMyIp!=null){
				break;
			}else{
				postInfo("wait 3s to retry getMyIp");
				enterSleep(3000);
			}
		}
		postInfo("myIp="+mMyIp);
		
		/*
		if(!RootShellCmd.getInstance().isViewServerRunning()) {
			if(!RootShellCmd.getInstance().startViewServer()){
				postInfo("startViewServer failed.");	
				return false;
			}
		}
		postInfo("viewServer is running");
		*/
		return true;
	}
	
	// 空槽数-start-空槽数-end-空槽数
	// 空槽数可选
	// 1001-1016
	// 1001-2-1016
	// 2-1003-1016
	// 2-1003-2-1013-2-1016
	private static boolean initSlotSeqListBySlot(ArrayList<Integer> slotSeqList, String seqParams){
		if(StringUtil.isEmptyOrNull(seqParams)){
			Logger.e(TAG, "initSlotSeqListByPool: seqParams is null");
			return false;
		}
		String[] arr = seqParams.split("-");
		if(arr == null || arr.length == 0){
			Logger.e(TAG, "initSlotSeqList: invalid format: "+ seqParams);
			return false;
		}
		int last_num = 0;
		int num_count = 0;
		int last_slot = 0;
		for(String s: arr){
			if(s.length()==0){
				Logger.e(TAG, "initSlotSeqList: invalid format: "+ seqParams);
				return false;
			}
			int num = Integer.parseInt(s);
			if(num>1000){
				if(last_slot != 0){
					for(int i=last_slot+1; i<=num;i++){
						slotSeqList.add(i);
						num_count++;
					}
				}else{
					slotSeqList.add(num);
					num_count++;
				}
				last_slot = num;
			}else {
				for(int i=0;i<num;i++){
					slotSeqList.add(0);
				}
				if(last_slot > 0){
					last_slot+=num;
				}
			}
			last_num = num;
		}
		if(num_count == 0){
			Logger.e(TAG, "initSlotSeqList: no valid params found : "+ seqParams);
			return false;
		}
		return true;
	}
	//不连续卡槽用空格区分，如 1001-1016 1049-1064
	public static boolean initSlotSeqList(ArrayList<Integer> slotSeqList, String seqParams){
		if(StringUtil.isEmptyOrNull(seqParams)){
			Logger.e(TAG, "initSlotSeqList: seqParams is null");
			return false;
		}
		String[] slots = seqParams.split("\\s");
		for(int i=0; i<slots.length;i++){
			if(!initSlotSeqListBySlot(slotSeqList, slots[i])){
				return false;
			}
		}
		return true;
	}
	
	
	
	
	protected void deinit(){
		LuaEnv.getInstance().deinitOcr();
	}
	
	
	protected void postStartedMsg() {
		if(mHandler.get()!=null)
		mHandler.get().sendEmptyMessage(MSG_THREAD_STARTED);
	}
	protected void postEndedMsg() {
		if(mHandler.get()!=null)
		mHandler.get().sendEmptyMessage(MSG_THREAD_ENDED);
	}
	protected void postInfo(String msg) {
		//Logger.e(TAG, "postInfo: "+msg);
		if(mHandler.get()!=null)
		mHandler.get().obtainMessage(MSG_THREAD_INFO, msg).sendToTarget();
	}
	protected void postFalseSlots(String msg) {
		Logger.e(TAG, "postFalseSlots: "+msg);
		/*String[] arr = msg.split(",");
		String output = "";
		int last_num = -1;
		for(String s:arr){
			s=s.trim();
			last_num = Integer.parseInt(s);
			
		}*/
		if(mHandler.get()!=null)
		mHandler.get().obtainMessage(MSG_FALSE_SLOTS, msg).sendToTarget();
	}
	protected void postFalseTasks(String msg) {
		Logger.e(TAG, "postFalseTasks: "+msg);
		if(mHandler.get()!=null)
		mHandler.get().obtainMessage(MSG_FALSE_TASKS, msg).sendToTarget();
	}
	protected void postSuccessTasks(String msg) {
		Logger.e(TAG, "postSuccessTasks: "+msg);
		if(mHandler.get()!=null)
		mHandler.get().obtainMessage(MSG_SUCCESS_TASKS, msg).sendToTarget();
	}
	protected void postCurrentCard(String msg) {
		Logger.e(TAG, "postCurrentCard: "+msg);
		if(mHandler.get()!=null)
		mHandler.get().obtainMessage(MSG_CARD_IN_JOB, msg).sendToTarget();
	}
	protected void postWorkerSleep(int ms, int taskCount){
		if(mHandler.get()!=null)
			mHandler.get().obtainMessage(MSG_WORKER_SLEEP, ms, taskCount).sendToTarget();
	}
	protected void postVpn(String vpn){
		Logger.e(TAG, "postVpn: "+vpn);
		if(mHandler.get()!=null)
			mHandler.get().obtainMessage(MSG_VPN_INFO, vpn).sendToTarget();
	}
	
	
	public int wait_response(String name){
		return wait_response(name, 0);
	}
	public int wait_response(String name, long ms){
		Logger.e(TAG, "wait_response:"+name+", "+ms);
		try{
			synchronized(mLock) {
				mLock.name = name;
				mLock.result = RESULT_DEFAULT;
				mLock.obj = null;
				mLock.wait(ms);
				return mLock.result;
			}
		}catch(InterruptedException e){
			
		}
		return RESULT_INTERRUPT;
	}
	
	public Object getLockContent() {
		synchronized(mLock) {
			return mLock.obj;
		}
	}
	
	public void notify_interrupt(){
		synchronized(mLock) {
			mLock.result = RESULT_INTERRUPT;
			mLock.name= "";
			mLock.obj = null;
			mLock.notifyAll();
		}
	}
	public void notify_response(String name, int result, Object msg){
		synchronized(mLock) {
			if(mLock.name.equals(name)){
				Logger.e(TAG, "notify_response:"+name+", "+result+", "+msg);
				mLock.result = result;
				mLock.name= "";
				mLock.obj = msg;
				mLock.notifyAll();
			}
		}
	}
	
	private class MyPhoneStateListener extends PhoneStateListener
    {
		private BaseThread mThread;
		
		public MyPhoneStateListener(BaseThread thread) {
			mThread = thread;
		}
 
		/* Get the Signal strength from the provider, each tiome there is an update */
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength)
		{
		     super.onSignalStrengthsChanged(signalStrength);
		     mSignalStength = signalStrength.getGsmSignalStrength();
		     Logger.e(TAG, "MyPhoneStateListener: mSignalStength="+mSignalStength);
		}

		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			// TODO Auto-generated method stub
			super.onServiceStateChanged(serviceState);
		    mServiceState = serviceState.getState();
			Logger.e(TAG, "MyPhoneStateListener: mServiceState="+mServiceState);
		    //if(MainActivity.mSimState == TelephonyManager.SIM_STATE_READY && mServiceState==ServiceState.STATE_IN_SERVICE && mThread!=null){
		    //	mThread.notify_response("service_state", mServiceState, null);
			//}
		}
		
		@Override  
        public void onCallStateChanged(int state, String incomingNumber) { 
            super.onCallStateChanged(state, incomingNumber);  
            Logger.e(TAG, "MyPhoneStateListener: mCallState="+mCallState+(mCallState==TelephonyManager.CALL_STATE_RINGING?", incomingNumber="+incomingNumber:""));
            //if(mCallState==TelephonyManager.CALL_STATE_RINGING && mThread != null) {
            //	mThread.notify_response("incoming_call", 0, incomingNumber);
            //}
        }  
      
    }

	protected boolean registerPhoneStatus(int status, String info){
		if(mEnableMifi || mEnable4G || mEnableVPN){
			if(!mWifiManager.getConnectionInfo().getRawBSSID().equals(mWifiAddr)) {
				if(!NetworkUtil.wifiConnectTo(mContext, mWifiName, mWifiAddr, sleepObject)) {
					postInfo ("wifi not connected to "+mWifiAddr);
					return false;
				}
			}
		}
		if(mEnableVPN){
			if(!turnOffVPN()) {
				postInfo("turnOffVPN failed.");
				return false;
			}
		}
		try  
        {
			OkHttpClient client = new OkHttpClient(); 
			FormBody.Builder builder = new FormBody.Builder()
				    .add("phone_seq", String.valueOf(mPhoneSeq))
				    .add("status", String.valueOf(status))
				    .add("model", Build.MODEL)
				    .add("hardware", Build.HARDWARE);
			if(info!=null){
				builder.add("info", info);
			}
			postInfo("registerPhoneStatus: phone_seq="+mPhoneSeq+",status="+status);
			Request request = new Request.Builder()  
		            .url(Constant.getURL_REGISTER_PHONE())  
		            .post(builder.build())
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            postInfo("registerPhoneStatus: response erroNo=" + errorNo);
            if(errorNo!=null && errorNo.equals("0")){
            	JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					mPhoneId = dataObj.optString("phone_id");
					return StringUtil.isNotEmpty(mPhoneId);
				}
            }
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
        	postInfo("registerPhoneStatus Exception: "+e);
        }  
		return false; 	
	}
	

	protected boolean registerDevice(boolean no_sim) {
		try  
        {
			OkHttpClient client = new OkHttpClient(); 
			FormBody.Builder builder = new FormBody.Builder()
				    .add("phone_id", mPhoneId)
				    .add("has_sim", no_sim?"0":"1");
			if(no_sim){
				builder.add("nosim_key", String.format("%d-%06d", mPhoneSeq,mNoSimVdeviceId+1));
			}else{
				builder.add("sim_imsi", mImsi);
			}
			postInfo("registerDevice: phone_seq="+mPhoneSeq+",has_sim="+(!no_sim)+ (no_sim?(",nosim_dev="+mNoSimVdeviceId):(",sim_imsi="+mImsi)));
			Request request = new Request.Builder()  
		            .url(Constant.getURL_REGISTER_DEVICE())  
		            .post(builder.build())
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);

		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "registerDevice: response erroNo=" + errorNo);
            if(errorNo!=null && errorNo.equals("0")){
            	JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					Device d = null;
					Platform p = null;
					String sim_str = null;
					String d_str = dataObj.optString("device");
					if(StringUtil.isNotEmpty(d_str)){
						d = JSON.parseObject(d_str, Device.class);
					}
					String p_str = dataObj.optString("platform");
					if(StringUtil.isNotEmpty(p_str)){
						p = JSON.parseObject(p_str, Platform.class);
					}
					Logger.e(TAG, "registerDevice: device=" + d_str+",platform="+p_str+",sim="+sim_str);
					if(d != null && p != null){
						mEnvProfile = new EnvProfile();
						mEnvProfile.device = d;
						mEnvProfile.platform = p;
						return true;
					}
				}
            }
        } catch (Exception e) {  
        	e.printStackTrace();
        	Logger.e(TAG, "registerDevice Exception: "+e);
        }  
		return false; 	
	}
	
	
	
	public static boolean updateEmuData(boolean on, EnvProfile p, SleepObject sleep){
		if(p == null && on == true) return false;

        boolean saved = false;
		Log.e(TAG, "updateEmuData: "+on);
		if(on){
			Logger.e(TAG, "updateEmuData: platform_id="+p.platform.getId());
			StringBuilder sb = new StringBuilder();
			sb.append("ro.build.type=user");
            sb.append("\n");
            

        	Random r = new Random();
        	r.setSeed(System.currentTimeMillis());
        	
            if(p.platform.getBdVerSdkInt()!=null){
            	String s = p.platform.getBdVerSdkInt();
            	if(s.equals("24")) {
            		p.platform.setBdVerRel("7.0.1");
            	}else if(s.equals("23")) {
            		if(r.nextInt(100)<80)
            			p.platform.setBdVerRel("6.0.1");
            		else
            			p.platform.setBdVerRel("6.0");
            	}else if(s.equals("22")) {
            		if(r.nextInt(100)<80)
            			p.platform.setBdVerRel("5.1");
            		else
            			p.platform.setBdVerRel("5.1.1");
            	}else if(s.equals("21")) {
            		p.platform.setBdVerRel("5.0.1");
            	}else if(s.equals("19")) {
            		p.platform.setBdVerRel("4.4.2");
            	}else if(s.equals("18")) {
            		p.platform.setBdVerRel("4.3.1");
            	}else if(s.equals("17")) {
            		p.platform.setBdVerRel("4.2.2");
            	}else if(s.equals("16")) {
            		p.platform.setBdVerRel("4.1.2");
            	}else if(s.equals("15")) {
            		p.platform.setBdVerRel("4.0.4");
            	}else if(s.equals("14")) {
            		p.platform.setBdVerRel("4.0.2");
            	}else if(s.equals("13")) {
            		p.platform.setBdVerRel("3.2");
            	}
            }
            if(p.platform.getBdUser() == null){
            	p.platform.setBdUser(Generator.RandomBdUser(r));
            	p.platform.setBdHost(p.platform.getBdUser());
            }
            if(p.platform.getBdTime() == null){
            	String bdTime = String.format("1%d%s%d", 3000+r.nextInt(2500), r.nextBoolean()?"1":"0", r.nextInt(9999));
            	p.platform.setBdTime(bdTime);
            }
            if(p.platform.getBdId()==null){
            	p.platform.setBdId(Generator.RandomBdID(r));
            }
            if(p.platform.getBdDisplay() == null){
            	p.platform.setBdDisplay(String.format("%s-user %s %s %s test-keys",
            			p.platform.getBdProduct(),  p.platform.getBdVerRel(), p.platform.getBdId(),
            			p.platform.getBdTime()));
            }
            
            
            
			if(p.platform.getBdId()!=null){
	            sb.append("ro.build.id="+p.platform.getBdId());
	            sb.append("\n");
	        }
	        if(p.platform.getBdDisplay()!=null){
	            sb.append("ro.build.display.id="+p.platform.getBdDisplay());
	            sb.append("\n");
	        }
	        if(p.platform.getBdProduct() != null){
	            sb.append("ro.product.name="+p.platform.getBdProduct());
	            sb.append("\n");
	        }
	        if(p.platform.getBdDevice()!=null){
	            sb.append("ro.product.device="+p.platform.getBdDevice());
	            sb.append("\n");
	        }
	        if(p.platform.getBdBoard()!=null){
	            sb.append("ro.product.board="+p.platform.getBdBoard());
	            sb.append("\n");
	        }
	        if(p.platform.getBdManufacture()!=null){
	            sb.append("ro.product.manufacturer="+p.platform.getBdManufacture());
	            sb.append("\n");
	        }
	        if(p.platform.getBdBrand()!=null){
	            sb.append("ro.product.brand="+p.platform.getBdBrand());
	            sb.append("\n");
	        }
	        if(p.platform.getBdModel() != null){
	            sb.append("ro.product.model="+p.platform.getBdModel());
	            sb.append("\n");
	        }
	        if(p.platform.getBdBootloader()!=null){
	            sb.append("ro.bootloader="+p.platform.getBdBootloader());
	            sb.append("\n");
	        }
	        if(p.platform.getBdHardware() != null){
	            sb.append("ro.hardware="+p.platform.getBdHardware());
	            sb.append("\n");
	        }
	        if(p.platform.getBdVerInc()!=null){
	            sb.append("ro.build.version.incremental="+p.platform.getBdVerInc());
	            sb.append("\n");
	        }
	        if(p.platform.getBdVerRel()!=null){
	            sb.append("ro.build.version.release="+p.platform.getBdVerRel());
	            sb.append("\n");
	        }
	        
	        if(p.platform.getBdVerSdkInt()!=null){
	            sb.append("ro.build.version.sdk="+p.platform.getBdVerSdkInt());
	            sb.append("\n");
	        }
	        
	        if(p.platform.getBdVerCode()!=null){
	            sb.append("ro.build.version.codename="+p.platform.getBdVerCode());
	            sb.append("\n");
	        }
	        /*
	        if(p.platform.getBdType()!=null){
	            sb.append("ro.build.type="+p.platform.getBdType());
	            sb.append("\n");
	        }
	        */
	        if(p.platform.getBdTags()!=null){
	            sb.append("ro.build.tags="+p.platform.getBdTags());
	            sb.append("\n");
	        }
	        if(p.platform.getBdFingerprint()!=null){
	            sb.append("ro.build.fingerprint="+p.platform.getBdFingerprint());
	            sb.append("\n");
	        }
	        
	        if(p.platform.getBdUser()!=null){
	            sb.append("ro.build.user="+p.platform.getBdUser());
	            sb.append("\n");
	        }
	        
	        if(p.platform.getBdHost()!=null){
	            sb.append("ro.build.host="+p.platform.getBdHost());
	            sb.append("\n");
	        }
	        
	        if(p.platform.getBdTime()!=null){
	        	sb.append("ro.build.date.utc="+p.platform.getBdTime());
	            sb.append("\n");
	        }
	        
	        if(p.device.getSerial() != null){
	        	sb.append("ro.serialno="+p.device.getSerial());
	            sb.append("\n");
	        }
	        if(p.device.getAid() != null){
	        	sb.append("ty.aid="+p.device.getAid());
	            sb.append("\n");
	        }
	        if(p.device.getImei() != null){
	        	sb.append("ty.imei="+p.device.getImei());
	            sb.append("\n");
	        }
	        if(p.device.getWmac() != null){
	        	sb.append("ty.wifi_mac="+p.device.getWmac());
	            sb.append("\n");
	        }
	        if(p.device.getBmac() != null){
	        	sb.append("ty.bt_mac="+p.device.getBmac());
	            sb.append("\n");
	        }
	        if(p.device.getEssid() != null){
	        	sb.append("ty.ssid="+p.device.getEssid());
	            sb.append("\n");
	        }
	        if(p.device.getBssid() != null){
	        	sb.append("ty.bssid="+p.device.getBssid());
	            sb.append("\n");
	        }
	        if(p.device.getBssid() != null){
	        	sb.append("ty.bssid="+p.device.getBssid());
	            sb.append("\n");
	        }
	        if(p.device.getIpv4() != null){
	        	sb.append("ty.ipv4="+(int)NetworkUtil.ip2int(p.device.getIpv4()));
	            sb.append("\n");
	        }
	        if(p.device.getIpv6() != null){
	        	sb.append("ty.ipv6="+p.device.getIpv6());
	            sb.append("\n");
	        }
	        Logger.e(TAG, sb.toString());
	        
	        saved = false;
	        for(int i=0;i<3;i++){
	        	saved = FileUtil.saveTxtFile("/data/app-lib/prop.txt", sb.toString());
	        	if(saved){
	        		break;
	        	}else if(sleep != null){
	        		if(!sleep.sleep(1000)) break;
	        	}
	        }
	        if(!saved){
	        	Log.e(TAG, "updateEmuData failed: save prop file failed.");
	        	return false;
	        }
	        RootShellCmd.getInstance().setFilePermission("/data/app-lib/prop.txt", "777");
	        
		}
        saved = false;
        for(int i=0;i<3;i++){
        	saved = FileUtil.saveTxtFile("/data/app-lib/emulate.txt", on?"1":"0");
        	if(saved){
        		break;
        	}else if(sleep != null){
        		if(!sleep.sleep(1000)) break;
        	}
        }
        if(!saved){
        	Log.e(TAG, "updateEmuData failed: save emulate file failed.");
        	return false;
        }
        RootShellCmd.getInstance().setFilePermission("/data/app-lib/emulate.txt", "777");

		Build.reset();
        return true;
	}

	protected boolean getNextSimCard(){
		int fail_count = 0;		
		postInfo("getNextSimCard: sim="+mSimCardId);
		while(isRunning && fail_count < SIM_CARD_CONT_FAIL_MAX) {
			String imsi = switchSimCard(mContext, 0, mSimCardId, mImsi);
			if(imsi != null) {
				mImsi = imsi;
				postInfo("getNextSimCard success: sim="+mSimCardId);
				return true; // repeat register for new simcard
			}else {
				fail_count++;
				postInfo("getNextSimCard failed: sim="+mSimCardId+",fail_count="+fail_count);				
			}
		}
		return false;
	}
	
	private String getNewImsi(Context context, String old_imsi){
		String new_imsi = null;
		for(int i=0; i<100; i++){
			new_imsi = NetworkUtil.getImsi(context);
        	if(new_imsi == null || new_imsi.equals(old_imsi)){
    			new_imsi = null;
        		if(!enterSleep(500)) break;
        	}else{
        		break;
        	}
        }
		return new_imsi;
	}
	
	// return new imsi
	protected String switchSimCard(Context context, int group_id, int sim_id, String old_imsi){
		postInfo("switchSimCard: group="+group_id+",sim="+sim_id+", old_imsi="+old_imsi);

		while(isRunning && !btIsConnected()){
			if(btConnect()){
				break;
			}
			postInfo("switchSimCard: restart bt");
			mBluetoothAdapter.disable();
			if(!enterSleep(15*1000)) break;
			mBluetoothAdapter.enable();
			if(!enterSleep(15*1000)) break;
		}
		if(!isRunning || !btIsConnected()){
			postInfo("switchSimCard failed: bt not connected");
			return null;
		}
		long t1 = System.currentTimeMillis();
		boolean bt_cmd = false;
		for(int i=0; i<3; i++) {
			String s= String.format("AT+SWIT%02d-%04d\r\n", group_id+1, sim_id+1);
			postInfo("bt cmd: "+s);
			if(isRunning && !btWriteAndWait(s, "SWITCH OK")){
				postInfo("switchSimCard: bt cmd failed. bad response");
				if(!enterSleep(1000)) break;
			}else {
				if(isRunning) {
					postInfo("switchSimCard: bt cmd success");
					bt_cmd = true;
				}
				break;
			}
		}
		if(!bt_cmd){
		    postInfo("switchSimCard failed: bt_cmd failed"); //sometimes serial cable not stable, and we assume its ok after 5 retries.
		    return null;
		}
		context.sendBroadcast(new Intent("android.intent.action.sim.ACTION_RESET_MODEM"));
		postInfo("switchSimCard: wait sim_reset for 30s");
		if(wait_response("sim_reset", 30*1000)==RESULT_INTERRUPT){
			return null;
		}
		int sim_state = NetworkUtil.getSimState(context);
        if(sim_state != TelephonyManager.SIM_STATE_READY){
        	postInfo("switchSimCard failed: bad sim state "+sim_state);
        	return null;
        }
        String imsi = getNewImsi(context, old_imsi);
        if(imsi == null || imsi.equals(old_imsi)){
        	postInfo("switchSimCard failed: read imsi failed, now="+imsi);
        	return null;
        }
        postInfo("switchSimCard: cost "+(System.currentTimeMillis()-t1)+" ms");
        //mSwitchCardTotalDuration += t4-t1;
        //mSwitchCardCount++;
        
        return imsi;
	}
	
	protected boolean turnOnVPN(boolean test, boolean loop_infinite, String vpn_city) {
		long loop_count = 0; 
		long loop_max = loop_infinite?Integer.MAX_VALUE:3;
		while(isRunning && loop_count < loop_max) {
			loop_count++;
			long t1 = System.currentTimeMillis();
			turnOffVPN();
			//postVpn("preparing vpn");
			if(!NetworkUtil.isNetworkConnected(mContext)){
				Logger.e(TAG, "turnOnVPN: isNetworkConnected failed.");
				enterSleep(15*1000);
				continue;
			}
			if(!ServerUtil.testServerConnect()){
				Logger.e(TAG, "turnOnVPN: testServerConnect failed.");
				enterSleep(15*1000);
				continue;
			}
			if(!mSpecifyVPN) {
				mVpnProfile = allocVpn(vpn_city);
				if(mVpnProfile == null){
					Logger.e(TAG, "turnOnVPN: allocVpn failed.");
					break;
				}
			}
			Logger.e(TAG, "turnOnVPN: found vpn: "+mVpnProfile.account.getServer()+", "+mVpnProfile.account.getUsername());
			
			int failed_status = 0;
			if(PlatformUtil.switchVPN(mContext, true, mVpnProfile, sleepObject)) {
				long t2 = System.currentTimeMillis();
				postInfo("turnOnVPN: switchVPN success. cost "+(t2-t1)+" ms");
				postVpn(mVpnProfile.account.getServer()+" "+mVpnProfile.account.getUsername());
				if(mReportIp) {
					for(int i=0; i<10; i++){
						if(!enterSleep(2000)) break;
						String ip = ServerUtil.getMyIp();
						if(ip != null && !ip.equals(mMyIp)){
							postInfo("turnOnVPN: myIp="+ip+", rawIp="+mMyIp);
							return true;
						}
						postInfo("turnOnVPN: getMyIp failed. "+ip+", rawIp="+mMyIp);
						
					}
					failed_status = EnumType.VPN_STATUS_FAILED_HOME_UNREACHED;
				}else{
					return true;
				}
			}else{
				failed_status = EnumType.VPN_STATUS_FAILED_CONNECTION;
				postInfo("turnOnVPN: switchVPN failed. cost "+( System.currentTimeMillis()-t1)+" ms");
			}
			if(isRunning&&!mSpecifyVPN && failed_status > 0) {
				//mark the vpn na
				reprotVpnFailed(failed_status);
			}
		}
		return false;
	}
	
	protected boolean turnOffVPN() {
		if(!PlatformUtil.switchVPN(mContext, false, null, sleepObject)) {
			postVpn("turnOffVPN failed.");
			return false;
		}else{
			postVpn("turnOffVPN success");
			return true;
		}
	}
	
	protected boolean reprotVpnFailed(int status){
		try  
        {
			OkHttpClient client = new OkHttpClient();  
			FormBody.Builder builder = new FormBody.Builder();
			builder.add("phone_seq", String.valueOf(mPhoneSeq));
			Logger.e(TAG, "reprotVpnFailed: phone_seq=" + mPhoneSeq+", status="+status);
			
			RequestBody formBody = builder.build();

			Request request = new Request.Builder()  
		            .url(Constant.getURL_REPORT_VPN_FAILED())  
		            .post(formBody)
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "reprotVpnFailed: response erroNo=" + errorNo);
            
            return (errorNo!=null && errorNo.equals("0"));
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
        	postInfo("reprotVpnFailed Exception: "+e);
        }  
		return false;
	}
	
	protected boolean freeVpn(){
		try  
        {
			OkHttpClient client = new OkHttpClient();  
			FormBody.Builder builder = new FormBody.Builder();
			builder.add("phone_seq", String.valueOf(mPhoneSeq));
			Logger.e(TAG, "freeVpn: phone_seq=" + mPhoneSeq);
			
			RequestBody formBody = builder.build();

			Request request = new Request.Builder()  
		            .url(Constant.getURL_FREE_VPN())  
		            .post(formBody)
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "freeVpn: response erroNo=" + errorNo);
            
            return (errorNo!=null && errorNo.equals("0"));
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
        	postInfo("freeVpn Exception: "+e);
        }  
		return false;
	}
	
	
	protected VpnProfile allocVpn(String vpn_city){
		try  
        {
			OkHttpClient client = new OkHttpClient();  
			//MediaType JSON = MediaType.parse("application/json; charset=utf-8");
			//RequestBody requestBody = RequestBody.create(JSON, json);
			//File file = new File("fileDir", "test.jpg");
		    //RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
			FormBody.Builder builder = new FormBody.Builder();
			builder.add("phone_seq", String.valueOf(mPhoneSeq));
			if(vpn_city != null){
				builder.add("city", vpn_city);
			}
			Logger.e(TAG, "allocVpn: phone_seq=" + mPhoneSeq+", city="+vpn_city);
			
			RequestBody formBody = builder.build();

			Request request = new Request.Builder()  
		            .url(Constant.getURL_ALLOC_VPN())  
		            .post(formBody)
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
		    postInfo("allocVpn: response erroNo=" + errorNo);
            
            if(errorNo!=null && errorNo.equals("0")){
            	JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					String vpnAccountStr = dataObj.optString("vpn_account");
					if(vpnAccountStr != null){
						VpnAccount account = JSON.parseObject(vpnAccountStr, VpnAccount.class);
						if(account != null){
							VpnProfile p = new VpnProfile();
							p.account = account;
							p.specified = false;
							return p;
						}
					}
				}
            }
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
        	postInfo("allocVpn Exception: "+e);
        }  
		return null;
	}
	
	
	protected SimSlot sim_slot_get_status(int slot_seq, int sim_seq) {
		try  
        {
			OkHttpClient client = new OkHttpClient(); 
			FormBody.Builder builder = new FormBody.Builder()
				    .add("slot_seq", Integer.toString(slot_seq))
				    .add("sim_seq", Integer.toString(sim_seq));
			Request request = new Request.Builder()  
		            .url(Constant.getURL_GET_SIM_SLOT())  
		            .post(builder.build())
		            .build();  
			postInfo("sim_slot_get_status: slot_seq="+slot_seq+",sim_seq="+sim_seq);
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "sim_slot_get_status: response erroNo=" + errorNo);
            if(errorNo!=null && errorNo.equals("0")){
            	JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					String slotStr = dataObj.optString("sim_slot");
					if(slotStr != null){
						SimSlot ss = JSON.parseObject(slotStr, SimSlot.class);
						if(ss != null){
							postInfo("sim_slot_get_status: sim_status=" + ss.getSimStatus()+", slot_status="+ss.getSlotStatus());
							return ss;
						}
					}
				}
            }  
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
        	postInfo("sim_slot_get_status Exception: "+e);
        }  
		return null;
	}
	
	protected boolean update_sim_recent_status(String sim_imsi, int sim_status) {
		if(StringUtil.isEmpty(sim_imsi)) return false;
		try  
        {
			OkHttpClient client = new OkHttpClient(); 
			FormBody.Builder builder = new FormBody.Builder()
				    .add("sim_imsi", sim_imsi)
				    .add("sim_status", Integer.toString(sim_status));
			Request request = new Request.Builder()  
		            .url(Constant.getURL_UPDATE_SIM_RECENT_STATUS())  
		            .post(builder.build())
		            .build();  
			postInfo("update_sim_recent_status: sim_imsi="+sim_imsi+",sim_status="+sim_status);
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "update_sim_recent_status: response erroNo=" + errorNo);
            return (errorNo!=null && errorNo.equals("0"));
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
        	postInfo("update_sim_recent_status Exception: "+e);
        }  
		return false;
	}
	
	protected boolean btIsConnected() {
		return mBtThread!=null&&mBtThread.isConnected();
	}
	
	protected boolean btConnect() {
		if(mBluetoothDevice == null){
			postInfo("btConnect failed. bt device is null");
			return false;
		}
		if(mBtThread != null){
			mBtThread.cancel();
			mBtThread= null;
		}
		if(mHandler.get()==null){
			postInfo("btConnect failed. mHandler.get() is null");
			return false;
		}
		try {
			mBtThread = new BtThread(mHandler.get(), mBluetoothDevice);
			mBtThread.start();
			return mBtThread.wait4connect(3000);
        }catch(Exception e){
        	postInfo("btConnect: Exception "+e);
        }
		return false;
		
	}
	protected boolean btDisconnect() {
		if(mBtThread != null){
			mBtThread.cancel();
			mBtThread= null;
		}
		return true;
	}
	
	protected boolean btWriteAndWait(String cmd, String resp){
		if(mBtThread != null){
			return mBtThread.write_and_wait(cmd, resp);
		}
		return false;
	}
	

	public boolean enterSleep(long ms) {
		if(sleepObject != null) return sleepObject.sleep(ms);
		return false;
	}

	public void wakeSleep() {
		if(sleepObject != null) sleepObject.wake();
	}
	
}
