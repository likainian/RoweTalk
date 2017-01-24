package com.example.rowetalk.util;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class Config  {
	private static final String TAG = Config.class.getName();
	private static Config instance;
	private SharedPreferences settings;
	
	private void initInternal(Context context){
		settings = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
	}
	
	public static void init(Context context){
		if(instance == null){
			instance = new Config();
			instance.initInternal(context);
		}
	}
	public static boolean getBoolean(String key, boolean defValue){
		return instance.settings.getBoolean(key, defValue);
	}
	public static int getInt(String key, int defValue){
		return instance.settings.getInt(key, defValue);
	}
	
	public static long getLong(String key, long defValue){
		return instance.settings.getLong(key, defValue);
	}
	
	public static String getString(String key, String defValue){
		return instance.settings.getString(key, defValue);
	}
	
	public static boolean remove(String key){
		SharedPreferences.Editor e = instance.settings.edit();
		e.remove(key);
		return e.commit();
	}
	public static boolean putBoolean(String key, boolean v){
		SharedPreferences.Editor e = instance.settings.edit();
		e.putBoolean(key, v);
		return e.commit();
	}
	public static boolean putInt(String key, int v){
		SharedPreferences.Editor e = instance.settings.edit();
		e.putInt(key, v);
		return e.commit();
	}
	
	public static boolean putLong(String key, long v){
		SharedPreferences.Editor e = instance.settings.edit();
		e.putLong(key, v);
		return e.commit();
	}
	
	public static boolean putString(String key, String v){
		SharedPreferences.Editor e = instance.settings.edit();
		e.putString(key, v);
		return e.commit();
	}
	
	public static boolean putFloat(String key, float v){
		SharedPreferences.Editor e = instance.settings.edit();
		e.putFloat(key, v);
		return e.commit();
	}
}
