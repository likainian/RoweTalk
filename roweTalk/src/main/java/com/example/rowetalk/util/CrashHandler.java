package com.example.rowetalk.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.rowetalk.activity.MainActivity;
import com.example.rowetalk.util.HttpUtil.ProgressHandler;
import com.example.rowetalk.util.HttpUtil.ResponseHandler;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;

public class CrashHandler implements UncaughtExceptionHandler {
	private static final String TAG = CrashHandler.class.getName();
	// 需求是 整个应用程序 只有一个 MyCrash-Handler   
    private static CrashHandler myCrashHandler ;  
    private Context mContext;  
    private SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");  
      
    //1.私有化构造方法  
    private CrashHandler(){  
          
    }  
      
    public static synchronized CrashHandler getInstance(){  
        if(myCrashHandler!=null){  
            return myCrashHandler;  
        }else {  
            myCrashHandler  = new CrashHandler();  
            return myCrashHandler;  
        }  
    }  
    public void init(Context context){  
    	mContext = context;  
    }  
    
       
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		Logger.e(TAG, "uncaughtException： "+ex);  
        // 1.获取当前程序的版本号. 版本的id  
		final PackageInfo info = SystemUtil.getPackageInfo(mContext);
          
        // 2.获取手机的硬件信息.  
        final String mobileInfo  = getMobileInfo();  
          
        // 3.把错误的堆栈信息 获取出来   
        final String errorinfo = getErrorInfo(ex);  
          
        // 4.把所有的信息 还有信息对应的时间 提交到服务器   
        if(info != null){
        	new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Intent i = new Intent("ty.intent.action.restart_app");
					i.putExtra("package_name", info.packageName);
					i.putExtra("launch_activity", "com.example.rowetalk.activity.MainActivity");
					mContext.sendBroadcast(i);
					String filePath = saveLogFile(mobileInfo, errorinfo);
					if(filePath != null){
						boolean b = send_crash_log(info, filePath);
			        	Logger.e(TAG, "send_crash_log="+b);
					}
		        	
		        	//干掉当前的程序   
			        //android.os.Process.killProcess(android.os.Process.myPid());  
				}
			}).start();
        	
        }else {
	        //干掉当前的程序   
        	Intent i = new Intent("ty.intent.action.restart_app");
			i.putExtra("package_name", info.packageName);
			mContext.sendBroadcast(i);
	        android.os.Process.killProcess(android.os.Process.myPid());  
        }
	}
	
	private boolean send_crash_log(PackageInfo info, String filePath) {
		
		if(!NetworkUtil.isNetworkConnected(mContext)) {
			Logger.e(TAG, "send_crash_log failed, network not connected.");
			return false;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("package_name", info.packageName);
		params.put("version_name", info.versionName);
		params.put("version_code", info.versionCode);
		params.put("phone_seq", MainActivity.mPhoneSeq);
		params.put("crashTime", new Date().getTime());
		
		for(int i=0; i<3; i++) {
			boolean b = HttpUtil.uploadFile(Constant.getURL_SEND_CRASH_LOG(), params, "logfile", filePath, 
				new ProgressHandler() {
					
					@Override
					public void updateSize(long size) {
						// TODO Auto-generated method stub
						Logger.e(TAG, "send_crash_log: updateSize "+size);
					}
				}, 
				new ResponseHandler(){
	
					@Override
					public boolean onFailure(int statusCode, String responseString) {
						Logger.e(TAG, "send_crash_log: onFailure "+statusCode+" "+responseString);
						return false;
					}
	
					@Override
					public boolean onSuccess(String responseString) {
						try {
							JSONObject response = new JSONObject(responseString);
							String errorNo = response.optString("errorNo");
				            Logger.e(TAG, "send_crash_log: response erroNo=" + errorNo);
				            if (errorNo.equals("0")) {
				            	return true;
				            }
						}catch (JSONException e) {
							// TODO Auto-generated catch block
							Logger.e(TAG, "send_crash_log: onSuccess: Exception: "+e);
						}
						return false;
					}
			});
			if(b){
				return b;
			}else {
				ThreadSleep.Sleep(1000);
			}
		}
		return false;
	}

	private String saveLogFile(final String mobileInfo, final String errorInfo){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			String logdir = Environment.getExternalStorageDirectory().getPath()+"/logs/";
			String logfile = logdir + "/crash_log.txt";
			try {
				File d = new File(logdir);
				if(!d.exists()) {
	            	d.mkdirs();
	            }
				File f = new File(logfile);
				f.createNewFile();
				FileWriter filerWriter = new FileWriter(f, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
				filerWriter.write(DateUtil.simpleDateFormatLong.format(new Date())+"\n");
	            //filerWriter.write(mobileInfo+"\n");
	            filerWriter.write(errorInfo+"\n");
	            filerWriter.flush();
	            filerWriter.close();
	            return logfile;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return null;
	}
	

	/** 
     * 获取错误的信息  
     * @param arg1 
     * @return 
     */  
    private String getErrorInfo(Throwable arg1) {  
        Writer writer = new StringWriter();  
        PrintWriter pw = new PrintWriter(writer);  
        arg1.printStackTrace(pw);  
        pw.close();  
        String error= writer.toString();  
        return error;  
    }  
  
    /** 
     * 获取手机的硬件信息  
     * @return 
     */  
    private String getMobileInfo() {  
        StringBuffer sb = new StringBuffer();  
        //通过反射获取系统的硬件信息   
        try {
            Field[] fields = Build.class.getDeclaredFields();  
            for(Field field: fields){  
                //暴力反射 ,获取私有的信息   
                field.setAccessible(true);  
                String name = field.getName();  
                String value = field.get(null).toString();  
                sb.append(name+"="+value);  
                sb.append("\n");  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return sb.toString();  
    }  
  
    
}
