package com.liblua;

import java.util.Date;

import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaState;

import android.R.layout;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;

public class LuaSendSms extends JavaFunction
{

	private LuaState L;
	private LuaEnv Env;
	
	public LuaSendSms(LuaEnv Env, LuaState L)
	{
		super(L);
		this.L = L;
		this.Env = Env;
	}

	// 参数：String number, String message, long wait_second
	// 结果：boolean result, string fail_msg, long sent_time
	@Override
	public int execute() throws LuaException
	{
		if (L.getTop() < 3)
		{
			L.pushBoolean(false);
			L.pushString("bad parameter count");
			return 2;
		}
		// get number
		String number = L.toString(2);
		String message = L.toString(3);
		long wait_seconds = 0;
		if(L.getTop()>3){
			wait_seconds = L.toInteger(4);
		}
		if(number == null){
			L.pushBoolean(false);
			L.pushString("number is null");
			return 2;
		}
		if(message == null){
			L.pushBoolean(false);
			L.pushString("message is null");
			return 2;
		}
		Date date = new Date();
		if(!sendSms(number, message, wait_seconds*1000)){
			L.pushBoolean(false);
			L.pushString("sendSms failed.");
			return 2;
		}
		L.pushBoolean(true);
		L.pushString("success.");
		L.pushInteger(date.getTime());
		return 3;
	}

	
	private boolean sendSms(String number, String msg, long wait_ms){
		Env.print("sendSms: number="+number+",message="+msg);
		
		Intent intent = new Intent(Env.SMS_SENT);
		intent.putExtra("number", number);
		intent.putExtra("message", msg);
		
		PendingIntent sentPI = PendingIntent.getBroadcast(Env.getContext(), 0, intent, 0);//PendingIntent.FLAG_ONE_SHOT);
		/*PendingIntent deliveredPI = PendingIntent.getBroadcast(mContext, 0, 
				new Intent(BotService.SMS_DELIVERED).putExtra("number", number), 0);*/

		
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(number, null, msg, sentPI, null/* deliveredPI*/);
		
		if(wait_ms > 0){
			int r = Env.wait("sendSms", wait_ms);
			if(r == Env.RESULT_DEFAULT || r != Activity.RESULT_OK){
				Env.print("sendSms result="+r);
				return false;
			}
		}
		return true;
	}

}

