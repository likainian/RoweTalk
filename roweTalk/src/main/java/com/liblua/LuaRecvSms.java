package com.liblua;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.rowetalk.util.Logger;
import com.example.rowetalk.util.StringUtil;
import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaState;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.telephony.SmsMessage;
import android.util.Log;

public class LuaRecvSms extends JavaFunction
{
	private static final String TAG = LuaRecvSms.class.getName();
	private LuaState L;
	private LuaEnv Env;

	private static final String SMS_URI_ALL = "content://sms/";  
	private static final String SMS_URI_INBOX = "content://sms/inbox";  
	private static final String SMS_URI_SEND = "content://sms/sent";  
	private static final String SMS_URI_DRAFT = "content://sms/draft";  
	private static final String SMS_URI_OUTBOX = "content://sms/outbox";  
	private static final String SMS_URI_FAILED = "content://sms/failed";  
	private static final String SMS_URI_QUEUED = "content://sms/queued"; 
	
	public LuaRecvSms(LuaEnv Env, LuaState L)
	{
		super(L);
		this.L = L;
		this.Env = Env;
	}

	/**
	 * 参数：String number，long wait_seconds，String flagPattern, String codePattern, long sent_time
	 * 返回：boolean success, string wait_secondsrecv_number, string matched, string whole_sms
	 */
	@Override
	public int execute() throws LuaException
	{
		if (L.getTop() < 2)
		{
			L.pushBoolean(false);
			L.pushString("bad parameter count");
			return 2;
		}
		// get number
		String number = L.toString(2);
		
		long wait_seconds = 0;
		if(L.getTop()>2){
			wait_seconds = L.toInteger(3);
		}else {
			wait_seconds = 120; // 120s
		}
		String flagPattern = null;
		if(L.getTop()>3){
			flagPattern = L.toString(4);
		}
		String codePattern = null;
		if(L.getTop()>4){
			codePattern = L.toString(5);
		}
		long sent_time = 0;
		if(L.getTop()>5){
			sent_time = L.toInteger(6);
		}
		
		//read storage sms
		List<String> mobileList = new ArrayList<String>();
		List<String> contentList = new ArrayList<String>();
		List<String> rawList = new ArrayList<String>();
		readSms(mobileList, contentList, rawList, flagPattern, codePattern, sent_time);
		
		if(mobileList.size()!=0){
			Logger.e(TAG, "read new msg: "+ mobileList.size());
		}else {
			Logger.e(TAG, "wait recvSms: "+wait_seconds+" secs");
			int r = Env.wait("recvSms", wait_seconds*1000);
			if(r != Activity.RESULT_OK){
				L.pushBoolean(false);
				L.pushString("recvSms result="+r);
				return 2;
			}
			List<SmsMessage> l = (List<SmsMessage>)Env.getNotifyObject();
			if(l == null || l.size()==0){
				L.pushBoolean(false);
				L.pushString("no sms recved.");
				return 2;
			}
			
			
			for(SmsMessage sms: l){
				String mobile=sms.getOriginatingAddress();
				String content=sms.getMessageBody();
				if(!StringUtil.isEmptyOrNull(number)&&!number.equals(mobile)) {
					//return first message
					continue;
				}
				if(!StringUtil.isEmptyOrNull(flagPattern)){
					if(content!=null && content.indexOf(flagPattern)<0){
						continue;
					}
				}
				if(!StringUtil.isEmptyOrNull(codePattern)){
					Pattern p = Pattern.compile(codePattern);
					Matcher m = p.matcher(content);
					if(!m.find()){
						continue;
					}
					p = Pattern.compile(codePattern);
					m = p.matcher(content);
					if(!m.find()){
						continue;
					}
					mobileList.add(mobile);
					contentList.add(m.group(1));
					rawList.add(content);
					Logger.e(TAG, "matched sms: "+mobile+", "+m.group(1));
				}else{
					mobileList.add(mobile);
					contentList.add(content);
					rawList.add("");
					Logger.e(TAG, "matched sms2: "+mobile+", "+content);
				}
			}
		}
	
		if(mobileList.size()==0){
			L.pushBoolean(false);
			L.pushString("not found matched sms.");
			return 2;
		}
		L.pushBoolean(true);
		int count = 1;
		
		for(int i=0;i<mobileList.size();i++){
			L.pushString(mobileList.get(i));
			L.pushString(contentList.get(i));
			L.pushString(rawList.get(i));
			count+=3;
			Logger.e(TAG, "recvd sms: "+mobileList.get(i)+", "+contentList.get(i));
		}
		return count;
	}

	private boolean readSms(List<String> mobileList, List<String> contentList, List<String> rawList, String flagPattern, String codePattern, long sent_time) {
		try {  
            Uri uri = Uri.parse(SMS_URI_INBOX);  
            String[] projection = new String[] { "_id", "address","body", "date" };  
            Cursor cur = Env.getContext().getContentResolver().query(uri, projection, "read=0", null, "date desc");      // 获取手机内部短信
            if (cur.moveToFirst()) { 
            	int index_Address = cur.getColumnIndex("address");  
                int index_Body = cur.getColumnIndex("body");  
                int index_Date = cur.getColumnIndex("date");  
                Date d = new Date();
                do {  
                    String mobile = cur.getString(index_Address);  
                    String content = cur.getString(index_Body);  
                    long longDate = cur.getLong(index_Date); 
                    
                    if(!StringUtil.isEmptyOrNull(flagPattern)){
    					if(content.indexOf(flagPattern)<0){
    						continue;
    					}
    				}
                    if(sent_time != 0){
                    	if(longDate<=sent_time)
                    		continue;
                    }else {
	                    long diff = d.getTime()-longDate;
	                    //Logger.e(TAG, "readSms: time_diff="+diff+" ms");
	                    if(diff > 2000) // recvd in 2s
	                    	continue;
                    }
                    if(!StringUtil.isEmptyOrNull(codePattern)){
        				Pattern p = Pattern.compile(codePattern);
        				Matcher m = p.matcher(content);
        				if(!m.find()){
        					continue;
        				}
        				
        				p = Pattern.compile(codePattern);
        				m = p.matcher(content);
        				if(!m.find()){
        					continue;
        				}
        				mobileList.add(mobile);
        				contentList.add(m.group(1));
        				rawList.add(content);
        			}else{
        				mobileList.add(mobile);
        				contentList.add(content);
        				rawList.add("");
        			}
                } while (cur.moveToNext()); 
            }
            if (!cur.isClosed()) {  
                cur.close();  
                cur = null;  
            }  
		} catch (SQLiteException ex) {  
            Logger.d("SQLiteException in getSmsInPhone", ex.getMessage());  
            return false;
        }  
		return true;
	}
	
	private String readSms() {
		StringBuilder smsBuilder = new StringBuilder();  
		  
        try {  
            Uri uri = Uri.parse(SMS_URI_ALL);  
            String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };  
            Cursor cur = Env.getContext().getContentResolver().query(uri, projection, null, null, "date desc");      // 获取手机内部短信  
  
            if (cur.moveToFirst()) {  
                int index_Address = cur.getColumnIndex("address");  
                int index_Person = cur.getColumnIndex("person");  
                int index_Body = cur.getColumnIndex("body");  
                int index_Date = cur.getColumnIndex("date");  
                int index_Type = cur.getColumnIndex("type");  
  
                do {  
                    String strAddress = cur.getString(index_Address);  
                    int intPerson = cur.getInt(index_Person);  
                    String strbody = cur.getString(index_Body);  
                    long longDate = cur.getLong(index_Date);  
                    int intType = cur.getInt(index_Type);  
  
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
                    Date d = new Date(longDate);  
                    String strDate = dateFormat.format(d);  
  
                    String strType = "";  
                    if (intType == 1) {  
                        strType = "接收";  
                    } else if (intType == 2) {  
                        strType = "发送";  
                    } else {  
                        strType = "null";  
                    }  
  
                    smsBuilder.append("[ ");  
                    smsBuilder.append(strAddress + ", ");  
                    smsBuilder.append(intPerson + ", ");  
                    smsBuilder.append(strbody + ", ");  
                    smsBuilder.append(strDate + ", ");  
                    smsBuilder.append(strType);  
                    smsBuilder.append(" ]\n\n");  
                } while (cur.moveToNext());  
  
                if (!cur.isClosed()) {  
                    cur.close();  
                    cur = null;  
                }  
            } else {  
                smsBuilder.append("no result!");  
            } // end if  
  
            smsBuilder.append("getSmsInPhone has executed!");  
  
        } catch (SQLiteException ex) {  
            Logger.e("SQLiteException in getSmsInPhone", ex.getMessage());  
        }  
  
        return smsBuilder.toString();  
	}
	

}

