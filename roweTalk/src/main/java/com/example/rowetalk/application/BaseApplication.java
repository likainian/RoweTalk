package com.example.rowetalk.application;

import com.example.rowetalk.util.Config;
import com.example.rowetalk.util.CrashHandler;
import com.liblua.LuaEnv;

import android.app.Application;

public class BaseApplication extends Application {
	

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Config.init(this);
		LuaEnv.init(this);
		CrashHandler handler = CrashHandler.getInstance();  
        handler.init(getApplicationContext());  
        Thread.setDefaultUncaughtExceptionHandler(handler);  
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

}
