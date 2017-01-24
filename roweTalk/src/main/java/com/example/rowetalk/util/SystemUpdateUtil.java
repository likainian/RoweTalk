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
import android.os.SystemProperties;

public class  SystemUpdateUtil {
	private static final String TAG = SystemUpdateUtil.class.getName();
	

	public static SystemUpdate checkUpdate(int phone_seq, String tag) {
		// TODO Auto-generated method stub
		try  
        {
			/*
			private static final String OEM_DEV = SystemProperties.get("ro.product.manufacturer");
		    private static final String LANG_DEV = SystemProperties.get("ro.product.locale.language");
		    private static final String PRODUCT_DEV = SystemProperties.get("ro.product.device");
		    private static final String OPER_DEV = SystemProperties.get("ro.operator.optr");
		    private static final String FLAVOR_DEV = SystemProperties.get("ro.build.flavor");
			 */
			String base_version = SystemProperties.get("ro.mediatek.version.release");
			String product = SystemProperties.get("ro.product.device");
			String oem = SystemProperties.get("ro.product.manufacturer");
			String operator = SystemProperties.get("ro.operator.optr");
			String flavor = SystemProperties.get("ro.build.flavor");
			if(base_version == null || product==null || oem ==null){
				Logger.e(TAG, "checkSystemUpdate failed, params error "+String.format("(%s/%s/%s)", base_version, product, oem));
				return null;
			}
			Logger.e(TAG, "checkSystemUpdate:"+String.format("phone_seq=%d,base_version=%s,product=%s,oem=%s,tag=%s)", phone_seq,base_version, product, oem,tag));
			OkHttpClient client = new OkHttpClient(); 
			FormBody.Builder builder = new FormBody.Builder()
				    .add("phone_seq", String.valueOf(phone_seq))
				    .add("base_version", base_version)
				    .add("product", product)
				    .add("oem", oem);
			if(operator != null) operator= operator.trim();
			if(flavor != null) flavor= flavor.trim();
			if(tag != null) tag= tag.trim();
			if(StringUtil.isNotEmpty(operator))
					builder.add("operator", operator);
			if(StringUtil.isNotEmpty(flavor))
				builder.add("flavor", flavor);
			if(StringUtil.isNotEmpty(tag))
				builder.add("tag", tag);
			Request request = new Request.Builder()  
		            .url(Constant.getURL_SYSTEM_CHECK_VERSION())  
		            .post(builder.build())
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
		    Logger.e(TAG, "checkSystemUpdate: response erroNo=" + errorNo);
            if(errorNo!=null && errorNo.equals("0")){
            	JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					SystemUpdate d = null;
					String d_str = dataObj.optString("system_update");
					if(StringUtil.isNotEmpty(d_str)){
						d = JSON.parseObject(d_str, SystemUpdate.class);
					}
					return d;
				}
            }
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
        	Logger.e(TAG, "checkSystemUpdate Exception: "+e);
        }  
		return null; 	
	}

	public static boolean downloadUpdate(SystemUpdate su, String filePath) {
		// TODO Auto-generated method stub
		if(su == null || filePath==null) return false;
		String url = Constant.getURL_SYSTEM_DOWNLOAD_VERSION();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("system_update_id", su.geteId());
		
		if(!HttpUtil.downloadFile(url, params, filePath, new ProgressHandler() {
			
			@Override
			public void updateSize(long size) {
				// TODO Auto-generated method stub
				Logger.e(TAG, "downloadSystemUpdate updateSize "+size);
			}
		})){
			return false;//"download apk file failed.";
		}
		return true;
	}

	
}
