package com.liblua;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Random;

import com.example.rowetalk.util.Config;
import com.example.rowetalk.util.Constant;
import com.example.rowetalk.util.FileUtil;
import com.example.rowetalk.util.Logger;
import com.example.rowetalk.util.SimpleLock;
import com.example.rowetalk.util.SleepObject;
import com.example.rowetalk.util.StringUtil;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaState;
import com.luajava.LuaStateFactory;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import android.view.SurfaceControl;

public class LuaEnv {
	private static final String TAG = LuaEnv.class.getName();
	private Context mContext;
	private static LuaEnv instance = null;
	private SimpleLock lock = new SimpleLock();
	private static final String TY_TAKE_SCREEN_SHOT = "com.ty.intent.take_screen_shot"; //hongbiao.zhang@2016.5.18: add

	public LuaState L;
	public String luaDir;
	public String luaCpath;
	public String luaPath;

	private String luaDirPattern;
	private boolean stopped = false;
	private boolean keepapp = false;
	private HashMap<String, String> paramsMap = new HashMap<String, String>();
	private SleepObject sleepObj;
	
	
	private StringBuilder prints = new StringBuilder();

	public static final int MSG_PRINT = 1000;

	

	public final static String SMS_SENT = "SMS_SENT";
	public final static String SMS_DELIVERED = "SMS_DELIVERED";
	
	public final static int RESULT_DEFAULT =  -9999;
	

	public TessBaseAPI mTessBaseAPIChi, mTessBaseAPIEng;
	public Random mRandom = new Random();
	
	
	private Matrix mDisplayMatrix;
	private WindowManager mWindowManager;
	private DisplayMetrics mDisplayMetrics = new DisplayMetrics();
	
	
	private int mImeWaitTime = 0;
	
	public interface ThreadCallback {
		public void onBegin(String name);
		public void onDone(boolean b);
	};
	
	private LuaEnv(Context c) {
		mContext = c;
	    mDisplayMatrix = new Matrix();
    	mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
    	mRandom.setSeed(System.currentTimeMillis());
    	
    	// init dir
		luaDir = Constant.getScriptPath();
		luaCpath = mContext.getApplicationInfo().nativeLibraryDir + "/lib?.so" + ";"
				+ mContext.getDir("lib", Context.MODE_PRIVATE).getAbsolutePath() + "/lib?.so";
		//File destDir = new File(luaDir);
		//if (!destDir.exists())
		//	destDir.mkdirs();
		copyAssetLuaFiles(luaDir+"/lua");
		try {
			initLua();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.e(TAG, "initEnv: Exception: " + e);
		}
	}

	private void showToast(String s) {
		if (!StringUtil.isEmptyOrNull(s))
		Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
	}

	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// super.handleMessage(msg);
			switch (msg.what) {
			case MSG_PRINT:
				showToast((String) msg.obj);
				break;
			}
		}
		
	};

	
	// 初始化lua使用的Java函数
	private void initLua() throws Exception {
		L = LuaStateFactory.newLuaState();
		L.openLibs();
		L.pushJavaObject(this);
		L.setGlobal("activity");

		L.getGlobal("luajava");
		L.pushString(luaDir);
		L.setField(-2, "luadir");
		L.pushString(luaPath);
		L.setField(-2, "luapath");
		L.pop(1);
		
		JavaFunction print = new LuaPrint(this, L);
		print.register("print");
		
		JavaFunction sendsms = new LuaSendSms(this, L);
		sendsms.register("sendsms");
		
		JavaFunction recvsms = new LuaRecvSms(this, L);
		recvsms.register("recvsms"); // number, waitMs, flag, codeLen
		
		JavaFunction thread_sleep = new LuaThreadSleep(this, L);
		thread_sleep.register("thread_sleep");
		
		JavaFunction java_exec = new LuaJavaExec(this, L);
		java_exec.register("java_exec");
		
		JavaFunction exe = new LuaExe(this, L);
		exe.register("exe");
		
		JavaFunction ocr = new LuaOcr(this, L);
		ocr.register("ocr");
		
		JavaFunction layout_dump = new LuaLayoutDump(this, L);
		ocr.register("layout_dump");

		JavaFunction assetLoader = new LuaAssetLoader(this, L);

		L.getGlobal("package");
		L.getField(-1, "loaders");
		int nLoaders = L.objLen(-1);
		for (int i = nLoaders; i >= 2; i--) {
			L.rawGetI(-1, i);
			L.rawSetI(-2, i + 1);
		}
		L.pushJavaFunction(assetLoader);
		L.rawSetI(-2, 2);
		L.pop(1);

		luaDirPattern = luaDir + "/?.lua;" + luaDir + "/lua/?.lua;" + luaDir + "/?/init.lua;" + luaDir + "/task/?.lua;";
		L.pushString(luaDirPattern);
		L.setField(-2, "path");
		L.pushString(luaCpath);
		L.setField(-2, "cpath");
		L.pop(1);
		/*
		 * JavaFunction task = new newLuaAsyncTask(L); task.register("task");
		 * 
		 * 
		 * JavaFunction thread = new newLuaThread(L); thread.register("thread");
		 */
		JavaFunction set = new JavaFunction(L) {
			@Override
			public int execute() throws LuaException {
				LuaThread thread = (LuaThread) L.toJavaObject(2);

				thread.set(L.toString(3), L.toJavaObject(4));
				return 0;
			}
		};
		set.register("set");

		JavaFunction call = new JavaFunction(L) {
			@Override
			public int execute() throws LuaException {
				LuaThread thread = (LuaThread) L.toJavaObject(2);

				int top = L.getTop();
				if (top > 3) {
					Object[] args = new Object[top - 3];
					for (int i = 4; i <= top; i++) {
						args[i - 4] = L.toJavaObject(i);
					}
					thread.call(L.toString(3), args);
				} else if (top == 3) {
					thread.call(L.toString(3));
				}

				return 0;
			};
		};
		call.register("call");
	}

	public void onException(String title, Exception e) {
		Logger.e(TAG, "onException: title="+title+",e="+e);
	}

	public boolean isStopped() {
		return stopped;
	}
	public boolean isKeepapp() {
		return keepapp;
	}

	public void setStopped(boolean stopped, boolean keepapp) {
		this.stopped = stopped;
		this.keepapp = keepapp;
	}

	// 运行lua脚本
	public boolean doFile(String filePath) {
		Logger.e(TAG, "doFile: "+filePath);
		int ok = 0;
		stopped = false;
		L.setTop(0);
		String dirPath = FileUtil.getFileDir(filePath);
		
		ok = L.LloadFile(filePath);
		
		if (ok == 0) {
			L.getGlobal("debug");
			L.getField(-1, "traceback");
			L.remove(-2);
			L.insert(-2);
			ok = L.pcall(0, 0, -2);
			if (ok == 0) {
				// setResult(ok);
				return true;
			}
		}
		return false;
	}
	
	public void doAssetInThread(final String name, final ThreadCallback callback){
		Thread t = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(callback != null){
					callback.onBegin(name);
				}
				boolean b = doAsset(name) ;
				if(callback != null){
					callback.onDone(b);
				}
			}
			
		});
		//t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	public boolean doAsset(String name) {
		int ok = 0;
		try {
			byte[] bytes = readAsset(name);
			L.setTop(0);
			ok = L.LloadBuffer(bytes, name);

			if (ok == 0) {
				L.getGlobal("debug");
				L.getField(-1, "traceback");
				L.remove(-2);
				L.insert(-2);
				ok = L.pcall(0, 0, -2);
				if (ok == 0) {
					return true;
				}
			}
		} catch (Exception e) {
			onException("doAsset:"+name, e);
		}
		return false;
	}

	// 运行lua函数
	private Object runFunc(String funcName, Object... args) {
		try {
			L.setTop(0);
			L.getGlobal(funcName);
			if (L.isFunction(-1)) {
				L.getGlobal("debug");
				L.getField(-1, "traceback");
				L.remove(-2);
				L.insert(-2);

				int l = args.length;
				for (int i = 0; i < l; i++) {
					L.pushObjectValue(args[i]);
				}

				int ok = L.pcall(l, 1, -2 - l);
				if (ok == 0) {
					return L.toJavaObject(-1);
				}
				throw new LuaException(errorReason(ok) + ": " + L.toString(-1));
			}
		} catch (LuaException e) {
			onException("runFunc: "+funcName, e);
		}
		return false;
	}

	// 运行lua代码
	private boolean doString(String funcSrc, Object... args) {
		try {
			L.setTop(0);
			int ok = L.LloadString(funcSrc);

			if (ok == 0) {
				L.getGlobal("debug");
				L.getField(-1, "traceback");
				L.remove(-2);
				L.insert(-2);

				int l = args.length;
				for (int i = 0; i < l; i++) {
					L.pushObjectValue(args[i]);
				}

				ok = L.pcall(l, 1, -2 - l);
				if (ok == 0) {
					return true;
				}
			}
		} catch (LuaException e) {
			onException("doString: "+funcSrc, e);
		}
		return false;
	}

	// 生成错误信息
	private String errorReason(int error) {
		switch (error) {
		case 4:
			return "Out of memory";
		case 3:
			return "Syntax error";
		case 2:
			return "Runtime error";
		case 1:
			return "Yield error";
		}
		return "Unknown error " + error;
	}

	// 读取asset文件
	public byte[] readAsset(String name) throws IOException {
		AssetManager am = mContext.getAssets();
		InputStream is = am.open(name);
		byte[] ret = readAll(is);
		is.close();
		// am.close();
		return ret;
	}

	//复制asset文件到sd卡
	public void assetsToSD(String InFileName, String OutFileName) throws IOException 
	{  
		InputStream myInput;  
		OutputStream myOutput = new FileOutputStream(OutFileName);  
		myInput = mContext.getAssets().open(InFileName);  
		byte[] buffer = new byte[8192];  
		int length = myInput.read(buffer);
        while (length > 0)
        {
			myOutput.write(buffer, 0, length); 
			length = myInput.read(buffer);
		}

        myOutput.flush();  
		myInput.close();  
		myOutput.close();        
	}  
	private static byte[] readAll(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
		byte[] buffer = new byte[4096];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
		byte[] ret = output.toByteArray();
		output.close();
		return ret;
	}

	private void setField(String key, Object value) {
		try {
			L.pushObjectValue(value);
			L.setGlobal(key);
		} catch (LuaException e) {
			print("setField Exception: " + e.getMessage());
		}
	}

	public void call(String func) {
		push(2, func);

	}

	public void call(String func, Object[] args) {
		if (args.length == 0)
			push(2, func);
		else
			push(3, func, args);
	}

	public void set(String key, Object value) {
		push(1, key, new Object[] { value });
	}

	public Object get(String key) throws LuaException {
		L.getGlobal(key);
		return L.toJavaObject(-1);
	}

	public void push(int what, String s) {
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("data", s);
		message.setData(bundle);
		message.what = what;

		mHandler.sendMessage(message);

	}

	public void push(int what, String s, Object[] args) {
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("data", s);
		bundle.putSerializable("args", args);
		message.setData(bundle);
		message.what = what;

		mHandler.sendMessage(message);

	}

	public void print(String s) {
		synchronized (prints) {
			prints.append(s).append("\n");
		}
		Logger.e(TAG, "[script]: "+s);
	}
	
	public void clearPrints(){
		synchronized (prints) {
			prints.setLength(0);
		}
	}
	public String getPrints(){
		synchronized (prints) {
			return prints.toString();
		}
	}
	
	public Context getContext() { return mContext; }
	
	public int wait(String name, long ms){
		try{
			synchronized(lock) {
				lock.name = name;
				lock.result = RESULT_DEFAULT;
				lock.obj = null;
				lock.wait(ms);
				return lock.result;
			}
		}catch(InterruptedException e){
			
		}
		return -1;
	}
	
	public Object getNotifyObject() {
		synchronized(lock) {
			return lock.obj;
		}
	}
	
	public void notify(String name, int result){
		notify(name, result, null);
	}
	
	public void notify(String name, int result, Object obj){
		synchronized(lock) {
			if(lock.name.equals(name)){
				lock.result = result;
				lock.name= "";
				lock.obj = obj;
				lock.notifyAll();
			}
		}
	}
	
	public void cancelWaitLocks(){
		synchronized(lock) {
			lock.notifyAll();
		}
	}

	public static void init(Context c) {
		if (instance == null) {
			instance = new LuaEnv(c);
		}
	}

	public static LuaEnv getInstance() {
		
		return instance;
	}
	
	private float getDegreesForRotation(int value) {
        switch (value) {
        case Surface.ROTATION_90:
            return 360f - 90f;
        case Surface.ROTATION_180:
            return 360f - 180f;
        case Surface.ROTATION_270:
            return 360f - 270f;
        }
        return 0f;
    }
	
	public Bitmap takeScreenshot() {
    	// Inflate the screenshot layout
    	Display mDisplay = mWindowManager.getDefaultDisplay();
        mDisplay.getRealMetrics(mDisplayMetrics);
        
        // We need to orient the screenshot correctly (and the Surface api seems to take screenshots
        // only in the natural orientation of the device :!)
        mDisplay.getRealMetrics(mDisplayMetrics);
        float[] dims = {mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels};
        
        /// @}
        float degrees = getDegreesForRotation(mDisplay.getRotation());
        Log.e(TAG, "takeScreenshot: dims = " + dims[0] + "," + dims[1] + " of " + degrees);
        boolean requiresRotation = (degrees > 0);
        if (requiresRotation) {
            // Get the dimensions of the device in its native orientation
            mDisplayMatrix.reset();
            mDisplayMatrix.preRotate(-degrees);
            mDisplayMatrix.mapPoints(dims);
            dims[0] = Math.abs(dims[0]);
            dims[1] = Math.abs(dims[1]);
            Log.e(TAG, "takeScreenshot reqRotate, dims = " + dims[0] + "," + dims[1]);
        }

        // Take the screenshot
        Bitmap bmp = SurfaceControl.screenshot((int) dims[0], (int) dims[1]);

        if (bmp == null) {
        	Log.e(TAG, "takeScreenshot mScreenBitmap == null, " + dims[0] + "," + dims[1]);
            return null;
        }

        if (requiresRotation) {
            // Rotate the screenshot to the current orientation
            Bitmap ss = Bitmap.createBitmap(mDisplayMetrics.widthPixels,
                    mDisplayMetrics.heightPixels, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(ss);
            c.translate(ss.getWidth() / 2, ss.getHeight() / 2);
            c.rotate(degrees);
            c.translate(-dims[0] / 2, -dims[1] / 2);
            c.drawBitmap(bmp, 0, 0, null);
            c.setBitmap(null);
            // Recycle the previous bitmap
            bmp.recycle();
            bmp = ss;
        }

        // Optimizations
        bmp.setHasAlpha(false);
        bmp.prepareToDraw();
        return bmp;
        /*
        try {
        	File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/tmp/test.png");
        	if(file.exists()){
        		file.delete();
        	}else{
        		File d = new File(file.getParent());
        		if(!d.exists()) d.mkdirs();
        		
        	}
        	file.createNewFile();
	        FileOutputStream out = new FileOutputStream(file);
	        mScreenBitmap.compress(Bitmap.CompressFormat. PNG, 100, out);
	           
        } catch (Exception e) {
        }    */   
    }

	public void clearParams() {
		synchronized (paramsMap) {
			paramsMap.clear();
		}
	}
	public boolean containsParam(String k){
		synchronized (paramsMap) {
			return paramsMap.containsKey(k);
		}
	}
	public String getParam(String k){
		synchronized (paramsMap) {
			return paramsMap.get(k);
		}
	}
	public void putParam(String k, String v){
		synchronized (paramsMap) {
			paramsMap.put(k, v);
		}
	}

	public int getImeWaitTime() {
		return mImeWaitTime;
	}

	public void setImeWaitTime(int mImeWaitTime) {
		this.mImeWaitTime = mImeWaitTime;
	}

	
	private void copyAssetLuaFiles(String destinationPath) {
		File dir = new File(destinationPath);
		if(!dir.exists()){
			copyAssetFileOrDir(mContext.getAssets(), "", destinationPath);
		}
	}
	private void copyAssetFileOrDir(AssetManager assetManager, String path, String destinationPath){
		try {
			String str[] = assetManager.list(path);
			if (str.length > 0) {//如果是目录
				Log.e(TAG, "copyAssetDir: "+path+" => "+destinationPath+"/"+path);
				File file = new File(destinationPath+"/"+path);
				file.mkdirs();
				for (String string : str) {
					String src = path+(path.length()!=0?"/":"")+string;
					if(!src.equals("images")&&!src.equals("sounds")&&!src.equals("webkit")){
						copyAssetFileOrDir(assetManager, src, destinationPath);
					}
				}
			} else {//如果是文件
				Log.e(TAG, "copyAssetFile: "+path+" => "+destinationPath);
				InputStream is = assetManager.open(path);
				File file = new File(destinationPath+"/"+path);
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[1024];
				int count = 0;
				while (true) {
					count++;
					int len = is.read(buffer);
					if (len == -1) {
						break;
					}
					fos.write(buffer, 0, len);
				}
				is.close();
				fos.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean initOcr(){
		if(Config.getBoolean("ocr_chinese", true)){
			mTessBaseAPIChi = new TessBaseAPI();
			mTessBaseAPIChi.init("/system/etc/tesseract/", "chi_sim");//"eng");
		}
		if(Config.getBoolean("ocr_english", true)){
			mTessBaseAPIEng = new TessBaseAPI();
			mTessBaseAPIEng.init("/system/etc/tesseract/", "eng");//"eng");
		}
		return true;
	}
	
	public boolean deinitOcr(){
		if(mTessBaseAPIChi != null){
			try{
				mTessBaseAPIChi.clear();
				mTessBaseAPIChi.end();
			}catch(Exception e){
				e.printStackTrace();
			}
			mTessBaseAPIChi = null;
		}
		if(mTessBaseAPIEng != null){
			try{
				mTessBaseAPIEng.clear();
				mTessBaseAPIEng.end();
			}catch(Exception e){
				e.printStackTrace();
				
			}
			mTessBaseAPIEng = null;
		}
		return true;
	}
	
	public SleepObject getSleepObject() { return sleepObj; }

	public boolean sleep(long ms){
		if(sleepObj != null){
			return sleepObj.sleep(ms);
		}
		return false;
	}

	public boolean startScript(SleepObject obj){
		this.sleepObj = obj;
		clearParams();
		return true;
	}
	
	
	public boolean endScript(boolean keep_app_state){
		putParam("next_step", "true");
		setStopped(true, keep_app_state);
		if(sleepObj != null){
			sleepObj.wake();
		}
		cancelWaitLocks();
		return true;
	}
}
