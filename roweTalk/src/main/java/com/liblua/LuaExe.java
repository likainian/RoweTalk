package com.liblua;


import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.example.rowetalk.bean.Device;
import com.example.rowetalk.bean.EnvProfile;
import com.example.rowetalk.bean.Platform;
import com.example.rowetalk.bean.VpnAccount;
import com.example.rowetalk.bean.VpnProfile;
import com.example.rowetalk.bean.WebAccount;
import com.example.rowetalk.bean.WebAccountDetail;
import com.example.rowetalk.thread.BaseThread;
import com.example.rowetalk.util.Config;
import com.example.rowetalk.util.Constant;
import com.example.rowetalk.util.FileUtil;
import com.example.rowetalk.util.HttpUtil;
import com.example.rowetalk.util.Logger;
import com.example.rowetalk.util.NameUtil;
import com.example.rowetalk.util.NetworkUtil;
import com.example.rowetalk.util.PlatformUtil;
import com.example.rowetalk.util.ServerUtil;
import com.example.rowetalk.util.StringUtil;
import com.example.rowetalk.util.SystemUtil;
import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaState;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LuaExe extends JavaFunction
{
	private static final String TAG = LuaExe.class.getName();
	private LuaState L;
	private LuaEnv Env;
	

    private static final String IME_MESSAGE = "ADB_INPUT_TEXT";
    private static final String IME_CHARS = "ADB_INPUT_CHARS";
    private static final String IME_KEYCODE = "ADB_INPUT_CODE";
    private static final String IME_EDITORCODE = "ADB_EDITOR_CODE";
	
	public LuaExe(LuaEnv Env, LuaState L)
	{
		super(L);
		this.L = L;
		this.Env = Env;
	}

	// exec(name, param1, param2, ...)
	@Override
	public int execute() throws LuaException
	{
		int count = L.getTop();
		if (count < 2)
		{
			L.pushBoolean(false);
			L.pushString("bad parameter count");
			return 2;
		}
		// set prop
		String prop = L.toString(2);
		
		if(prop.equals("rand")){
			long param = 0;
			if(count > 2){
				param = L.toInteger(3);
			}
			int result =  0;
			if (param > 0){
				result = Env.mRandom.nextInt((int)param);
			}else{
				result = Env.mRandom.nextInt();
			}
			Logger.e(TAG, "exe: rand( "+(param==0?"":param)+")="+result);
			L.pushInteger(result);
			return 1;
		}else if(prop.equals("getparam")) {
			if (count < 3){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String param = L.toString(3);
			boolean b = Env.containsParam(param);
			String result = b?Env.getParam(param):"";
			L.pushBoolean(b);
			L.pushString(result);
			if(b && param.equals("next_step")){
				Env.putParam(param, "false");
			}

			Logger.e(TAG, "exe: "+String.format("getparam(%s)=%s",param, result));
			return 2;
		}else if(prop.equals("getprop")) {
			if (count < 3){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String value = L.toString(3);
			String result = null;
			if(value.equals("imei")){
			    result = NetworkUtil.getImei(Env.getContext());
			}else if(value.equals("imsi")){
				result = NetworkUtil.getImsi(Env.getContext());
			}else if(value.equals("btMac")){
				result = NetworkUtil.getBtMacAddr(Env.getContext());
			}else if(value.equals("wifiMac")){
				result = NetworkUtil.getWifiMacAddr(Env.getContext());
			}else if(value.equals("wifi_state")) {
				final ConnectivityManager cm = (ConnectivityManager) Env.getContext()
		                .getSystemService(Context.CONNECTIVITY_SERVICE);
		        NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		        if (networkInfo != null && networkInfo.isAvailable()) {
		        	result = networkInfo.isConnected()?"connected":"disconnected";
		        }else {
		        	result = "closed";
		        }
			}else if(value.equals("mobile_state")) {
				final ConnectivityManager cm = (ConnectivityManager) Env.getContext()
		                .getSystemService(Context.CONNECTIVITY_SERVICE);
		        NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		        if (networkInfo != null && networkInfo.isAvailable()) {
		        	result = networkInfo.isConnected()?"connected":"disconnected";
		        }else {
		        	result = "closed";
		        }  
			}
			Logger.e(TAG, "exe: "+String.format("getprop(%s)=%s",value, result));
			if(result == null){
				L.pushBoolean(false);
				L.pushString("getprop failed.");
				return 2;
			}else{
				L.pushBoolean(true);
				L.pushString(result);
				return 2;
			}
	    }else if(prop.equals("wifi_switch")) {
			if (count < 3){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String value = L.toString(3);
			boolean b = NetworkUtil.switchWiFi(Env.getContext(), value.equals("on"), Env.getSleepObject());
			Logger.e(TAG, "exe: "+String.format("wifi_switch(%s)=%s",value, b?"success":"failed"));
			L.pushBoolean(b);
			L.pushString("");
			return 2;
		}else if(prop.equals("mobile_switch")) {
			if (count < 3){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String value = L.toString(3);
			boolean b = PlatformUtil.switch4G(Env.getContext(), value.equals("on"), Env.getSleepObject());
			Logger.e(TAG, "exe: "+String.format("mobile_switch(%s)=%s",value, b?"success":"failed"));
			L.pushBoolean(b);
			L.pushString("");
			return 2;
		}else if(prop.equals("input_chinese")) { // input_chinese string, go, auto_wait
			if (count < 3){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String value = L.toString(3);
			boolean auto_wait = true;
			boolean go = false;
			if(count > 3){
				go = L.toBoolean(4);
			}
			if(count > 4){
				auto_wait = L.toBoolean(5);
			}
			//boolean b = RootShellCmd.getInstance().inputTyText(value, go, auto_wait);
			
			Intent intent = new Intent(IME_MESSAGE);
			intent.putExtra("msg", value);
			if(auto_wait){
				if(!Env.sleep(value.length()*600)) { // each char 0.6s
					L.pushBoolean(false);
					L.pushString("Env.sleep failed.");
					return 2;
				}
			}
			boolean b = false;
			Env.getContext().sendBroadcast(intent);
			if(Env.getImeWaitTime()>0) {
				Logger.e(TAG, "wait ime_confirm for "+Env.getImeWaitTime()+"s");
				b = Activity.RESULT_OK == Env.wait("ime_confirm", Env.getImeWaitTime()*1000);
			}else {
				b = true;
			}
			if(b){
				if(go){
					if(!Env.sleep(300)){
						L.pushBoolean(false);
						L.pushString("Env.sleep failed.");
						return 2;
					}
					intent = new Intent(IME_EDITORCODE);
					intent.putExtra("code", 2);
					Env.getContext().sendBroadcast(intent);
					if(Env.getImeWaitTime()>0) {
						b = Activity.RESULT_OK == Env.wait("ime_confirm", Env.getImeWaitTime()*1000);
					}else {
						b = true;
					}
					if(!Env.sleep(500)){
						L.pushBoolean(false);
						L.pushString("Env.sleep failed.");
						return 2;
					}
				}else{
					Env.sleep(1000);
				}
			}
			Logger.e(TAG, "exe: "+String.format("input_chinese(%s, %s, %s)=%s",value, String.valueOf(go), String.valueOf(auto_wait), b?"success":"failed"));
			L.pushBoolean(b);
			L.pushString("");
			return 2;
			/*}else if(prop.equals("send_app_image")) { // send_app_image, app_id, desc, left, top, right, bottom		
			if (count < 8){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String app_id = L.toString(3);
			String desc = L.toString(4);
			int left = (int) L.toInteger(5);
			int top = (int) L.toInteger(6);
			int right = (int) L.toInteger(7);
			int bottom = (int) L.toInteger(8);
			if(left <0 || right<0 || top <0 || bottom <0 ||  left >= right || top >= bottom){
				Logger.e(TAG, "capture_and_send_app_image failed: bad coordinates.");
				L.pushBoolean(false);
				L.pushString("capture_and_send_app_image failed. bad coordinates.");
				return 2;
			}
			Logger.e(TAG, "capture_and_send_app_image: app_id="+app_id+",desc="+desc+String.format(" (%d,%d,%d,%d)", left, top, right, bottom));
			while(!Env.isStopped()&&!capture_and_send_app_image(app_id, desc, left, top, right, bottom)){
				Logger.e(TAG, "capture_and_send_app_image: failed, wait 5 secs");
				ThreadUtil.Sleep(5000);
			}
			if(Env.isStopped()){
				L.pushBoolean(false);
				L.pushString("capture_and_send_app_image failed.");
				return 2;
			}else {
				L.pushBoolean(true);
				L.pushString("");
				return 2;
			}*/
		}else if(prop.equals("getappdata")) { // getappdata, data_name, sim_imsi, nosim_key
			if (count < 4){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String data_name = L.toString(3);
			String sim_imsi = L.toString(4);
			String nosim_key = null;
			if (count > 4){
				nosim_key = L.toString(5);
			}
			String dataStr = getappdata(data_name, sim_imsi, nosim_key);
			Logger.e(TAG, "exe: "+String.format("getappdata(%s, %s, %s)=%s",data_name, sim_imsi, nosim_key, dataStr));
			if(dataStr == null){
				L.pushBoolean(false);
				L.pushString("getappdata failed.");
				return 2;
			}else{
				L.pushBoolean(true);
				L.pushString(dataStr);
				return 2;
			}
		}else if(prop.equals("sumitappdata")) { // sumitappdata, accountJson, accountDetailJson, task_id, subtask_id
			if (count < 6){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String accountJson = L.toString(3);
			String accountDetailJson = L.toString(4);
			String task_id = L.toString(5);
			String subtask_id = L.toString(6);
			boolean b = false;
			if(!Env.isStopped()){
				b = sumitappdata(accountJson, accountDetailJson, task_id, subtask_id);
			}
			Logger.e(TAG, "exe: "+String.format("sumitappdata(%s, %s, %s, %s)=%s",accountJson, accountDetailJson, task_id, subtask_id, b?"success":"failed"));
			
			L.pushBoolean(b);
			L.pushString(b?"":"sumitappdata failed.");
			return 2;
		}else if(prop.equals("testconnect")) { 
			boolean b = false;
			for(int i=0;i<2; i++){
				if(ServerUtil.testServerConnect()){
					b = true;
					break;
				}
				if(!Env.sleep(2000)) break;
			}
			Logger.e(TAG, "exe: "+String.format("testconnect()=%s",b?"success":"failed"));
			L.pushBoolean(b);
			L.pushString(b?"success": "testconnect failed");
			return 2;
		}else if(prop.equals("is_stopped")) {
			boolean b = Env.isStopped();
			Logger.e(TAG, "exe: is_stopped()="+b);
			L.pushBoolean(b);
			L.pushString(b?"stopped": "");
			return 2;
		}else if(prop.equals("keep_app")) {
			boolean b = Env.isKeepapp();
			Logger.e(TAG, "exe: keep_app()="+b);
			L.pushBoolean(b);
			L.pushString(b?"keep_app": "");
			return 2;
		}else if(prop.equals("get_top_app")) {
			ComponentName info = SystemUtil.getForegroundActivity(Env.getContext());
			Logger.e(TAG, "exe: "+String.format("get_top_app()=%s",info!=null?info.getPackageName()+"/"+info.getClassName():""));
			if(info == null){
				L.pushBoolean(false);
				L.pushString("getForegroundActivity failed.");
				return 2;
			}else{
				L.pushBoolean(true);
				L.pushString(info.getPackageName());
				L.pushString(info.getClassName());
				return 3;
			}
		}else if(prop.equals("get_apk_files")) {
			if (count < 3){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String path = L.toString(3);
			File d = new File(path);
			if(!d.exists() || !d.isDirectory()){
				Logger.e(TAG, "exe: "+String.format("get_apk_files(%s)=%s",path, ""));
				L.pushBoolean(false);
				L.pushString("invalid path");
				return 2;
			}else{
				File[] childrens = d.listFiles();
				String files = "";
				if(childrens!=null && childrens.length>0){
					for(File f:childrens){
						if(f.isFile()&&f.getName().toLowerCase().endsWith(".apk")){
							files += f.getName();
							files += ";";
						}
					}
				}
				if(files.length()>0) files = files.substring(0, files.length()-1);
				Logger.e(TAG, "exe: "+String.format("get_apk_files(%s)=%s",path, files));
				if(files.length()>0){
					L.pushBoolean(true);
					L.pushString(files);
					return 2;
				}else{
					L.pushBoolean(false);
					L.pushString("no files");
					return 2;
				}
			}
		}else if(prop.equals("random_password")) { //random_password, min_len, max_len, extra_chars, boolean require_asc, boolean require_asc_u_l
			
			int min_len = 6;
			if (count > 2){
				min_len = (int)L.toInteger(3);
			}
			if(min_len<4) min_len = 4;
			int max_len = 16;
			if(count > 3){
				max_len = (int)L.toInteger(4);
				if(max_len<min_len) max_len = min_len;
			}
			String extra_chars = null;
			if(count > 4){
				extra_chars = L.toString(5);
			}
			boolean require_asc = true;
			if(count > 5){
				require_asc = L.toBoolean(6);
			}
			boolean require_asc_u_l = true;
			if(count > 6){
				require_asc_u_l = L.toBoolean(7);
			}
			String pass=NameUtil.getRandomPassword(Env.mRandom, min_len,max_len,extra_chars, require_asc, require_asc_u_l);
			Logger.e(TAG, "exe: "+String.format("random_password(%d, %d, %s, %s, %s)=%s",min_len, max_len, extra_chars, String.valueOf(require_asc), String.valueOf(require_asc_u_l), pass));
			L.pushBoolean(true);
			L.pushString(pass);
			return 2;
		}else if(prop.equals("random_username")) { //random_username, int min_len, int max_len, boolean gender
			int min_len = 6;
			if (count > 2){
				min_len = (int)L.toInteger(3);
			}
			if(min_len<4) min_len = 4;
			int max_len = 32;
			if(count > 3){
				max_len = (int)L.toInteger(4);
				if(max_len<min_len) max_len = min_len;
			}
			
			int gender = 0;
			if(count > 4){
				gender = L.toBoolean(5)?1:2;
			}
			String username = NameUtil.getRandomUserName(Env.mRandom, min_len, max_len, gender);
			Logger.e(TAG, "exe: "+String.format("random_username(%d, %d, %s)=%s",min_len, max_len, String.valueOf(gender), username));
			L.pushBoolean(true);
			L.pushString(username);
			return 2;
		}else if(prop.equals("random_nickname")) { //random_nickname, boolean chinese, boolean gender
			
			boolean chinese = false;
			if(count > 2){
				chinese = L.toBoolean(3);
			}
			int gender = 0;
			if(count > 3){
				gender = L.toBoolean(4)?1:2;
			}
			String username = NameUtil.getRandomNickName(Env.mRandom, chinese, gender);
			Logger.e(TAG, "exe: "+String.format("random_nickname(%s, %s)=%s",String.valueOf(chinese), String.valueOf(gender), username));
			L.pushBoolean(true);
			L.pushString(username);
			return 2;
		}else if(prop.equals("get_sim_number")) { //get_sim_number, string sim_imsi
			if (count < 3){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String sim_imsi = L.toString(3);
			String number = getNumberFromServer(sim_imsi);
			Logger.e(TAG, "exe: "+String.format("get_sim_number(%s)=%s",sim_imsi, number));
			if(StringUtil.isEmpty(number)){
				L.pushBoolean(false);
				L.pushString("getNumberFromServer failed");
				return 2;
			}else{
				L.pushBoolean(true);
				L.pushString(number);
				return 2;
			}
		}else if(prop.equals("emulate_device")) { //emulate_device, boolean random, boolean has_sim, string key
			boolean random = false;
			if(count > 2){
				random = L.toBoolean(3);
			}
			if(random){
				int vdev_num = Config.getInt("nosim_vdevice_num", 0);
				int phone_seq = Config.getInt("phone_seq", 0);
				EnvProfile p = null;
				String err = "get emualte data failed.";
				if(vdev_num >0 && phone_seq > 0){
					String nosim_key = String.format("%d-%06d", phone_seq,Env.mRandom.nextInt(vdev_num)+1);
					p = registerDevice(true, null, nosim_key);
				}else{
					err = "failed: vdev_num="+vdev_num+",phone_seq="+phone_seq;
				}
				Logger.e(TAG, "exe: "+String.format("emulate_device(true)=%s",p != null?"success":"failed"));
				if(p != null){
					BaseThread.updateEmuData(true, p, Env.getSleepObject());
					L.pushBoolean(true);
					L.pushString("succecss");
					return 2;
				}else{
					L.pushBoolean(false);
					L.pushString(err);
					return 2;
				}
			}else {
				if(count <4){
					L.pushBoolean(false);
					L.pushString("bad parameter count");
					return 2;
				}
				
				boolean has_sim  = L.toBoolean(3);
				String key = L.toString(4);
				EnvProfile p = registerDevice(!has_sim, has_sim?key:null, has_sim?null:key);
				Logger.e(TAG, "exe: "+String.format("emulate_device(false, %s, %s)=%s", String.valueOf(has_sim), key, p != null?"success":"failed"));
				if(p != null){
					BaseThread.updateEmuData(true, p, Env.getSleepObject());
					L.pushBoolean(true);
					L.pushString("succecss");
					return 2;
				}else{
					L.pushBoolean(false);
					L.pushString("get emualte data failed.");
					return 2;
				}
				
			}
			
			
		}else if(prop.equals("switch_vpn")) { // switch_vpn, boolean onoff, string server, string username, string password, boolean encrypt, string dns
			if (count < 3){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			boolean on = L.toBoolean(3);
			if(on && count<6){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			if(on){
				String server = L.toString(4);
				String username = L.toString(5);
				String password = L.toString(6);
				boolean enc = false;
				if(count > 6){
					enc = L.toBoolean(7);
				}
				String dns = "8.8.8.8";
				if(count > 7){
					dns = L.toString(8);
				}
				String vpn_city = "unset";
				if(count > 7){
					vpn_city = L.toString(8);
				}
				VpnProfile p=new VpnProfile();
				p.account = new VpnAccount();
				p.account.setPpe((byte)(enc?1:0));
				p.account.setServer(server);
				p.account.setUsername(username);
				p.account.setPassword(password);
				p.account.setDns(dns);
				p.account.setCity(vpn_city);
				boolean b = PlatformUtil.switchVPN(Env.getContext(), true, p, Env.getSleepObject());
				Logger.e(TAG, "exe: "+String.format("switch_vpn(true, %s, %s, %s, %s, %s, %s)=%s", server, username, password, String.valueOf(enc), dns, vpn_city, b?"success":"failed"));
				L.pushBoolean(b);
				L.pushString(b?"success":"failed");
				return 2;
			}else{
				boolean b = PlatformUtil.switchVPN(Env.getContext(), false, null, Env.getSleepObject());
				Logger.e(TAG, "exe: "+String.format("switch_vpn(false)=%s", b?"success":"failed"));
				L.pushBoolean(true);
				L.pushString("success");
				return 2;
			}
		}else if(prop.equals("get_my_ip")) {  // get_my_ip
			String ip = ServerUtil.getMyIp();
			Logger.e(TAG, "exe: "+String.format("get_my_ip()=%s", ip));
			if(!StringUtil.isEmpty(ip)){
				L.pushBoolean(true);
				L.pushString(ip);
				return 2;
			}else{
				L.pushBoolean(false);
				L.pushString("getMyIp failed");
				return 2;
			}
		}else if(prop.equals("write_txt_file")) {  // write_txt_file, string filepath, string text, boolean append
			if(count<4){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String filepath = L.toString(3);
			String text = L.toString(4);
			boolean append = false;
			if(count > 4){
				append = L.toBoolean(5);
			}
			boolean b = FileUtil.writeTxtFile(filepath, text, append);

			Logger.e(TAG, "exe: "+String.format("write_txt_file(%s, %s, %s)=%s", filepath, text, String.valueOf(append), b?"success":"failed"));
			L.pushBoolean(b);
			L.pushString(b?"success":"write_txt_file failed.");
			return 2;
		}else if(prop.equals("read_txt_file")) {  // read_txt_file, string filepath, 
			if(count<3){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String filepath = L.toString(3);
			String text = FileUtil.ReadTxtFile(filepath);
			Logger.e(TAG, "exe: "+String.format("read_txt_file(%s)=%s", filepath, text));
			L.pushBoolean(text!=null);
			L.pushString(text==null?"failed":text);
			return 2;
		}else if(prop.equals("is_tyime_present")) {  // is_tyime_present
			boolean b = SystemUtil.isTyImePresent(Env.getContext());
			L.pushBoolean(b);
			L.pushString(b?"ok":"failed");
			return 2;
		}else if(prop.equals("get_language")) { 
			L.pushBoolean(true);
			L.pushString(Locale.getDefault().toString());
			return 2;
		}else if(prop.equals("switch_language")) { 
			if(count<3){
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String language = L.toString(3);
			String current = Locale.getDefault().toString(); //比如繁体为zh_TW，简体为zh_CN，英文中有en_GB，日文有ko_KR。
			if(language.equals(current)){
				L.pushBoolean(true);
				L.pushString("no changes");
				return 2;
			}
			final Locale l = Locale.forLanguageTag(language.replace('_', '-'));
			boolean b = false;
			if(l != null){
				b = PlatformUtil.updateLocale(l);
			}
			L.pushBoolean(b);
			L.pushString(b?"ok":"failed");
			return 2;
		}else {
			Logger.e(TAG, "exe: unknow name "+prop);
			L.pushBoolean(false);
			L.pushString("unknow name "+prop);
			return 2;
		}
	}
	private EnvProfile registerDevice(boolean no_sim, String sim_imsi, String nosim_key) {
		try  
        {
			OkHttpClient client = new OkHttpClient(); 
			FormBody.Builder builder = new FormBody.Builder()
				    .add("has_sim", no_sim?"0":"1");
			if(no_sim){
				builder.add("nosim_key", nosim_key);
			}else{
				builder.add("sim_imsi", sim_imsi);
			}
			Logger.e(TAG, "registerDevice: no_sim="+no_sim+ (no_sim?(",nosim_key="+nosim_key):(",sim_imsi="+sim_imsi)));
			Request request = new Request.Builder()  
		            .url(Constant.getURL_REGISTER_DEVICE())  
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
            	JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					Device d = null;
					Platform p = null;
					String sim_str = null;
					String d_str = dataObj.optString("device");
					if(StringUtil.isNotEmpty(d_str)){
						d = JSON.parseObject(d_str, Device.class);
					}
					String p_str = dataObj.optString("platform");
					if(StringUtil.isNotEmpty(p_str)){
						p = JSON.parseObject(d_str, Platform.class);
					}
					Logger.e(TAG, "registerDevice: device=" + d_str+",platform="+p_str+",sim="+sim_str);
					if(d != null && p != null){
						EnvProfile env = new EnvProfile();
						env.device = d;
						env.platform = p;
						return env;
					}
				}
            }
        } catch (Exception e) {  
        	e.printStackTrace();
        	Logger.e(TAG, "registerDevice Exception: "+e);
        }  
		return null; 	
	}
	
	private String getNumberFromServer(String imsi){
		if(imsi == null) {
			return null;
		}
		try  
        {
			OkHttpClient client = new OkHttpClient(); 
			FormBody.Builder builder = new FormBody.Builder()
				    .add("imsi", imsi);
			
			Logger.e(TAG, "getNumberFromServer: imsi=" + imsi);
			Request request = new Request.Builder()  
		            .url(Constant.getURL_QUERY_NUMBER())  
		            .post(builder.build())
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Log.e(TAG, "getNumberFromServer: response erroNo=" + errorNo);
            if(errorNo!=null && errorNo.equals("0")){
            	JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					return dataObj.optString("number");
				}
	        }
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
            Logger.e(TAG, "getNumberFromServer Exception: "+e);
        }  
		return null;
    }
	
	private String getappdata(String account_name, String sim_imsi, String nosim_key){
		if(StringUtil.isEmpty(account_name)||(StringUtil.isEmpty(sim_imsi)&&StringUtil.isEmpty(nosim_key))){
			return null;
		}
		try  
        {
			OkHttpClient client = new OkHttpClient(); 
			FormBody.Builder builder = new FormBody.Builder();
	        builder.add("account_name", account_name);
	        if(!StringUtil.isEmpty(sim_imsi)){
		        builder.add("sim_imsi", sim_imsi);
	        }
	        if(!StringUtil.isEmpty(nosim_key)){
		        builder.add("nosim_key", nosim_key);
	        }
	        Logger.e(TAG, "getappdata: account_name="+account_name+",sim_imsi="+sim_imsi+",nosim_key="+nosim_key);
			
			Request request = new Request.Builder()  
		            .url(Constant.getURL_APP_GET_DATA())  
		            .post(builder.build())
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "getappdata: response erroNo=" + errorNo);
            if(errorNo!=null && errorNo.equals("0")){
            	Logger.e(TAG, "getappdata: body=" + body);
            	JSONObject dataObj = rspObject.optJSONObject("data");
				if (dataObj != null) {
					return dataObj.toString();
				}
            }
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
        	Logger.e(TAG, "getappdata Exception: "+e);
        }  
		return null; 	
	}
	
	private boolean sumitappdata(String accountJson, String accountDetailJson, String task_id, String subtask_id){
		if(StringUtil.isEmpty(accountJson)) return false;
		Logger.e(TAG, "sumitappdata: accountJson="+accountJson+",accountDetailJson="+accountDetailJson);
		try  
        {
			WebAccount wa = new WebAccount();
		    JSONObject jsonObj = new JSONObject(accountJson);
			Iterator<String> it = jsonObj.keys();  
	        while(it.hasNext()){  
	        	 String key = (String) it.next();  
	             String value = jsonObj.getString(key);  
	             if(key !=null && value != null){
	            	 if(key.equals("eId")) wa.seteId(value);
	            	 else if(key.equals("accountName")) wa.setAccountName(value);
	            	 else if(key.equals("simImsi")) wa.setSimImsi(value);
	            	 else if(key.equals("simNumber")) wa.setSimNumber(value);
	            	 else if(key.equals("nosimKey")) wa.setNosimKey(value);
	            	 else if(key.equals("username")) wa.setUsername(value);
	            	 else if(key.equals("password")) wa.setPassword(value);
	            	 else if(key.equals("vpnCity")) wa.setVpnCity(value);
	            	 else if(key.equals("type")) wa.setType((byte)Integer.parseInt(value));
	            	 else if(key.equals("status")) wa.setStatus((byte)Integer.parseInt(value));
	             }
	        }  
	        if(wa.getStatus()==1){
	        	wa.setSignupTime(new Date());
	        }else if(wa.getStatus()==2){
	        	wa.setSigninTime(new Date());
	        }
			if(StringUtil.isEmpty(wa.getAccountName()) ||
					(StringUtil.isEmpty(wa.getSimImsi())&&StringUtil.isEmpty(wa.getNosimKey()))){
				return false;
			}
	        WebAccountDetail wad = null;
	        if(!StringUtil.isEmpty(accountDetailJson)) {
	        	wad = new WebAccountDetail();
	        	boolean update =false;
		        jsonObj = new JSONObject(accountDetailJson);
		        it = jsonObj.keys();  
		        while(it.hasNext()){  
		        	 String key = (String) it.next();  
		             String value = jsonObj.getString(key);  
		             if(key !=null && value != null){
		            	 if(key.equals("nickname")) { wad.setNickname(value); update = true; }
		            	 else if(key.equals("gender")) { wad.setGender((byte)Integer.parseInt(value)); update = true; }
		            	 else if(key.equals("area")) { wad.setArea(value); update = true; }
		            	 else if(key.equals("signature")) { wad.setSignature(value); update = true; }
		            	 else if(key.equals("email")) { wad.setEmail(value); update = true; }
		            	 else if(key.equals("other")) { wad.setOther(value); update = true; }
		             }
		        }  
		        if(!update) { wad = null; }
	        }
	        
			OkHttpClient client = new OkHttpClient(); 
			FormBody.Builder builder = new FormBody.Builder();
			builder.add("web_account", JSON.toJSONString(wa));
			if(wad != null){
				builder.add("web_account_detail", JSON.toJSONString(wad));
			}
			if(task_id!=null){
				builder.add("task_id", task_id);
			}
			if(subtask_id!=null){
				builder.add("subtask_id", subtask_id);
			}
			
			Request request = new Request.Builder()  
		            .url(Constant.getURL_APP_SUBMIT_DATA())  
		            .post(builder.build())
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "sumitappdata: response erroNo=" + errorNo);
            return (errorNo!=null && errorNo.equals("0"));
        }  
        catch (Exception e)  
        {  
        	e.printStackTrace();
        	Logger.e(TAG, "sumitappdata Exception: "+e);
        }  
		return false; 	
	}
	
	/*
	private boolean capture_and_send_app_image(String app_id, String description, int left, int top, int right, int bottom){
		Logger.e(TAG, "capture_and_send_app_image: app_id="+app_id+",desc="+description+String.format(",(%d, %d, %d, %d)", left, top, right,bottom));
		
		Bitmap bitmap = null;
		for(int i=0; i<5; i++) {
			Bitmap cb = Env.takeScreenshot();
			if(cb != null){
				bitmap = cb;
				break;
			}else{
				ThreadUtil.Sleep(3000);
			}
		}
		if(bitmap == null) {
			Logger.e(TAG, "capture_and_send_app_image failed: capture_image failed.");
			return false;
		}
		Bitmap cropBmp = null;
		try {
			cropBmp = Bitmap.createBitmap(bitmap, left, top, (right-left), (bottom-top));
		}catch(Exception e){
			e.printStackTrace();
		}
		if(cropBmp == null){
			Logger.e(TAG, "capture_and_send_app_image failed: create cropBmp failed.");
			return false;
		}
		// save cropBmp
		File file = new File("/sdcard/tmp/crop.png");
        if(file.exists()){
            file.delete();
        }
        FileOutputStream out;
        try{
            out = new FileOutputStream(file);
            if(cropBmp.compress(Bitmap.CompressFormat.PNG, 90, out))
            {
                out.flush();
                out.close();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        file = new File("/sdcard/tmp/crop.png");
        if(!file.exists()){
			Logger.e(TAG, "capture_and_send_app_image failed: compress cropBmp failed.");
        	return false;
        }
        
        try{
	        OkHttpClient client = new OkHttpClient();
	        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
			RequestBody formBody = new MultipartBody.Builder()
						.setType(MultipartBody.FORM)
					    .addFormDataPart("app_id", app_id)
					    .addFormDataPart("file_type", "0")
					    .addFormDataPart("description",description)
					    .addFormDataPart("appFile", "1.png", fileBody)
					    .build();
			Logger.e(TAG,"capture_and_send_app_image: app_id="+app_id+",desc="+description);

			Request request = new Request.Builder()  
		            .url(Constant.getURL_APP_SUBMIT_FILE())  
		            .post(formBody)
		            .build();  
			Response response = client.newCall(request).execute();  
		    if (!response.isSuccessful())  
		        throw new IOException("Unexpected code " + response);  
		    String body = response.body().string();
		    JSONObject rspObject = new JSONObject(body);
		    String errorNo = rspObject.optString("errorNo");
	        Logger.e(TAG, "capture_and_send_app_image: response erroNo=" + errorNo);
	        return errorNo!=null && errorNo.equals("0");
        }catch (Exception e)
        {
			Logger.e(TAG, "capture_and_send_app_image failed: url access exception: "+e);
            //e.printStackTrace();
            return false;
        }
		
	}
	*/
}

