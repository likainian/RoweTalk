package com.example.rowetalk.thread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.rowetalk.bean.EnumType;
import com.example.rowetalk.bean.SimSlot;
import com.example.rowetalk.util.Config;
import com.example.rowetalk.util.Constant;
import com.example.rowetalk.util.DateUtil;
import com.example.rowetalk.util.Logger;
import com.example.rowetalk.util.NetworkUtil;
import com.example.rowetalk.util.SleepObject;
import com.example.rowetalk.util.StringUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VerifyCardThread extends BaseThread {
	private static final String TAG = VerifyCardThread.class.getName();
   
	private int mSlotSeq;
	private int mSimSeq;
	private boolean mMarkCards;
	private boolean mSpecifyCard;
	private int mSpecifySimIndex;
	private int mSpecifySimSeq;
	private int mSpecifySlotSeq;
	
	private static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";  
	
	public VerifyCardThread(Context context, Handler handler, SleepObject object, Map<String, Object> map) {
		super(context, handler, object, map);
		mMarkCards= map.containsKey("mark_cards")?(Boolean)map.get("mark_cards"):false;
		mSpecifyCard = map.containsKey("specify_card")?(Boolean)map.get("specify_card"):false;
		if(mSpecifyCard) {
			mSpecifySimIndex = map.containsKey("sim_index")?(Integer)map.get("sim_index"):-1;
			mSpecifySimSeq = map.containsKey("sim_seq")?(Integer)map.get("sim_seq"):-1;
			mSpecifySlotSeq = map.containsKey("slot_seq")?(Integer)map.get("slot_seq"):-1;
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		postStartedMsg();
		if(!init()){
            postEndedMsg();
			return;
		}
		Set<String> successSet = new HashSet<String>();
		IntentFilter intentFilter = new IntentFilter();  
        intentFilter.addAction(ACTION_SIM_STATE_CHANGED);
        intentFilter.setPriority(Integer.MAX_VALUE);  
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);  
        WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK   | PowerManager.ON_AFTER_RELEASE, "DPA");  
        wakeLock.acquire();
        
        int firstSim = -1;
        
		isRunning = true;
        while (isRunning) {
        	if(mSpecifyCard) {
        		if(mSpecifySimIndex>=0){
        			postInfo("mSpecifyCard: sim_index="+mSpecifySimIndex);
        			mSimCardId = mSpecifySimIndex;
        			int index = (mSimCardId)/16;
        			int slot_seq = mSlotSeqList.size()>index?mSlotSeqList.get(index):-1;
        			int sim_seq = ((mSimCardId) % 16)+1;
        			boolean switch_result = getNextSimCard();
                	if(switch_result){
                		postInfo("switch card success imsi="+mImsi+",sim_index="+mSpecifySimIndex+",slot_seq="+slot_seq+",sim_seq="+sim_seq);
                	}else{
                		postInfo("switch card failed sim_index="+mSpecifySimIndex+",slot_seq="+slot_seq+",sim_seq="+sim_seq);
                	}
        		}else if(mSpecifySlotSeq>1000&&mSpecifySimSeq>0&&mSpecifySimSeq<=16){
        			int count = 0;
        			for(int i=0;i<mSlotSeqList.size();i++){
        				if(mSpecifySlotSeq==mSlotSeqList.get(i)){
        					break;
        				}
        				count++;
        			}
        			mSimCardId = count*16+mSpecifySimSeq-1;
        			int index = (mSimCardId)/16;
        			int slot_seq = mSlotSeqList.get(index);
        			int sim_seq = ((mSimCardId) % 16)+1;
        			if(mSpecifySimSeq != sim_seq || mSpecifySlotSeq != slot_seq){
        				postInfo("bad param: "+String.format("(%d,%d) calc-> (%d,%d)", mSpecifySlotSeq, mSpecifySimSeq, slot_seq, sim_seq));
        				break;
        			}
        			
        			boolean switch_result = getNextSimCard();
        			if(switch_result){
                		postInfo("switch card success imsi="+mImsi+",sim_index="+mSpecifySimIndex+",slot_seq="+mSpecifySlotSeq+",sim_seq="+mSpecifySimSeq);
                	}else{
                		postInfo("switch card failed sim_index="+mSpecifySimIndex+",slot_seq="+mSpecifySlotSeq+",sim_seq="+mSpecifySimSeq);
                	}
        		}else{
        			postInfo("传入参数错误!");
        		}
        		break;
        	}
        	postFalseTasks(failedTasks);
        	postFalseSlots(failedSlots);
        	postSuccessTasks(Integer.toString(successSet.size()));
        	int sim_status = EnumType.SIM_STATUS_NOT_AVAILABLE;
        	
        	if(mSimCardId >= mSimRangeEnd){
        		mSimCardId = mSimRangeStart;
        	}
        	
        	if(firstSim == -1){
        		firstSim = mSimCardId;
        	}else if(firstSim == mSimCardId){
        		postInfo("loop finished.");
        		break;
        	}
        	
        	int index = (mSimCardId)/16;
        	if(index >= mSlotSeqList.size()){
        		postInfo(String.format("index(%d) >= mSlotSeqList.size(%d), mSimCardId=%d", index, mSlotSeqList.size(), mSimCardId));
        		break;
        	}
        	mSlotSeq = mSlotSeqList.get(index);
    		if(mSlotSeq==0){
    			postInfo("skip sim "+mSimCardId+" because slot not specified");
    			mSimCardId++;
    			continue;
    		}
    		mSimSeq = ((mSimCardId) % 16)+1;
    		if(!mMarkCards) {
        		SimSlot ss = sim_slot_get_status(mSlotSeq, mSimSeq);
				if(ss == null) {
					postInfo("wait 10s to retry sim_slot_get_status");
					enterSleep(2*1000);
					continue;
				}else if((ss.getSimStatus()>0 || ss.getSlotStatus()>0)){
					// 服务器上已经标记了此卡不可用
					postInfo("server marked sim NA, sim_status="+ss.getSimStatus()+",slot_status="+ss.getSlotStatus());
					mSimCardId++;
					failedTasks+="("+mSimCardId+"),";
					continue;
				}
    		}else{
    			SimSlot ss = sim_slot_get_status(mSlotSeq, mSimSeq);
    			if(ss != null && ss.getSimStatus()==0){
    				successSet.add(String.format("(%d,%d)", mSlotSeq, mSimSeq));
    				mSimCardId++;
    				postInfo(String.format("(%d,%d) exists, ignore", mSlotSeq, mSimSeq));
    				continue;
    			}
    		}
			boolean switch_result = getNextSimCard();
        	if(!switch_result){
				mSimCardId++;
				postCurrentCard(String.valueOf(mSimCardId));
				failedSlots += mSimCardId+",";
				if(!mMarkCards){
					continue;
				}
			}else {
				Config.putInt("sim_start", mSimCardId);
				mSimCardId++;
	        	postCurrentCard(String.valueOf(mSimCardId));
	        	
	        	//query 
	        	if(mMarkCards) {
	        		SimSlot ss = getSimSlotByImsi(mImsi);
	        		if(ss != null){
	        			if(DateUtil.daysElapsed(ss.getCreateTime())>30){
		        			postInfo("ERROR: old sim_slot exists, slot_seq="+ss.getSlotSeq()+",sim_seq="+ss.getSimSeq());
		        			break;
	        			}
	        			if(ss.getSlotSeq()!=mSlotSeq||ss.getSimSeq()!=mSimSeq){
	        				postInfo("ERROR: trying to replace exits slot: "+String.format("IMSI:%s, OLD(%d,%d), NEW(%d,%d)", mImsi, ss.getSlotSeq(),ss.getSimSeq(),mSlotSeq,mSimSeq));
	        				break;
	        			}
	        		}
	        	}
				
				mServiceState= ServiceState.STATE_POWER_OFF;
				for(int i=0; i<15; i++) {
					mTelephonyManager.listen(myListener, PhoneStateListener.LISTEN_SERVICE_STATE);  
					enterSleep(1000);
		    		mTelephonyManager.listen(myListener, PhoneStateListener.LISTEN_NONE);
					if(mServiceState == ServiceState.STATE_IN_SERVICE){
						break;
					}
					enterSleep(1000);
					if(!isRunning) {
		        		break;
		        	}
				}
				if(!isRunning) {
	        		break;
	        	}
				sim_status = mServiceState;
				if(mServiceState != ServiceState.STATE_IN_SERVICE){
					//break;
	        		postInfo("phone not in service after 30s.");
	        		failedTasks += mSimCardId+",";
				}
				successSet.add(String.format("(%d,%d)", mSlotSeq, mSimSeq));
			}
        	
        	if(isRunning && !updateSimSlot(sim_status)){
        		postInfo("updateSimSlot failed.");
        		break;
        	}
        	
        }
        mContext.unregisterReceiver(mBroadcastReceiver);
        wakeLock.release();
        isRunning = false;
		
		deinit();
		postFalseTasks(failedTasks);
		postEndedMsg();
		
	}
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver () {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(action == null) return;
			Log.e(TAG, "mBroadcastReceiver: onReceive "+intent.getAction());
			if (action.equals(ACTION_SIM_STATE_CHANGED)) {
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
	            Logger.e(TAG, "ACTION_SIM_STATE_CHANGED: "+state);
				if(state==TelephonyManager.SIM_STATE_READY ){
					notify_response("sim_reset", state, null);
				}
			
			}
		}
		
	};

	@Override
	public void cancel(boolean keep_app_state) {
		// TODO Auto-generated method stub
		super.cancel(keep_app_state);
	}
	
	private boolean updateSimSlot(int sim_status) {
		String imsi = null;
		if(sim_status != EnumType.SIM_STATUS_NOT_AVAILABLE) {
			imsi = mImsi;
		}
		if(!NetworkUtil.isNetworkConnected(mContext)) {
			postInfo("updateSimSlot failed, network not connected.");
			return false;
		}
		int slot_seq = 0;
		int sim_seq = 0;
		
		// calculate slot_seq
		slot_seq = mSlotSeqList.get((int)((mSimCardId-1)/16));
		sim_seq = ((mSimCardId-1) % 16)+1;
		
		if(mPhoneSeq == 0 || mPoolSeq == 0 || slot_seq == 0 || sim_seq ==0 ){
			postInfo("updateSimSlot failed, params failed: phone_seq="+mPhoneSeq+",pool_seq="+mPoolSeq+",slot_seq="+slot_seq+",sim_seq="+sim_seq);
			return false;
		}
		
		postInfo("updateSimSlot: slot_seq="+slot_seq+",sim_seq="+sim_seq+",sim_status="+sim_status+",sim_imsi="+imsi);
		
		try  
        {
			OkHttpClient client = new OkHttpClient(); 
			FormBody.Builder builder = new FormBody.Builder()
				    .add("phone_seq", Integer.toString(mPhoneSeq))
				    .add("pool_seq", Integer.toString(mPoolSeq))
				    .add("slot_seq", Integer.toString(slot_seq))
				    .add("sim_seq", Integer.toString(sim_seq))
				    .add("sim_status", Integer.toString(sim_status));
			if(imsi != null){
				builder.add("sim_imsi", imsi);
			}
			
			Request request = new Request.Builder()  
		            .url(Constant.getURL_UPDATE_SIM_SLOT())  
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
            	JSONObject data = rspObject.optJSONObject("data");
                SimSlot ss = JSON.parseObject(data.optString("sim_slot"), SimSlot.class);
                if(ss != null) {
                	postInfo("updateSimSlot success, number="+ss.getSimNumber());
                }
            	return true;
            }
	    } catch (Exception e) {  
	    	e.printStackTrace();
	    	Logger.e(TAG, "registerDevice Exception: "+e);
	    } 
		return false;
	}
	
	private SimSlot getSimSlotByImsi(String imsi){
		if(imsi == null) {
			return null;
		}
		if(!NetworkUtil.isNetworkConnected(mContext)) {
			Logger.e(TAG, "getSimSlotByImsi failed, network not connected.");
			return null;
		}
		try  
        {
			OkHttpClient client = new OkHttpClient();  
			FormBody.Builder builder = new FormBody.Builder();
			builder.add("imsi", String.valueOf(imsi));
			RequestBody formBody = builder.build();

			Request request = new Request.Builder()  
		            .url(Constant.getURL_QUERY_NUMBER())  
		            .post(formBody)
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "getSimSlotByImsi: imsi="+imsi+", response erroNo=" + errorNo);
            if(errorNo!=null && errorNo.equals("0")){
            	JSONObject dataObj = rspObject.optJSONObject("data");
            	String str = dataObj.optString("sim_slot");
				if(StringUtil.isNotEmpty(str)){
					SimSlot ss = JSON.parseObject(str, SimSlot.class);
					return ss;
				}
            }
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
            Logger.e(TAG, "getSimSlotByImsi Exception: "+e);
        }  
		return null;
    }
}
