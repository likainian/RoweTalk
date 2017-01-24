package com.example.rowetalk.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

public class SystemUtil {
	private static final String TAG = SystemUtil.class.getName();
	// 获得可用的内存
    public static long getmem_UNUSED(Context mContext) {
        long MEM_UNUSED;
	// 得到ActivityManager
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

	// 创建ActivityManager.MemoryInfo对象  

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

	// 取得剩余的内存空间 

        MEM_UNUSED = mi.availMem / 1024;
        return MEM_UNUSED;
    }

    // 获得总内存
    public static long getmem_TOLAL() {
        long mTotal=0;
        // /proc/meminfo读出的内核信息进行解释
        String path = "/proc/meminfo";
        String content = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path), 8);
            String line;
            if ((line = br.readLine()) != null) {
                content = line;
            }
            // beginIndex
            int begin = content.indexOf(':');
            // endIndex
            int end = content.indexOf('k');
            // 截取字符串信息

            content = content.substring(begin + 1, end).trim();
            mTotal = Integer.parseInt(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
       
        return mTotal;
    }
    
    public static boolean checkApkInstalled(Context context, String package_name) {
		PackageManager packageManager = context.getPackageManager();
		try {
            // 通过packageInfo即可获取AndroidManifest.xml中的信息。
			PackageInfo packageInfo  = packageManager.getPackageInfo(package_name, 0);
			return packageInfo != null;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
		return false;
	}
    
    public static int checkApkInstalled(Context context, String package_name, int verCode){
    	PackageManager pm = context.getPackageManager();
    	List<PackageInfo> pakageinfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
    	for (PackageInfo packageInfo : pakageinfos) {
    		if((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)!=0 ||
    				(packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)!=0){
    			continue;
    		}
    			
    		if(packageInfo.packageName.equals(package_name)){
    			return packageInfo.versionCode != verCode?1:0;
    		}
    	}
    	return -1;
    }
    
    public static boolean isSystemApp(PackageInfo pInfo) {  
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);  
    }  
  
    public static boolean isSystemUpdateApp(PackageInfo pInfo) {  
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);  
    }  
  
    public static boolean isUserApp(PackageInfo pInfo) {  
        return (!isSystemApp(pInfo) && !isSystemUpdateApp(pInfo));  
    }  
    public static void disableAppStaticReceivers(Context context, String packageName){
		//final ComponentName receiver = new ComponentName(mContext,需要禁止的receiver); 
		PackageManager pm = context.getPackageManager(); 
		PackageInfo currInfo = getPackageInfo(context);
		if(currInfo == null) return;
		if (!isSystemApp(currInfo) && !isSystemUpdateApp(currInfo)) { 
			Logger.e(TAG, "disableAppStaticReceivers: failed. i'm not a systam app.");
			return;
		}
		PackageInfo packageInfo = null;
		try {
            // 通过packageInfo即可获取AndroidManifest.xml中的信息。
		    packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_RECEIVERS);
		    if(packageInfo != null && packageInfo.receivers != null) {
				for(ActivityInfo info: packageInfo.receivers){
					pm.setComponentEnabledSetting(
							new ComponentName(packageName, info.name),
							PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
							PackageManager.DONT_KILL_APP); 				
				}
			}
        } catch (Exception e) {
        	Logger.e(TAG, "disableAppStaticReceivers: Exception: "+e);
        }
		
	}
    
    /*
	public static boolean isSdCardExist() {  
	    return Environment.getExternalStorageState().equals(  
	            Environment.MEDIA_MOUNTED);  
	} 
	*/ 
	
	public static String getSdCardPath() { 
	    //if(isSdCardExist()) {  
	        return Environment.getExternalStorageDirectory()  
	                .getAbsolutePath();  
	    //}
	}  
	
	public static PackageInfo getPackageInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        PackageInfo packageInfo = null;
        // flags提供了10种选项，及其组合，如果只是获取版本号，flags=0即可
        int flags = 0;
        try {
            // 通过packageInfo即可获取AndroidManifest.xml中的信息。
            packageInfo = packageManager.getPackageInfo(packageName, flags);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }
	
	public static PackageInfo getPackageInfo(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        //String packageName = context.getPackageName();
        PackageInfo packageInfo = null;
        // flags提供了10种选项，及其组合，如果只是获取版本号，flags=0即可
        int flags = 0;
        try {
            // 通过packageInfo即可获取AndroidManifest.xml中的信息。
            packageInfo = packageManager.getPackageInfo(packageName, flags);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }
	
    public static ComponentName getForegroundActivity(Context context) {  
        ActivityManager mActivityManager =  
            (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);  
            if (mActivityManager.getRunningTasks(1) == null) {  
                Logger.e(TAG, "running task is null, ams is abnormal!!!");  
                return null;  
            }  
            ActivityManager.RunningTaskInfo mRunningTask =  
                        mActivityManager.getRunningTasks(1).get(0);  
            if (mRunningTask == null) {  
                Logger.e(TAG, "failed to get RunningTaskInfo");  
                return null;  
            }  
       
            //String pkgName = mRunningTask.topActivity.getPackageName();  
            //String activityName =  mRunningTask.topActivity.getClassName();  
            return mRunningTask.topActivity;  
    } 
    
    public static boolean isTyImePresent(Context context){
		String ss=Settings.Secure.getString(context.getContentResolver(),
				Settings.Secure.DEFAULT_INPUT_METHOD);
		return ss!=null&& ss.equals("com.ty.ime/.LatinIME");
	}
    public static boolean isTyImeEnabled(Context context){
    	InputMethodManager mInputManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
    	List<InputMethodInfo> list = mInputManager.getEnabledInputMethodList();
		for(InputMethodInfo info: list){
			if(info.getServiceName().equals("com.ty.ime.LatinIME")){
				return true;
			}
		}
		return false;
    }
	
}
