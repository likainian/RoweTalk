package com.example.rowetalk.thread;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.example.rowetalk.bean.Apk;
import com.example.rowetalk.bean.EnumType;
import com.example.rowetalk.bean.SimSlot;
import com.example.rowetalk.bean.Subtask;
import com.example.rowetalk.bean.Task;
import com.example.rowetalk.service.MainService;
import com.example.rowetalk.util.Config;
import com.example.rowetalk.util.Constant;
import com.example.rowetalk.util.DateUtil;
import com.example.rowetalk.util.FileUtil;
import com.example.rowetalk.util.HttpUtil;
import com.example.rowetalk.util.HttpUtil.ProgressHandler;
import com.example.rowetalk.util.Logger;
import com.example.rowetalk.util.NetworkUtil;
import com.example.rowetalk.util.PlatformUtil;
import com.example.rowetalk.util.RootApkUtil;
import com.example.rowetalk.util.RootShellCmd;
import com.example.rowetalk.util.SleepObject;
import com.example.rowetalk.util.StringUtil;
import com.example.rowetalk.util.SystemUtil;
import com.example.rowetalk.util.ZipUtils;
import com.liblua.LuaEnv;
import com.luajava.LuaState;

import android.os.Environment;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WorkThread extends BaseThread {
	private static final String TAG = WorkThread.class.getName();
	
	private Task mTask;
	private Apk mApk;
	private String mApkLocalPath;
	//private String mShotDataLocalPath;
	//private String mShotLog;
	private int mSimTaskCount;
	private int mNoSimTaskCount;
	private Subtask mSubtask;
	//private Subtask mLastask;
    
    private int mSlotSeq;
    private int mSimSeq;
    private int[] mSimStatusArray; // -1: uninit, 0: false, 1: ok
    private boolean[] mSimRecentExecTask; // remember if recent execed task for each sim
    private long mScriptTime;
    
    private SimSlot mSimSlot;
    private String mScriptFailedMsg;
    private int mScriptResultP1;
    private int mScriptResultP2;
    private String mScriptResultP3;
    private long idleCheckTime = System.currentTimeMillis();
    private Map<String, Integer> taskCountMap = new HashMap<String, Integer>();
    private int mSimSuccessCount = 0; // for clear sms messages
    
    
    private static final int TASK_FAILED_BAD_SCRIPT = -1;
	private static final int TASK_FAILED_REGISTER_DEVICE = -2;
	private static final int TASK_FAILED_DOWNLOAD_SCRIPT = -3;
	private static final int TASK_FAILED_DOWNLOAD_APK = -4;
	private static final int TASK_FAILED_INSTALL_APK = -5;
	private static final int TASK_FAILED_DOWNLOAD_APK_DATA = -6;
	private static final int TASK_FAILED_DECODE_APK_DATA = -7;
	private static final int TASK_FAILED_CONNECT_MIFI = -8;
	private static final int TASK_FAILED_SWITCH_WIFI = -9;
	private static final int TASK_FAILED_SWITCH_4G = -10;
	private static final int TASK_FAILED_TURNON_VPN = -11;
	private static final int TASK_FAILED_TURNOFF_VPN = -12;
	private static final int TASK_FAILED_SEND_IP = -13;
	private static final int TASK_FAILED_ENCODE_APK_DATA = -14;
	private static final int TASK_FAILED_EXEC_SCRIPT = -15;
	private static final int TASK_FAILED_DOWNLOAD_EXTRA = -16;
	private static final int TASK_FAILED_UNZIP_EXTRA = -17;
	private static final int TASK_FAILED_INTERRUPT = -18;
	private static final int TASK_FAILED_UPLOAD_APK_DATA = -19;
	private static final int TASK_FAILED_CLEAR_APK = -20;
	
	private FTPClient mFTPClient = new FTPClient();
	
	
	public WorkThread(MainService context, Handler handler, SleepObject object, Map<String, Object> map) {
		super(context, handler, object, map);
	}
	
	

	@Override
	protected boolean init() {
		// TODO Auto-generated method stub
		if(!super.init())
			return false;
		Logger.e(TAG, "init");
		if(!mNoSimPool) {
			int count = mSimRangeEnd-mSimRangeStart;
			mSimStatusArray = new int[count];
			mSimRecentExecTask = new boolean[count];
			for(int i=0; i<count; i++){
				mSimStatusArray[i] = -1;
				mSimRecentExecTask[i] = true;
			}
		}
		
		
		updateEmuData(false, null, sleepObject);
		
		if(!RootShellCmd.getInstance().setImeTy()){
			postInfo("setImeTy failed.");
			return false;
		}
		
		try {
			mFTPClient.connect(Constant.FTP_SERVER, 21); 
			mFTPClient.login(Constant.FTP_USER, Constant.FTP_PASSWORD); 
		}catch(Exception e){
			e.printStackTrace();
			Logger.e(TAG, "mFTPClient exception: "+e);
			return false;
		}
		
		return true;
	}



	@Override
	protected void deinit() {
		Logger.e(TAG, "deinit");
		// TODO Auto-generated method stub
		super.deinit();
	}


	@Override
	public void run() {
		isRunning = true;
		postStartedMsg();
		if(!init()){
			isRunning = false;
			deinit();
            postEndedMsg();
			return;
		}

		//shell.setProp("ty.emulate", "false");
		if(!registerPhoneStatus(EnumType.PHONE_STATUS_RUNNING, null)){
			postInfo("registerPhone failed.");
			isRunning = false;
			deinit();
			postEndedMsg();
			return;
		}
		
		while(isRunning){
			do_nosim_jobs();
			if(!isRunning) break;
			if(!mNoSimPool && mSimTaskCount > 0){
				do_sim_jobs();
			}else{
				do_idle_jobs();
				postInfo("sleep 30 secs.");
				enterSleep(30*1000); // sleep 30 seconds
			}
		}
		
		if(!mNoSimPool) {
			postFalseTasks(failedTasks);
			postFalseSlots(failedSlots);
		}
		isRunning = false;
		
		deinit();
		postEndedMsg();
	}

	
	@Override
	public void cancel(boolean keep_app_state) {
		Logger.e(TAG, "cancel: "+keep_app_state);
		// TODO Auto-generated method stub
		super.cancel(keep_app_state);
	}
	
	private void do_nosim_jobs(){
		// first query no sim task
		int firstsimId = -1;
		postInfo("do_nosim_jobs: "+firstsimId);
		while(isRunning) {
			mSimTaskCount = 0;
			mNoSimTaskCount = 0;
			mTask = null;
			mSubtask = null;
			if(mNoSimVdeviceId>= mNoSimVdevieNum){
				mNoSimVdeviceId = 0;
			}
			if(firstsimId==-1){
				firstsimId = mNoSimVdeviceId;
			}else if(firstsimId == mNoSimVdeviceId){
				postInfo("nosim one loop finished");
				break; // 检查一轮了，没有可做的任务
			}
			
			while(isRunning && !allocTask(true)){
				postInfo("allocTask for nosim failed.");
				enterSleep(3*1000);
			}
			if(!isRunning) break;
			if(mTask==null || mSubtask == null){
				if(mNoSimTaskCount == 0){
					break; // 没有多余任务
				}
				mNoSimVdeviceId++;
				// 继续为下一个虚拟设备领取任务
				continue;
			}
			// 执行领取的任务
			long t1 = System.currentTimeMillis();
			mScriptTime = 0;
			int err = execTask(true);
			if(!isRunning) return;
			String errMsg = getErr(err);
    		if(err == TASK_FAILED_EXEC_SCRIPT){
    			errMsg = mScriptFailedMsg;
    		}
    		long cost_time = System.currentTimeMillis()-t1;
    		while(isRunning && !reportTask((int)cost_time, (int)mScriptTime,
    				err==0?EnumType.SUBTASK_STATUS_FINISHED:EnumType.SUBTASK_STATUS_FAILED, errMsg)){
    			postInfo("reportTask for nosim failed.");
				enterSleep(3*1000);
			}
    		// show success task count
    		if(isRunning && err == 0){
    			String key = mTask.getName()+mTask.geteId();
    			if(taskCountMap.containsKey(key)){
    				taskCountMap.put(key, taskCountMap.get(key)+1);
    			}else{
    				taskCountMap.put(key, 1);
    			}
    			String s="";
    			for(Map.Entry<String, Integer> entry:taskCountMap.entrySet()){    
    			    s += String.format("(%s,%d),", entry.getKey().substring(0, 4), entry.getValue());
    			}   
    			postSuccessTasks(s);
    			Config.putInt("nosim_start", mNoSimVdeviceId);
    		}
    		mNoSimVdeviceId++;
		}
	}
	
	private void do_sim_jobs(){
		for(int i=0; i<mSimRecentExecTask.length; i++){
			mSimRecentExecTask[i] = true;
		}
		while(isRunning && findValidSim()) {
			//switch sim card
			boolean no_sim = true;
			mSimRecentExecTask[mSimCardId-mSimRangeStart] = false;
			// 所有的 SIM 卡最近都没有执行过任务
			for(int i=0; i<mSimRecentExecTask.length;i++){
				if(mSimRecentExecTask[i]){
					no_sim = false;
					break;
				}
			}
			if(no_sim){
				// 执行了一轮，发现没有可用的
				postInfo("detect no sim tasks.");
				return;
			}
			if(mSimSlot == null){
				// 执行了一轮，发现没有可用的
				postInfo("mSimSlot = null something is wrong");
				return;
			}
			// query task
			mTask = null;
			mSimTaskCount = 0;
			mNoSimTaskCount = 0;
			mApk = null;
			mSubtask = null;
			String oldImsi = mImsi;
			mImsi = mSimSlot.getSimImsi();
			while(isRunning && !allocTask(false)){
				postInfo("allocTask failed.");
				enterSleep(3*1000);
			}
			if(!isRunning) return;
			if(mTask == null || mSubtask == null){
				if(mSimTaskCount == 0) return;
				mSimRecentExecTask[mSimCardId-mSimRangeStart] = false;
				mSimCardId++;  // 切换到下一张卡进行测试
				continue;
			}
			// 准备执行任务
			if(mTask.getSimType()==EnumType.TASK_SIM_TYPE_USE_IMSI) {
				// 只是使用 IMSI，不用切卡
				mSimRecentExecTask[mSimCardId-mSimRangeStart] = true;
	        	postCurrentCard(String.valueOf(mSimCardId+1));
			}else if(mTask.getSimType()==EnumType.TASK_SIM_TYPE_SMS) {
				mImsi = oldImsi;
				// 准备切卡
				postInfo("begin switch card for sim="+mSimCardId);
				if(!getNextSimCard()){
					postInfo("switch card failed for sim="+mSimCardId);
					//标记切卡失败的卡状态，下次就跳过
					//mSimStatusArray[mSimCardId-mSimRangeStart] = 1;
					//mSimRecentExecTask[mSimCardId-mSimRangeStart] = false;
					// notify server to mark this sim na.
					if(isRunning) {
						// 将失败结果发给服务器记录
						//notify_server_to_mark_sim_switch_failed(mSimSlot);
						mSimCardId++;
						postCurrentCard(String.valueOf(mSimCardId));
						String s = mSimCardId+",";
						if(failedSlots.startsWith(s)){
							failedSlots = "";
						}
						if(failedTasks.startsWith(s)){
							failedTasks = "";
						}
						failedTasks += s;
						failedSlots += s;
						postFalseSlots(failedSlots);
						postFalseTasks(failedTasks);
						// 向服务器归还任务，因为SIM卡不可用
						/* 暂时不报告，因为有连线导致的问题很多
						while(isRunning&&!reportTask(0, 0, EnumType.SUBTASK_STATUS_SIMCARD_NA, "switch card failed.")){
							postInfo("reportTask2 for sim failed.");
							enterSleep(3*1000);
						}*/
					}
					continue;
				}else {
					postInfo("switch card success for sim="+mSimCardId);
					mSimRecentExecTask[mSimCardId-mSimRangeStart] = true;
		        	postCurrentCard(String.valueOf(mSimCardId+1));
		        	update_sim_recent_status(mImsi, 0);
				}
			}
			
			// 开始执行任务
			Config.putInt("sim_start", mSimCardId);
			long t1 = System.currentTimeMillis();
			mScriptTime = 0;
			int err = execTask(false);
			if(!isRunning) return;
    		String errMsg = getErr(err);
    		if(err == TASK_FAILED_EXEC_SCRIPT){
    			errMsg = mScriptFailedMsg;
    		}
    		long cost_time = System.currentTimeMillis()-t1;
    		while(isRunning && !reportTask((int)cost_time, (int)mScriptTime,
    				err==0?EnumType.SUBTASK_STATUS_FINISHED:EnumType.SUBTASK_STATUS_FAILED, errMsg)){
    			postInfo("reportTask for sim failed.");
				enterSleep(3*1000);
			}
    		// show success task count
    		if(err == 0){
    			String key = mTask.getName()+mTask.geteId();
    			if(taskCountMap.containsKey(key)){
    				taskCountMap.put(key, taskCountMap.get(key)+1);
    			}else{
    				taskCountMap.put(key, 1);
    			}
    			String s="";
    			for(Map.Entry<String, Integer> entry:taskCountMap.entrySet()){    
    			    s += String.format("(%s,%d),", entry.getKey().substring(0, 4), entry.getValue());
    			}   
    			postSuccessTasks(s);
    		}
			mSimCardId++;
			mSimSuccessCount++;
			if(mSimSuccessCount%200==0){
				clearRecentMessages();
				clearRecentCalls();
			}
		}
	}
	
	private void do_idle_jobs(){
		// clear sms and calllog records6
		if(System.currentTimeMillis()-idleCheckTime>3*60*60*1000) { // afater 3 hours
			clearFinishedTaskFiles();
			clearInstalledApps();
			idleCheckTime = System.currentTimeMillis();
		}
	}
	
	// 查找下一张可用的SIM卡，切卡失败的标记状态为 1
	private boolean findValidSim() {
		mSimSlot = null;
		int count = 0;
		while(isRunning) {
			if(mSimCardId >= mSimRangeEnd){
				mSimCardId = mSimRangeStart;
			}
			if(mSimStatusArray[mSimCardId-mSimRangeStart]>0) {
				// 已经标记的sim 卡 不可用
				mSimRecentExecTask[mSimCardId-mSimRangeStart] = false;
				mSimCardId++;
			}else {
				// 向服务器查询此卡是否可用
        		mSlotSeq = mSlotSeqList.get((int)((mSimCardId)/16));
        		if(mSlotSeq==0){
        			postInfo("skip sim "+mSimCardId+" because slot not specified");
        			mSimCardId++;
        			continue;
        		}
				mSimSeq = ((mSimCardId) % 16)+1;
				SimSlot ss = sim_slot_get_status(mSlotSeq, mSimSeq);
				if(ss == null) {
					if(RootShellCmd.getInstance().exec("ping -c 3 -w 150 qq.com")) {
						postInfo("sim_slot_get_status failed, assume it's NA.");
						// 服务器上已经标记了此卡不可用
						mSimStatusArray[mSimCardId-mSimRangeStart] = 1; 
						mSimRecentExecTask[mSimCardId-mSimRangeStart] = false;
						mSimCardId++;
					}else {
						postInfo("sim_slot_get_status failed, wait 10s to retry");
						enterSleep(10*1000);
					}
				}else if(ss.getSimStatus()>0 || ss.getSlotStatus()>0){
					// 服务器上已经标记了此卡不可用
					mSimStatusArray[mSimCardId-mSimRangeStart] = 1; 
					mSimRecentExecTask[mSimCardId-mSimRangeStart] = false;
					mSimCardId++;
				}else {
					// 可用卡
					mSimStatusArray[mSimCardId-mSimRangeStart] = 0;
					mSimSlot = ss;
					postInfo("found sim "+mSimCardId+" slot_seq="+ss.getSlotSeq()+", sim_seq="+ss.getSimSeq()+", imsi="+ss.getSimImsi());
					break;
				}
				
			}
			count = getValidSimCount();
			if(count == 0){
				break;
			}
		}
		count = getValidSimCount();

		postInfo("findValidSim count="+count+",curr="+mSimCardId);
		return count != 0;
	}
	
	private boolean allocTask(boolean no_sim) {
		if(mEnableMifi || mEnable4G || mEnableVPN){
			if(!mWifiManager.getConnectionInfo().getRawBSSID().equals(mWifiAddr)) {
				if(!NetworkUtil.wifiConnectTo(mContext, mWifiName, mWifiAddr, sleepObject)) {
					postInfo ("wifi not connected to "+mWifiName+","+mWifiAddr);
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
		if(!no_sim && StringUtil.isEmpty(mImsi)){
			postInfo("mImsi is empty");
			return false;
		}
		try  
        {
			OkHttpClient client = new OkHttpClient();  
			//MediaType JSON = MediaType.parse("application/json; charset=utf-8");
			//RequestBody requestBody = RequestBody.create(JSON, json);
			FormBody.Builder builder = new FormBody.Builder()
					    .add("phone_id", mPhoneId)
					    .add("has_sim", no_sim?"0":"1");
			if(!no_sim){
				builder.add("sim_imsi", mImsi);
			}else{
				builder.add("nosim_key", String.format("%d-%06d", mPhoneSeq,mNoSimVdeviceId+1));
			}
			
			postInfo("allocTask: phone_seq="+mPhoneSeq+",has_sim="+(!no_sim)+ (no_sim?(",nosim_dev="+mNoSimVdeviceId):(",sim_imsi="+mImsi)));

			Request request = new Request.Builder()  
		            .url(Constant.getURL_TASK_ALLOC())  
		            .post(builder.build())
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    //Logger.e(TAG, "body="+response.body().string());
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "allocTask: response erroNo=" + errorNo);
            if (errorNo.equals("0")) {
				JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					//mShotDataLocalPath = null;
					mSimTaskCount = dataObj.optInt("sim_task_count");
					mNoSimTaskCount = dataObj.optInt("nosim_task_count");
					String taskStr = dataObj.optString("task");
					
					if(StringUtil.isNotEmpty(taskStr)){
						mTask = JSON.parseObject(taskStr, Task.class);
					}
					postInfo("allocTask: sim_task_count="+mSimTaskCount+", nosim_task_count="+mNoSimTaskCount+", task="+taskStr);
					if(mTask != null) {
						String subtaskStr = dataObj.optString("subtask");
						if(StringUtil.isNotEmpty(subtaskStr)){
							mSubtask = JSON.parseObject(subtaskStr, Subtask.class);
						}
						String apkStr = dataObj.optString("apk");
						if(StringUtil.isNotEmpty(apkStr)){
							mApk = JSON.parseObject(apkStr, Apk.class);
						}
						postInfo("allocTask: subtask="+ subtaskStr);
					}
					return true;
				} 
            }
        }  catch (Exception e)  {  
        	e.printStackTrace();
        	postInfo("allocTask Exception: "+e);
        }  
		return false;
	}
	
	private boolean reportTask(int cost_time, int script_time, int status, String err_msg){		
		try  
        {
			if(cost_time < 0){
				cost_time = 0;
			}else if(cost_time > DateUtil.DAY){
				cost_time = DateUtil.DAY;
			}
			cost_time /= 1000; //to seconds
			if(script_time<0){
				script_time = 0;
			}else if(script_time > DateUtil.DAY){
				script_time = DateUtil.DAY;
			}
			script_time /= 1000; //to seconds
			
			OkHttpClient client = new OkHttpClient();  
			//MediaType JSON = MediaType.parse("application/json; charset=utf-8");
			//RequestBody requestBody = RequestBody.create(JSON, json);
		    MultipartBody.Builder builder = new MultipartBody.Builder()
						.setType(MultipartBody.FORM)
					    .addFormDataPart("subtask_id", mSubtask.geteId())
					    .addFormDataPart("status", Integer.toString(status))
					    .addFormDataPart("message",err_msg==null?"":err_msg)
					    .addFormDataPart("cost_time", Integer.toString(cost_time))
					    .addFormDataPart("script_time", Integer.toString(script_time));
		    if(script_time != 0 && mVpnProfile != null && !StringUtil.isEmptyOrNull(mVpnProfile.account.getCity())){
		    	 postInfo("reportTask: vpn_city="+mVpnProfile.account);
		    	 builder.addFormDataPart("vpn_city", mVpnProfile.account.getCity());
		    }
		    if(status == EnumType.SUBTASK_STATUS_FINISHED){
		    	if(mScriptResultP1>0)
		    		builder.addFormDataPart("p1", Integer.toString(mScriptResultP1));
		    	if(mScriptResultP2>0)
		    		builder.addFormDataPart("p2", Integer.toString(mScriptResultP2));
		    	if(mScriptResultP3!=null&&mScriptResultP3.length()>0)
		    		builder.addFormDataPart("p3", mScriptResultP3);
		    }
		    /*
		    if(mShotDataLocalPath != null){
		    	File file = new File(mShotDataLocalPath);
			    RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
			    builder.addFormDataPart("appData", "1.zip", fileBody);
		    }*/
			postInfo("reportTask: subtask_id="+mSubtask.geteId()+",cost_time="+cost_time+",script_time="+script_time+",err_msg="+err_msg);

			Request request = new Request.Builder()  
		            .url(Constant.getURL_TASK_REPORT())  
		            .post(builder.build())
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "reportTask: response erroNo=" + errorNo);
            return errorNo!=null && errorNo.equals("0");
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
        	postInfo("reportTask Exception: "+e);
        }  
		return false;
	}
	
	
	private int execTask(boolean no_sim) {
		
		if(mTask.getScriptSize()==0){
			return TASK_FAILED_BAD_SCRIPT; //("unkonwn script file");
		}
		if(mTask.getEnvType() == EnumType.TASK_ENV_TYPE_EMULATE){
			if(!registerDevice(no_sim)){
				return TASK_FAILED_REGISTER_DEVICE;//("registerDevice failed");
			}
			boolean b = updateEmuData(true, mEnvProfile, sleepObject);
			postInfo("updateEmuData: "+(b?"success":"failed."));
		}
		// start download script
		String dir = Constant.getTaskStoragePath()+"/"+mTask.geteId();
		File f = new File(dir);
		if(!f.exists()){
			f.mkdirs();
		}
		String filePath = null;
		Logger.e(TAG, "check script");
		// download script
		filePath = dir + "/1.lua";
		f = new File(filePath);
		if(!f.exists() || Config.getInt(mTask.geteId()+"_sv", 0) != mTask.getScriptVersion()){
			String url = Constant.getURL_DOWNLOAD_TASK_SCRIPT();
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("task_id", mTask.geteId());
			
			if(!HttpUtil.downloadFile(url, params, filePath, new ProgressHandler() {
				
				@Override
				public void updateSize(long size) {
					// TODO Auto-generated method stub
					Logger.e(TAG, "downloadScript updateSize "+size);
				}
			})){
				return TASK_FAILED_DOWNLOAD_SCRIPT;//("download script file failed.");
			}
			Config.putInt(mTask.geteId()+"_sv", mTask.getScriptVersion());
		}else {
			Logger.e(TAG, "found script: "+filePath);
		}
		mTask.setScriptPath(filePath);
		
		//check extras files
		if(mTask.getExtrasPath() != null && mTask.getExtrasSize()>0) {
			filePath = dir + "/1.zip";
			f = new File(filePath);
			if(!f.exists() || Config.getInt(mTask.geteId()+"_exv", 0) != mTask.getExtrasVersion()){
				String url = Constant.getURL_DOWNLOAD_TASK_EXTRA();
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("task_id", mTask.geteId());
				
				if(!HttpUtil.downloadFile(url, params, filePath, new ProgressHandler() {
					
					@Override
					public void updateSize(long size) {
						// TODO Auto-generated method stub
						Logger.e(TAG, "downloadExtra updateSize "+size);
					}
				})){
					return TASK_FAILED_DOWNLOAD_EXTRA;//("download script file failed.");
				}
				Config.putInt(mTask.geteId()+"_exv", mTask.getExtrasVersion());
				
			}else {
				Logger.e(TAG, "found extra files: "+filePath);
			}
			// extract extra files
			boolean unzip = false;
			String unzip_dir = Constant.getTaskScriptPath();
			File d = new File(unzip_dir);
			if(d.exists()){
				String task_id = FileUtil.ReadTxtFile(unzip_dir+"/task_id");
				if(task_id == null || !task_id.equals(mTask.geteId())){
					unzip = true;
				}
			}else {
				unzip = true;
			}
			Logger.e(TAG, "unzip extra files: "+unzip);
			if(unzip) {
				FileUtil.deleteFile(unzip_dir, true);
				d.mkdirs();
				if(!ZipUtils.UnzipFile(filePath, unzip_dir)){
					return TASK_FAILED_UNZIP_EXTRA;
				}
				FileUtil.writeTxtFile(unzip_dir+"/task_id", mTask.geteId(), false);
			}
			mTask.setExtrasPath(filePath);
		}
		
		
		// download apk
		if(mApk != null){
			// clear apk dirs
			String dirs = mTask.getApkDirs();
			if(dirs != null){
				String[] arr = dirs.split(",");
				if(arr != null && arr.length > 0){
					for(String a: arr){
						a = a.trim();
						if(!StringUtil.isEmptyOrNull(a)){
							FileUtil.deleteFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+a, true);						
						}
					}
				}
			}
			// download apk
			filePath = dir+"/1.apk";
			f = new File(filePath);
			if(!f.exists() || mApk.getFileSize() != f.length()){
				// download apk file
				postInfo("start download apk: "+mApk.getName());
				String url = Constant.getURL_DOWNLOAD_TASK_APK();
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("apk_id", mApk.geteId());
				
				if(!HttpUtil.downloadFile(url, params, filePath, new ProgressHandler() {
					
					@Override
					public void updateSize(long size) {
						// TODO Auto-generated method stub
						Logger.e(TAG, "downloadApk updateSize "+size);
					}
				})){
					return TASK_FAILED_DOWNLOAD_APK;//"download apk file failed.";
				}
			}
			mApkLocalPath=filePath;
			postInfo("start install apk: "+mApk.getName());
			// install apk
			if(!installApk()){
				return TASK_FAILED_INSTALL_APK;//"install apk failed. "+mApkLocalPath;
			}
			
			if(!RootShellCmd.getInstance().clearApp(mApk.getPackageName())){
				return TASK_FAILED_CLEAR_APK;
			}
			//start recover apk data
			if(mTask.getApkDataType()== EnumType.TASK_APK_DATA_TYPE_SAVE_TO_SERVER 
					&& mSubtask.getStatus() != EnumType.SUBTASK_STATUS_ALLOCATED){
				//download last shot data
				dir = Constant.getTempStoragePath();
				f = new File(dir);
				if(!f.exists()){
					f.mkdirs();
				}
				filePath = dir +"/" + mSubtask.geteId()+".zip";
				f = new File(filePath);
				if(!f.exists() || mSubtask.getDataSize() != f.length()){
					try {
						f.createNewFile();
						String ftp_path = String.format("/task/%s/%c/%c/%c/", mTask.geteId(),
								mSubtask.geteId().charAt(0),mSubtask.geteId().charAt(1),mSubtask.geteId().charAt(2));
						mFTPClient.changeDirectory(ftp_path);
						FTPFile[] list = mFTPClient.list(mSubtask.geteId()+".zip");
						if(list != null && list.length==1){
							postInfo("Found apk data.");
							mFTPClient.download(list[0].getName(), f);
							postInfo("apk data downloaded.");
						}
					}catch(Exception e){
						e.printStackTrace();
						return TASK_FAILED_DOWNLOAD_APK_DATA;
					}
				}
				// download success, unzip shot data and recovery 
				if(f.exists()){
					String retStr = unzip_and_restore_shot(filePath);
					if(retStr != null){
						return TASK_FAILED_DECODE_APK_DATA;
					}
					f.delete();
				}
			}
		}
		
		
		// prepare exec environment
		if(mEnableMifi){
			if(!NetworkUtil.wifiConnectTo(mContext, mMifiName, mMifiAddr, sleepObject)) {
				Logger.e(TAG,"wifi not connected to "+mWifiName+","+mWifiAddr);
				return TASK_FAILED_CONNECT_MIFI;//("wifiConnectTo mifi failed.");
			}else{
				postInfo("wifiConnectTo mifi success.");
			}
		}
		if(mEnable4G && !no_sim){
			if(!NetworkUtil.switchWiFi(mContext, false, sleepObject)){
				return TASK_FAILED_SWITCH_WIFI;//("switchWiFi off failed.");
			}
			
			if(!PlatformUtil.switch4G(mContext, true, sleepObject)) {
				return TASK_FAILED_SWITCH_4G;//("switch4G on failed.");
			}else{
				postInfo("switch4G on success.");
			}
		}
		if(mEnableVPN){
			if(!turnOnVPN(false, true, mSubtask.getVpnCity())){
				return TASK_FAILED_TURNON_VPN;//("turnOnVPN failed.");
			}else{
				postInfo("turnOnVPN success.");
			}
		}
		// report ip
		if(mEnableMifi || mEnable4G || mEnableVPN){
			if(!enterSleep(2000)) return TASK_FAILED_INTERRUPT;
		}
		boolean network_ok = false;
		if(mReportIp){
			network_ok = true;
		}else{
			for(int i=0; i<5; i++) {
				if(isRunning && send_ip()){
					postInfo("send_ip success");
					network_ok = true;
					break;
				}
				Logger.e(TAG, "send_ip failed.");
				if(!enterSleep(2000)) break;
			}
		}
		//String err =  null;
		int err = 0;
		if(!network_ok){
			err = TASK_FAILED_SEND_IP;//"network unreached";
		}else if(isRunning) {
			// execute script
			long t1 = System.currentTimeMillis();
			err = execScript(no_sim);
			Logger.e(TAG, "execScript: "+err);
			mScriptTime = System.currentTimeMillis() - t1;
		}

		if(!isRunning){ return TASK_FAILED_INTERRUPT; }
		
		if(mEnableMifi){
			if(!NetworkUtil.wifiConnectTo(mContext, mWifiName, mWifiAddr, sleepObject)) {
				postInfo("wifiConnectTo wifi failed.");
			}else{
				postInfo("wifiConnectTo wifi success.");
			}
		}
		if(mEnable4G && !no_sim){
			if(!PlatformUtil.switch4G(mContext, false, sleepObject)) {
				postInfo("switch4G false failed.");
			}else{
				postInfo("switch4G false success.");
			}
			if(!NetworkUtil.switchWiFi(mContext, true, sleepObject)){
				postInfo("switchWiFi on failed.");
			}
			if(!NetworkUtil.wifiConnectTo(mContext, mWifiName, mWifiAddr, sleepObject)) {
				postInfo("wifiConnectTo wifi failed.");
			}else{
				postInfo("wifiConnectTo wifi success.");
			}
		}
		if(mEnableVPN && isRunning){
			if(!turnOffVPN()) {
				postInfo("turnOffVPN failed.");
			}
		}
		
		if(mApk != null){
			if(mTask.getApkDataType() == EnumType.TASK_APK_DATA_TYPE_SAVE_TO_SERVER) {
				//backup current environment
				String dirPath = Constant.getTempStoragePath()+"/"+mSubtask.geteId();
				int count = backup_and_zip_shot(dirPath, mTask.getApkDirs());
				if(count < 0){
					postInfo("backup_and_zip_shot failed");
					return TASK_FAILED_ENCODE_APK_DATA;
				}else if(count>0) {
					filePath = dirPath + ".zip";
					f = new File(filePath);
					if(!f.exists()){
						return TASK_FAILED_ENCODE_APK_DATA;//"zip file not found. "+filePath;
					}
					try {
						String ftp_path = String.format("/task/%s/%c/%c/%c/", mTask.geteId(),
								mSubtask.geteId().charAt(0),mSubtask.geteId().charAt(1),mSubtask.geteId().charAt(2));
						//mFTPClient.createDirectory(ftp_path);
						if(ftp_mkdirs(mFTPClient, ftp_path)){
							mFTPClient.changeDirectory(ftp_path);
							postInfo("start upload apk data.");
							mFTPClient.upload(f);
							postInfo("finished upload apk data.");
						}else {
							postInfo("ftp_mkdirs failed.");
							return TASK_FAILED_UPLOAD_APK_DATA;
						}
						
					}catch(Exception e){
						e.printStackTrace();
						Logger.e(TAG, "mFTPClient upload Exception: "+e);
						return TASK_FAILED_UPLOAD_APK_DATA;//"zip file not found. "+filePath;
					}
					f.delete();
				}
				FileUtil.deleteFile(dirPath, true);
			}
			/*
			uninstallApk();
			*/
		}
		return err;
	}
	
	
	private boolean send_ip() {
		try  
        {
			OkHttpClient client = new OkHttpClient();  
			//MediaType JSON = MediaType.parse("application/json; charset=utf-8");
			//RequestBody requestBody = RequestBody.create(JSON, json);
			RequestBody formBody = new FormBody.Builder()
					    .add("subtask_id", mSubtask.geteId())
					    .build();
			postInfo("send_ip: subtask_id="+mSubtask.geteId());

			Request request = new Request.Builder()  
		            .url(Constant.getURL_SEND_IP())  
		            .post(formBody)
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "send_ip: response erroNo=" + errorNo);
            if (errorNo!=null && errorNo.equals("0")) {
				return true;
			} 
        }  
        catch (Exception e)  
        {  
        	postInfo("send_ip Exception: "+e);
        }  
		return false;
	}
	
	
	private int execScript(boolean no_sim){
		mScriptFailedMsg = null;
		mScriptResultP1 = -1;
		mScriptResultP2 = -1;
		mScriptResultP3 = null;
		
		LuaEnv.getInstance().clearPrints();
		LuaEnv.getInstance().clearParams();
		if(mEnableVPN){
			LuaEnv.getInstance().putParam("vpn_city", mVpnProfile.account.getCity());
		}
		LuaEnv.getInstance().putParam("no_sim", no_sim?"true":"false");
		if(!no_sim){
			LuaEnv.getInstance().putParam("imsi", mImsi);
		}else{
			LuaEnv.getInstance().putParam("nosim_key", String.format("%d-%06d", mPhoneSeq,mNoSimVdeviceId+1));
		}
        LuaEnv.getInstance().putParam("test_script", "false");
        if(mApk != null){
        	LuaEnv.getInstance().putParam("package_name", mApk.getPackageName());
        	LuaEnv.getInstance().putParam("launch_activity", mApk.getLaunchActivity());
        	LuaEnv.getInstance().putParam("app_name", mApk.getName());
        }
        LuaEnv.getInstance().putParam("task_id", mTask.geteId());
        LuaEnv.getInstance().putParam("subtask_id", mSubtask.geteId());
        LuaEnv.getInstance().putParam("subtask_status", String.valueOf(mSubtask.getStatus()));
        if(mSubtask.getKeepDays()!=null && mSubtask.getKeepDays()>0){
        	LuaEnv.getInstance().putParam("keep_days", Integer.toString(mSubtask.getKeepDays()));
        }
        if(mSubtask.getP1()!=null&& mSubtask.getP1()>0){
        	LuaEnv.getInstance().putParam("p1", Integer.toString(mSubtask.getP1()));
        }
        if(mSubtask.getP2()!=null&& mSubtask.getP2()>0){
        	LuaEnv.getInstance().putParam("p2", Integer.toString(mSubtask.getP2()));
        }
        if(mSubtask.getP3()!=null&&mSubtask.getP3().length()>0){
        	LuaEnv.getInstance().putParam("p3", mSubtask.getP3());
        }
		boolean success = LuaEnv.getInstance().doFile(mTask.getScriptPath());
		LuaState L = LuaEnv.getInstance().L;

		//int status = L.toBoolean(-1)?EnumType.TASK_STATUS_DONE:EnumType.TASK_STATUS_FAILED;
		if(success) {
			L.getGlobal("return_status");
			boolean b = L.toBoolean(-1);
			L.getGlobal("return_result");
			String message = L.toString(-1);
			if(!b) {
				//mShotLog = LuaEnv.getInstance().getPrints();
			}else{
				L.getGlobal("return_p1");
				mScriptResultP1 = (int) L.toInteger(-1);
				L.getGlobal("return_p2");
				mScriptResultP2 = (int) L.toInteger(-1);
				L.getGlobal("return_p3");
				mScriptResultP3 =  L.toString(-1);
				//mShotLog = null;
			}
			LuaEnv.getInstance().clearPrints();
			Logger.e(TAG, "execScript result="+b+",message="+message);
			if(!b){
				mScriptFailedMsg = "execScript failed1: "+message;
				return TASK_FAILED_EXEC_SCRIPT;
			}
		}else{
			mScriptFailedMsg = "execScript failed2: "+L.toString(-1);
			return TASK_FAILED_EXEC_SCRIPT;
		}
		return 0;
	}
	
	private boolean notify_server_to_mark_sim_switch_failed(SimSlot ss){
		if(ss == null) return false;
		try  
        {
			OkHttpClient client = new OkHttpClient(); 
			FormBody.Builder builder = new FormBody.Builder()
					.add("phone_seq", Integer.toString(mPhoneSeq))
				    .add("pool_seq", Integer.toString(ss.getPoolSeq()))
				    .add("slot_seq", Integer.toString(ss.getSlotSeq()))
				    .add("pool_seq", Integer.toString(ss.getPoolSeq()))
				    .add("sim_status", Integer.toString(ss.getSimStatus()))
				    .add("slot_status", "1")
				    .add("sim_imsi", ss.getSimImsi())
				    .add("sim_seq", Integer.toString(ss.getSimSeq()));
			Request request = new Request.Builder()  
		            .url(Constant.getURL_UPDATE_SIM_SLOT())  
		            .post(builder.build())
		            .build();  
			postInfo("notify_server_to_mark_sim_switch_failed: slot_seq="+ss.getSlotSeq()+",sim_seq="+ss.getSimSeq()+",slot_status=1");
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "notify_server_to_mark_sim_switch_failed: response erroNo=" + errorNo);
            return (errorNo!=null && errorNo.equals("0"));
        }  
        catch (Exception e)  
        {  
        	postInfo("notify_server_to_mark_sim_switch_failed Exception: "+e);
        }  
		return false;
	}
	
	
	
	private boolean sim_service_ready(){
		// detect sim state
		mServiceState= ServiceState.STATE_POWER_OFF;
		for(int i=0; i<15; i++) {
			mTelephonyManager.listen(myListener, PhoneStateListener.LISTEN_SERVICE_STATE);  
			if(!enterSleep(1000)) break;
    		mTelephonyManager.listen(myListener, PhoneStateListener.LISTEN_NONE);
			if(mServiceState == ServiceState.STATE_IN_SERVICE){
				break;
			}
			if(!enterSleep(1000)) break;
		}
		return (mServiceState == ServiceState.STATE_IN_SERVICE);
	}
	
	private int getValidSimCount() {
		int count = 0;
		for(int i=0; i<mSimStatusArray.length;i++){
			if(mSimStatusArray[i]==0) count++;
		}
		return count;
	}
	
	private boolean installApk(){
		int stat = SystemUtil.checkApkInstalled(mContext, mApk.getPackageName(), mApk.getVersionCode());
		if(stat == 0){
			// clear apk cache
			//shell.killApp(mApk.getPackageName());
			//enterSleep(100);
			shell.clearApp(mApk.getPackageName());
			if(!enterSleep(200)) return false;
		}else {
			if(stat==1){
				shell.uninstallApp(mApk.getPackageName());
				if(!enterSleep(1000)) return false;
			}
			shell.installApp(mApkLocalPath, mApk.getPackageName());
		}
		
		boolean apk_installed = false;
		for(int i=0; i<30; i++) {
			if(SystemUtil.checkApkInstalled(mContext, mApk.getPackageName(), mApk.getVersionCode())==0){
				apk_installed = true;
				break;
			}
			if(!enterSleep(1000)) break;
		}
		
		if(apk_installed){
			SystemUtil.disableAppStaticReceivers(mContext, mApk.getPackageName());
			Logger.e(TAG, "APK installed.");
			return true;
		}else {
			Logger.e(TAG, "APK not installed.");
			return false;
		}
	}
	
	private boolean uninstallApk(){
		if(mTask.getApkType()==EnumType.TASK_APK_TYPE_DELETE){
			return shell.uninstallApp(mApk.getPackageName());
		}else {
			shell.clearApp(mApk.getPackageName());
			SystemUtil.disableAppStaticReceivers(mContext, mApk.getPackageName());
			return false;
		}
	}
	
	
	
	private int backup_and_zip_shot(String filePath, String pubDirs){
		int count = RootApkUtil.BackupPackageData(shell, mApk.getPackageName(), filePath, pubDirs, false);
		if(count < 0) return -1;
		if(count>0) {
			return ZipUtils.ZipDir(filePath, false)?1:-1;
		}else {
			return 0;
		}
	}
	private String unzip_and_restore_shot(String filePath){
		File f = new File(filePath);
		if(!f.exists()){
			return "file not exist: "+filePath;
		}
		if(!f.getName().toLowerCase().endsWith(".zip")){
			return "not a zip file: "+f.getName();
		}
		if(!ZipUtils.UnzipFile(filePath, null)){
			return "unzip file failed.";
		}
		String dir = FileUtil.getFileDir(filePath);
		if(!RootApkUtil.RestorePackageData(shell, mApk.getPackageName(), dir, true)){
			return "restore apk data failed.";
		}
		return null;
	}
	
	private void clearRecentMessages(){
		postInfo("clearRecentMessages");
		if(shell.startApp("com.android.mms", ".ui.BootActivity")){
			shell.inputTap(686, 90);
			shell.inputTap(554, 71);
			shell.inputTap(613, 700);
			shell.sleep_seconds(3);
			shell.killApp("com.android.mms");
		}
		
	}
	private void clearRecentCalls(){
		postInfo("clearRecentCalls");
		if(shell.startApp("com.android.dialer", ".DialtactsActivity")){
			shell.inputTap(668, 85);
			shell.inputTap(492, 88);
			shell.inputTap(688, 80);
			shell.inputTap(518, 152);
			shell.inputTap(508, 696);
			shell.sleep_seconds(3);
			shell.killApp("com.android.dialer");
		}
	}
	
	private void clearFinishedTaskFiles() {
		String dir = Constant.getTaskStoragePath();
		File d = new File(dir);
		if(!d.exists()||d.isFile()) return;
		File[] files = d.listFiles();
		if(files == null || files.length == 0)return;
		for(File f: files){
			if(f.isDirectory()&&f.getName().length()>5){
				Task t = queryTask(f.getName());
				if(t==null||(t.getStatus()!=EnumType.TASK_STATUS_RUNNING&&
						t.getStatus()!=EnumType.TASK_STATUS_PAUSED)){
					FileUtil.deleteFile(f.getAbsolutePath(), true);
					Config.remove(f.getName()+"_sv");
				}
			}
		}
	}
	
	private void clearInstalledApps() {
		
	}
	
	private Task queryTask(String task_id) {
		try  
        {
			OkHttpClient client = new OkHttpClient(); 
			FormBody.Builder builder = new FormBody.Builder()
				    .add("task_id", task_id);
			Request request = new Request.Builder()  
		            .url(Constant.getURL_GET_TASk())  
		            .post(builder.build())
		            .build();  
			postInfo("queryTask: task_id="+task_id);
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "queryTask: response erroNo=" + errorNo);
            if(errorNo!=null && errorNo.equals("0")){
            	JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					String taskStr = dataObj.optString("task");
					if(taskStr != null){
						Task t = JSON.parseObject(taskStr, Task.class);
						if(t != null){
							return t;
						}
					}
				}
            }  
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
        	postInfo("queryTask Exception: "+e);
        }  
		return null;
	}
	
	private String getErr(int err){
		switch(err){
		case 0:return "Success";
		case TASK_FAILED_BAD_SCRIPT: return "TASK_FAILED_BAD_SCRIPT";
		case TASK_FAILED_REGISTER_DEVICE: return "TASK_FAILED_REGISTER_DEVICE";
		case TASK_FAILED_DOWNLOAD_SCRIPT: return "TASK_FAILED_DOWNLOAD_SCRIPT";
		case TASK_FAILED_DOWNLOAD_APK: return "TASK_FAILED_DOWNLOAD_APK";
		case TASK_FAILED_INSTALL_APK: return "TASK_FAILED_INSTALL_APK";
		case TASK_FAILED_DOWNLOAD_APK_DATA: return "TASK_FAILED_DOWNLOAD_APK_DATA";
		case TASK_FAILED_DECODE_APK_DATA: return "TASK_FAILED_DECODE_APK_DATA";
		case TASK_FAILED_CONNECT_MIFI: return "TASK_FAILED_CONNECT_MIFI";
		case TASK_FAILED_SWITCH_WIFI: return "TASK_FAILED_SWITCH_WIFI";
		case TASK_FAILED_SWITCH_4G: return "TASK_FAILED_SWITCH_4G";
		case TASK_FAILED_TURNON_VPN: return "TASK_FAILED_TURNON_VPN";
		case TASK_FAILED_TURNOFF_VPN: return "TASK_FAILED_TURNOFF_VPN";
		case TASK_FAILED_SEND_IP: return "TASK_FAILED_SEND_IP";
		case TASK_FAILED_ENCODE_APK_DATA: return "TASK_FAILED_ENCODE_APK_DATA";
		case TASK_FAILED_EXEC_SCRIPT: return "TASK_FAILED_EXEC_SCRIPT";
		case TASK_FAILED_DOWNLOAD_EXTRA: return "TASK_FAILED_DOWNLOAD_EXTRA";
		case TASK_FAILED_UNZIP_EXTRA: return "TASK_FAILED_UNZIP_EXTRA";
		case TASK_FAILED_INTERRUPT: return "TASK_FAILED_INTERRUPT";
		case TASK_FAILED_UPLOAD_APK_DATA: return "TASK_FAILED_UPLOAD_APK_DATA";
		case TASK_FAILED_CLEAR_APK: return "TASK_FAILED_CLEAR_APK";
		default: return "unknown";
		}
	}
	
	public static boolean ftp_mkdirs(FTPClient c, String ftp_path){
		String[] dirs = ftp_path.split("/");
		String dir = "";
		for(int i=0; i<dirs.length;i++){
			dir += "/"+dirs[i];
			try {
				c.changeDirectory(dir);
			}catch(FTPException e){
				//e.printStackTrace();
				try {
					c.createDirectory(dir);
				}catch(Exception e2){
					e2.printStackTrace();
					return false;
				}
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
}
