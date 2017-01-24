package com.liblua;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.List;

import com.example.rowetalk.util.DateUtil;
import com.example.rowetalk.util.Logger;
import com.example.rowetalk.util.StringUtil;
import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaState;

import android.R.layout;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Display;

public class LuaLayoutDump extends JavaFunction
{
	private static final String TAG = LuaLayoutDump.class.getName();
	private LuaState L;
	private LuaEnv Env;
	
	public LuaLayoutDump(LuaEnv Env, LuaState L)
	{
		super(L);
		this.L = L;
		this.Env = Env;
	}

	// layout_dump(0) // 导出当前activity 列表
	// layout dump(1, string tag) // 导出指定tag的布局
	// layout dump(2, string package) // 导出指定 activity 的指定布局, 合并了 0， 1的处理
	// layout_dump(3, string image_tag, int op2) // 截图指定界面，op2:0, saveonly; 1: ocr chinese, 2: ocr english
	
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
		// get number
		int op = (int)L.toInteger(2);
		if(op == 0){
			StringBuilder sb= new StringBuilder();
			boolean b = sendViewCmd("LIST", sb);
			L.pushBoolean(b);
			L.pushString(b?sb.toString():"layout_dump 0 failed.");
			return 2;
		}else if(op == 1){
			if (count < 3)
			{
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String tag = L.toString(3);
			StringBuilder sb= new StringBuilder();
			boolean b = sendViewCmd("DUMP "+tag, sb);
			L.pushBoolean(b);
			L.pushString(b?sb.toString():"layout_dumpa 1 failed.");
			return 2;
		}else if(op == 2){
			if (count < 3)
			{
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String package_name = L.toString(3);
			StringBuilder sb= new StringBuilder();
			boolean b = sendViewCmd("LIST", sb);
			if(!b){
				L.pushBoolean(b);
				L.pushString("layout_dump 2 failed.");
				return 2;
			}
			String[] results=sb.toString().split("\n");
			if(results == null){
				L.pushBoolean(b);
				L.pushString("layout_dump 2 failed.");
				return 2;
			}
			String tag = null;
			for(String s:results){
				if(s.indexOf(package_name)>0){
					int i = s.trim().indexOf(" ");
					if(i >0) {
						tag = s.substring(0, i);
						break;
					}
				}
			}
			if(tag != null) tag = tag.trim();
			if(tag == null || tag.length() ==0){
				L.pushBoolean(b);
				L.pushString("sendViewCmd tag not found .");
				return 2;
			}
			sb= new StringBuilder();
			b = sendViewCmd("DUMP "+tag, sb);
			L.pushBoolean(b);
			L.pushString(b?sb.toString():"layout_dump 2 failed.");
			return 2;
		}else if(op == 3){ //layout_dump(3, string image_tag, int op2) // 截图指定界面，op2:0, saveonly; 1: ocr chinese, 2: ocr english
			if (count < 4)
			{
				L.pushBoolean(false);
				L.pushString("bad parameter count");
				return 2;
			}
			String image_tag = L.toString(3);
			int op2 = (int)L.toInteger(4);
			StringBuilder sb= new StringBuilder();
			boolean b = sendViewCaptureCmd(image_tag, op2, sb);
			L.pushBoolean(b);
			L.pushString(b?sb.toString():"layout_dump 3 failed.");
			return 2;
		}else {
			L.pushBoolean(false);
			L.pushString("unsupport op: "+op);
			return 2;
		}
		
	}
	
	private boolean sendViewCmd(String cmd, StringBuilder sb){
		Log.e(TAG, "sendViewCmd: "+cmd);
		Socket socket = null;
		BufferedWriter out = null;
		BufferedReader in = null;
		boolean b = false;
		try{
		    socket = new Socket();
		    socket.connect(new InetSocketAddress(InetAddress.getLocalHost(),4939),40000);
		    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		    in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
		    out.write(cmd);
		    out.newLine();
		    out.flush();
		   
		  //http://blog.csdn.net/dalianmaoblog/article/details/11098751
		  //receive response from viewserver
		  //Output: 21d12790 com.example.testimsi/com.example.testimsi.MainActivity

		   String line;
		   while((line = in.readLine()) != null) {
			   if("DONE.".equalsIgnoreCase(line))
			   { //$NON-NLS-1$
		         break;
		       }
			   Log.e(TAG, "Output: "+line);
			   sb.append(line+"\n");
		   }
		  
		   b = true;
		} catch (IOException e) {
            // Empty
			e.printStackTrace();
			Logger.e(TAG, "sendViewCmd Exception: "+e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		return b;
	}

	private boolean sendViewCaptureCmd(String image_tag, int op2, StringBuilder sb){
		Log.e(TAG, "sendViewCmd: CAPTURE "+image_tag);
		Socket socket = null;
		BufferedWriter out = null;
		BufferedInputStream in = null;
		boolean b = false;
		try{
		    socket = new Socket();
		    socket.connect(new InetSocketAddress(InetAddress.getLocalHost(),4939),40000);
		    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		    in = new BufferedInputStream(socket.getInputStream());
		    out.write("CAPTURE "+image_tag);
		    out.newLine();
		    out.flush();

		    Bitmap bmp = BitmapFactory.decodeStream(in);
		    if(bmp != null){
		    	if(op2 == 0){
		    		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+
							"/tmp/cap_"+DateUtil.getCurrentDate("yyyyMMdd_HHmmss")+".png";
		    		File file = new File(filePath);

					if(file.exists()){
		        		file.delete();
		        	}else{
		        		File d = new File(file.getParent());
		        		if(!d.exists()) d.mkdirs();
		        	}
					file.createNewFile();
			        FileOutputStream out2 = new FileOutputStream(file);
			        bmp.compress(Bitmap.CompressFormat. PNG, 100, out2);
			        sb.append(filePath);
			        b = true;
		    	}else if(op2 == 1){
		    		Env.mTessBaseAPIChi.setImage(bmp);
					String text2= Env.mTessBaseAPIChi.getUTF8Text();
					Env.mTessBaseAPIChi.clear();
					Log.e(TAG, "ocr chinese text->"+text2);
					if(StringUtil.isNotEmpty(text2)){
						sb.append(text2);
				        b = true;
					}
		    	}else if(op2 == 2){
		    		Env.mTessBaseAPIEng.setImage(bmp);
					String text2= Env.mTessBaseAPIEng.getUTF8Text();
					Env.mTessBaseAPIEng.clear();
					Log.e(TAG, "ocr english text->"+text2);
					if(StringUtil.isNotEmpty(text2)){
						sb.append(text2);
				        b = true;
					}
		    	}

		        bmp.recycle();
		    }
		    
		} catch (IOException e) {
            // Empty
			e.printStackTrace();
			Logger.e(TAG, "sendViewCaptureCmd Exception: "+e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		return b;
	}

}

