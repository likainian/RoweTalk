package com.example.rowetalk.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.example.rowetalk.bean.Device;
import com.example.rowetalk.bean.Platform;
import com.example.rowetalk.bean.SystemUpdate;
import com.example.rowetalk.util.HttpUtil.ProgressHandler;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.os.Environment;

public class ServerUtil {
	private static final String TAG = ServerUtil.class.getName();
	
	public static String getMyIp(String url){
		
        try  
        {
			OkHttpClient client = new OkHttpClient(); 
			Request request = new Request.Builder()  
		            .url(Constant.getURL_GET_MY_IP())//.url("http://api.wipmania.com/")  
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG,"getMyIp: url="+url+", response erroNo=" + errorNo);
            if(errorNo!=null && errorNo.equals("0")){
            	JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					String ip = dataObj.optString("ip");
		            Logger.e(TAG, "getMyIp: ip=" + ip);
					return ip;
				}
            }
		    
        } catch (Exception e) {  
        	e.printStackTrace();
        	Logger.e(TAG, "getMyIp Exception: "+e);
        }  
		return null; 	
    }

	public static String getMyIp(){
		String ip = getMyIp(Constant.getURL_GET_MY_IP());
		if(ip == null){
			ip = getMyIp("http://api.wipmania.com/");
		}
		return ip;
	}
	
	public static String RandomWord(){
        try  
        {
			OkHttpClient client = new OkHttpClient(); 
			Request request = new Request.Builder()  
		            .url(Constant.getURL_RANDOM_WORD())
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG,"RandomWord: response erroNo=" + errorNo);
            if(errorNo!=null && errorNo.equals("0")){
            	JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					String word = dataObj.optString("word");
		            Logger.e(TAG, "RandomWord: word=" + word);
					return word;
				}
            }
		    
        } catch (Exception e) {  
        	e.printStackTrace();
        	Logger.e(TAG, "RandomWord Exception: "+e);
        }  
		return null; 	
    }
	
	public static Platform RandomPlatform(){
        try  
        {
			OkHttpClient client = new OkHttpClient(); 
			Request request = new Request.Builder()  
		            .url(Constant.getURL_RANDOM_PLATFORM())
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG,"RandomPlatform: response erroNo=" + errorNo);
            if(errorNo!=null && errorNo.equals("0")){
            	JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					String platStr = dataObj.optString("platform");
		            Logger.e(TAG, "RandomPlatform: platStr=" + platStr);
		            Platform p = null;
		            if(StringUtil.isNotEmpty(platStr)){
						p = JSON.parseObject(platStr, Platform.class);
					}
		            return p;
				}
            }
		    
        } catch (Exception e) {  
        	e.printStackTrace();
        	Logger.e(TAG, "RandomPlatform Exception: "+e);
        }  
		return null; 	
    }
	
	public static boolean testServerConnect(){
		try  
        {
			OkHttpClient client = new OkHttpClient(); 
			Request request = new Request.Builder()  
		            .url(Constant.getURL_TEST_CONNECT())  
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            //Logger.e(TAG, "testServerConnect: response erroNo=" + errorNo);
            return (errorNo!=null && errorNo.equals("0"));
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
        	Logger.e(TAG, "testServerConnect Exception: "+e);
        }  
		return false; 	
	}

}
