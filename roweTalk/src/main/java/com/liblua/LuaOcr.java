package com.liblua;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.example.rowetalk.util.Constant;
import com.example.rowetalk.util.DateUtil;
import com.example.rowetalk.util.HttpUtil;
import com.example.rowetalk.util.Logger;
import com.example.rowetalk.util.NumberUtil;
import com.example.rowetalk.util.StringUtil;
import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaState;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LuaOcr extends JavaFunction
{
	private static final String TAG = "LuaOcr";
	private static Bitmap bitmap;
	
	private static final String GET_OCR_TEXT_NUMBER_MARK = "~!-N-!~";
	private static final String GET_OCR_TEXT_ASCII_MARK = "~!-A-!~";
	private static final String GET_OCR_TEXT_CHINESE_MARK = "~!-C-!~";

	private LuaState L;
	private LuaEnv Env;
	
	public LuaOcr(LuaEnv Env, LuaState L)
	{
		super(L);
		this.L = L;
		this.Env = Env;
	}
	
	// ocr(0) // 截图  
	// ocr(1, String text, int left, int top, int right, int bottom, boolean similar_test, boolean english) //识别文本
	// ocr(2, int color, int x, int y, boolean similar_test, int diff) // 识别点颜色
	// ocr(3, int color, int left, int top, right, bottom) // 识别区域颜色
	// ocr(4, boolean save_package_image, string package_name_or_save_path, int left, int top, int right, int bottom) // 保存截图
	// ocr(5, int x, int y) // 取颜色
	// ocr(6, String vendor, String apikey, String codeType, int left, int top, int right, int bottom) // 第三方验证码识别接口
	//返回值： (boolean result, string error_message)
	@Override
	public int execute() throws LuaException
	{
		int param_count = L.getTop();
		if (param_count < 2)
		{
			L.pushBoolean(false);
			L.pushString("bad parameter count");
			return 2;
		}
		int op_type = (int)L.toInteger(2); 
		if(op_type == 0) {
			if(bitmap != null){
				bitmap.recycle();
				bitmap = null;
			}
		}else if(op_type > 6){
			L.pushBoolean(false);
			L.pushString("unknown operation");
			return 2;
		}

		Logger.e(TAG, "LuaOcr: op_type="+op_type);
		
		if(bitmap == null) {
			for(int i=0; i<5; i++) {
				Bitmap cb = Env.takeScreenshot();
				if(cb != null){
					bitmap = cb;
					break;
				}else{
					if(!Env.sleep(3000)) break;
				}
			}
		}
		if(bitmap == null){
			Logger.e(TAG,"takeScreenshot failed");
			L.pushBoolean(false);
			L.pushString("takeScreenshot failed");
			return 2;
		}
		if(op_type == 0) { // capture image
			L.pushBoolean(true);
			L.pushString("success");
			return 2;
		}else if(op_type == 1){ //text  ocr(1, String text, int left, int top, int right, int bottom, boolean similar_test, boolean english) //识别文本
			if (param_count < 7)
			{
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String ocr_text = L.toString(3);
			int left = (int) L.toInteger(4);
			int top = (int) L.toInteger(5);
			int right = (int) L.toInteger(6);
			int bottom = (int) L.toInteger(7);
			boolean smililar_test = false;
			boolean ocr_english = false;
			if(param_count > 7){
				smililar_test = (Boolean)L.toBoolean(8);
			}
			if(param_count > 8){
				ocr_english = (Boolean)L.toBoolean(9);
			}
			if(ocr_english && Env.mTessBaseAPIEng == null){
				L.pushBoolean(false);
				L.pushString("ocr english not support, please reset it.");
				return 2;
			}
			if(!ocr_english && Env.mTessBaseAPIChi == null){
				L.pushBoolean(false);
				L.pushString("ocr chinese not support, please reset it.");
				return 2;
			}
			if(left <0 || right<0 || top <0 || bottom <0 ||  left >= right || top >= bottom){
				L.pushBoolean(false);
				L.pushString("bad params for ocr text");
				return 2;
			}
			Logger.e(TAG, "LuaOcr: "+String.format("text=%s, (%d,%d,%d,%d)", ocr_text,left,top,right,bottom));
			Bitmap cropBmp = null;
			try {
				cropBmp = Bitmap.createBitmap(bitmap, left, top, (right-left), (bottom-top));
			}catch(Exception e){
				e.printStackTrace();
			}
			if(cropBmp != null){
				if(ocr_text.equals(GET_OCR_TEXT_NUMBER_MARK)){
					Env.mTessBaseAPIEng.setVariable("classify_bln_numeric_mode", "1");
					Env.mTessBaseAPIEng.setImage(cropBmp);
					String text1= Env.mTessBaseAPIEng.getUTF8Text();
					Env.mTessBaseAPIEng.clear();
					Log.e(TAG, "LuaOcr: ocr ascii->"+text1);
					L.pushBoolean(true);
					L.pushString(text1);
					cropBmp.recycle();
					cropBmp = null;
					return 2;
				}else if(ocr_text.equals(GET_OCR_TEXT_ASCII_MARK)){
					Env.mTessBaseAPIEng.setVariable("classify_bln_numeric_mode", "0");
					Env.mTessBaseAPIEng.setImage(cropBmp);
					String text1= Env.mTessBaseAPIEng.getUTF8Text();
					Env.mTessBaseAPIEng.clear();
					Log.e(TAG, "LuaOcr: ocr ascii->"+text1);
					L.pushBoolean(true);
					L.pushString(text1);
					cropBmp.recycle();
					cropBmp = null;
					return 2;
				}else if(ocr_text.equals(GET_OCR_TEXT_CHINESE_MARK)){
					Env.mTessBaseAPIChi.setImage(cropBmp);
					String text1= Env.mTessBaseAPIChi.getUTF8Text();
					Env.mTessBaseAPIChi.clear();
					Log.e(TAG, "LuaOcr: ocr chinese->"+text1);
					L.pushBoolean(true);
					L.pushString(text1);
					cropBmp.recycle();
					cropBmp = null;
					return 2;
				}
				String text2 = null;
				if(ocr_english) {
					Env.mTessBaseAPIEng.setImage(cropBmp);
					text2= Env.mTessBaseAPIEng.getUTF8Text();
					Env.mTessBaseAPIEng.clear();
					Log.e(TAG, "LuaOcr: ocr english text->"+text2);
				}else{
					Env.mTessBaseAPIChi.setImage(cropBmp);
					text2= Env.mTessBaseAPIChi.getUTF8Text();
					Env.mTessBaseAPIChi.clear();
					Log.e(TAG, "LuaOcr: ocr chinese text->"+text2);
					
				}
				
			    if(!StringUtil.isEmptyOrNull(text2)) {
					if((smililar_test&&text2.contains(ocr_text))|| ocr_text.contains(text2)){
						Log.e(TAG, "LuaOcr: "+ocr_text+" success");
						L.pushBoolean(true);
						L.pushString("success");
						cropBmp.recycle();
						cropBmp = null;
						return 2;
					}
				}
			}
			
			L.pushBoolean(false);
			L.pushString(cropBmp != null?"unmatched string":"crop bitmap failed.");
			if(cropBmp != null){
				cropBmp.recycle();
				cropBmp = null;
			}
			return 2;
		}else if(op_type == 2){ // ocr(2, int color, int x, int y, boolean similar_test, int diff) // 识别点颜色
			if (param_count < 5)
			{
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			long ocr_color = L.toInteger(3);
			int x = (int) L.toInteger(4);
			int y = (int) L.toInteger(5);
			if(x <0 || y<0){
				L.pushBoolean(false);
				L.pushString("bad params for ocr color");
				return 2;
			}
			
			Long color = getColor(bitmap, x, y);
			if(color == null){
				L.pushBoolean(false);
				L.pushString("getColor failed.");
				return 2;
			}
			
			Logger.e(TAG, String.format("ocr_color=0x%08x, color=0x%08x, (%d, %d)", ocr_color, color, x, y));
			boolean similar_test = false;
			if(param_count>=6 && L.isBoolean(6)){
				similar_test = L.toBoolean(6);
			}
			int diff = 100; // 150
			if(param_count>=7){
				diff = (int)L.toInteger(7);
			}
			if(similar_test) {
				long r1 = (ocr_color & 0xff0000) >> 16;
				long g1 = (ocr_color & 0xff00) >> 8;
				long b1 = ocr_color & 0xff;
				long r2 = (color & 0xff0000) >> 16;
				long g2 = (color & 0xff00) >> 8;
				long b2 = color & 0xff;
				long dr = r1 -r2;
				long dg = g1 -g2;
				long db = b1 -b2;
				long dd = (long) Math.sqrt(dr*dr + dg*dg + db* db);
				L.pushBoolean(dd<diff); // 150
				L.pushString(dd<diff?"success":"unmatched color");
				return 2;
			}else{
				//if(color==ocr_color) Log.e(TAG, String.format("LuaOcr: color 0x%08x success", color));
				boolean b = false;
				if(color==ocr_color) { b = true; }
				Logger.e(TAG, "ocr_color="+b);
				L.pushBoolean(b);
				L.pushString(b?String.valueOf(color):"unmatched color");
				return 2;
			}
		}else if(op_type == 3){ // ocr(3, int color, int left, int top, right, bottom) // 识别区域颜色
			if (param_count < 7)
			{
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			long ocr_color = L.toInteger(3);
			int x = (int) L.toInteger(4);
			int y = (int) L.toInteger(5);
			int x2 = (int) L.toInteger(6);
			int y2 = (int) L.toInteger(7);
			if(x <0 || y<0){
				L.pushBoolean(false);
				L.pushString("bad params for ocr color");
				return 2;
			}
			boolean found = false;
			for(int i=x; i<x2; i++){
				for(int j=y; j<y2;j++){
					long color = getColor(bitmap, i, j);
					if(color == ocr_color){
						found = true;
						break;
					}
				}
				if(found) break;
			}
			Logger.e(TAG, String.format("ocr_color=0x%08x, found=%s, (%d, %d, %d, %d)", ocr_color, found?"true":"false", x, y, x2, y2));

			L.pushBoolean(found);
			L.pushString(found?"success":"not found");
			return 2;
		}else if(op_type == 4){ //ocr(4, boolean save_package_image, string package_name_or_save_path, int left, int top, int right, int bottom) // 保存截图
			if (param_count < 4)
			{
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			boolean save_package_image = L.toBoolean(3);
			String package_name_or_save_path = L.toString(4);
			int left = -1, top = -1, right=-1, bottom = -1;
			boolean crop = false;
			if(param_count >= 8){
				left = (int)L.toInteger(5);
				top = (int)L.toInteger(6);
				right = (int)L.toInteger(7);
				bottom = (int)L.toInteger(8);
				if(left<0 || right<0 || top<0 || bottom < 0 || left >= right || top>=bottom){
					Logger.e(TAG, "bad coordinate: "+String.format("(%d,%d,%d,%d)", left, top, right, bottom));
					L.pushBoolean(false);
					L.pushString("bad coordinate: "+String.format("(%d,%d,%d,%d)", left, top, right, bottom));
					return 2;
				}

				crop = true;
			}
			if(StringUtil.isEmptyOrNull(package_name_or_save_path)){
				L.pushBoolean(false);
				L.pushString("bad param: "+package_name_or_save_path);
				return 2;
			}
			String filePath;
			if(save_package_image) {
				filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+
						"/tmp/"+save_package_image+"/"+DateUtil.getCurrentDate("yyyyMMdd_HHmmss")+".png";
			}else{
				filePath = package_name_or_save_path;
			}
		
			try {
				// rename 
				File file = new File(filePath);

				if(file.exists()){
	        		file.delete();
	        	}else{
	        		File d = new File(file.getParent());
	        		if(!d.exists()) d.mkdirs();
	        	}
	        	file.createNewFile();
		        FileOutputStream out = new FileOutputStream(file);
		        if(crop){
		        	Bitmap cropBmp  = Bitmap.createBitmap(bitmap, left, top, (right-left), (bottom-top));
		        	if(cropBmp == null){
		        		out.close();
		        		L.pushBoolean(true);
						L.pushString("cropBmp failed.");
						return 2;
		        	}
		        	cropBmp.compress(Bitmap.CompressFormat. PNG, 100, out);
		        }else{
		        	bitmap.compress(Bitmap.CompressFormat. PNG, 100, out);
		        }
		        out.close();
		        
				L.pushBoolean(true);
				L.pushString("save image success.");
				return 2;
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(op_type == 5){ //ocr(5, int x, int y) // 取颜色
			if (param_count < 4)
			{
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			int x = (int) L.toInteger(3);
			int y = (int) L.toInteger(4);
			if(x <0 || y<0){
				L.pushBoolean(false);
				L.pushString("bad params for ocr color");
				return 2;
			}
			Long color = getColor(bitmap, x, y);
			if(color == null){
				L.pushBoolean(false);
				L.pushString("getColor failed.");
				return 2;
			}
			L.pushBoolean(true);
			L.pushString(String.format("%08X", (long)color));
			L.pushInteger(color);
			return 3;
		}else if(op_type == 6){ // ocr(6, String vendor, String apikey, String codeType, int left, int top, int right, int bottom) // 第三方验证码识别接口
			if (param_count < 9)
			{
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String vendor = L.toString(3);
			String apikey = L.toString(4);
			String codeType = L.toString(5);
			int left = (int) L.toInteger(6);
			int top = (int) L.toInteger(7);
			int right = (int) L.toInteger(8);
			int bottom = (int) L.toInteger(9);
			if(left<0 || right<0 || top<0 || bottom < 0 || left >= right || top>=bottom){
				Logger.e(TAG, "bad coordinate: "+String.format("(%d,%d,%d,%d)", left, top, right, bottom));
				L.pushBoolean(false);
				L.pushString("bad coordinate: "+String.format("(%d,%d,%d,%d)", left, top, right, bottom));
				return 2;
			}
			String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+
						"/tmp/captcha.png";
			FileOutputStream out = null;
			try{
				// rename 
				File file = new File(filePath);
	
				if(file.exists()){
	        		file.delete();
	        	}else{
	        		File d = new File(file.getParent());
	        		if(!d.exists()) d.mkdirs();
	        	}
	        	file.createNewFile();
		        out = new FileOutputStream(file);
				Bitmap cropBmp  = Bitmap.createBitmap(bitmap, left, top, (right-left), (bottom-top));
	        	if(cropBmp == null){
	        		out.close();
	        		L.pushBoolean(true);
					L.pushString("cropBmp failed.");
					return 2;
	        	}
	        	cropBmp.compress(Bitmap.CompressFormat. PNG, 100, out);
	        	out.close();
			}catch(Exception e){
				e.printStackTrace();
				if(out!=null) try { out.close();}catch (Exception e2) {}
				L.pushBoolean(false);
				L.pushString("Exception : "+e);
				return 2;
			}
			if(vendor.equals("juhe")){
				String code = juheParseCaptcha(apikey, codeType, filePath);
				File file = new File(filePath);
				if(file.exists()){
	        		file.delete();
				}
				if(code !=null && code.length()>0) {
					L.pushBoolean(true);
					L.pushString(code);
					return 2;
				}else{
					L.pushBoolean(false);
					L.pushString("juheParseCaptcha failed.");
					return 2;
				}
			}else{
				L.pushBoolean(false);
				L.pushString("vendor unsupport: "+vendor);
				return 2;
			}

		}

		L.pushBoolean(false);
		L.pushString("failed");
		return 2;
	}
	
	private Long getColor(Bitmap bmp, int x, int y){
		try {
			return NumberUtil.getUnsignedInt(bitmap.getPixel(x, y));
		}catch(Exception e){
			Logger.e(TAG, "getColor Exception: "+ e);
			return null;
		}
	}
	
	//1.识别验证码 https://www.juhe.cn/docs/api/id/60
	/*
	 * {
		    "reason":"成功的返回",
		    "result":"00j8",
		    "error_code":0
		}
	 */
	public static String juheParseCaptcha(String apikey, String codeType, String filePath) {
		try{
			Logger.e(TAG, "juheParseCaptcha: codeType="+codeType+",filePath="+filePath);
			File file = new File(filePath);
			if(!file.exists()){
				Logger.e(TAG, "juheParseCaptcha: file not found!");
				return null;
			}
			OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(60, TimeUnit.SECONDS)  
		            .readTimeout(60, TimeUnit.SECONDS)  
		            .build();  
			MultipartBody.Builder builder = new MultipartBody.Builder();
			builder.setType(MultipartBody.FORM);
			builder.addFormDataPart("key", apikey);
			builder.addFormDataPart("codeType", codeType);
			builder.addFormDataPart("image", file.getName(),RequestBody.create(MediaType.parse("image/jpeg"), file));
			
			RequestBody formBody = builder.build();

			Request request = new Request.Builder()  
		            .url("http://op.juhe.cn/vercode/index")  
		            .post(formBody)
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    Logger.e(TAG, "juheParseCaptcha: response="+body);
		    JSONObject rspObject = new JSONObject(body);
		    int error_code = rspObject.optInt("error_code");
		    if(error_code==0){
		    	return rspObject.optString("result");
		    }

		}catch(Exception e){
			e.printStackTrace();
			Logger.e(TAG, "juheParseCaptcha: Exception="+e);
		}
		return null;
	}
	 
	

}

