package com.example.rowetalk.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;

import android.os.Build;
import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {
	private static final String TAG = HttpUtil.class.getName();
	private static final int TIMEOUIT = 60 * 1000; // 超时时间60秒
	
	public static boolean downloadFile(String urlStr, String filePath, ProgressHandler handler){
		URL url = null;  
        HttpURLConnection urlcon = null;  
        FileOutputStream fos = null; 
        InputStream is = null;
        File f = null;
        try {
            url = new URL(urlStr);  
            urlcon = (HttpURLConnection) url.openConnection();
            
            is = urlcon.getInputStream(); 
            f = new File(filePath);
            File dir = new File(f.getParent());
			if(!dir.exists()) dir.mkdirs();
			
            f.createNewFile();  
            fos = new FileOutputStream(f);  
            byte[] buf = new byte[4*1024];  
            int rbytes = 0;
            int recvd = 0;
            while ((rbytes = is.read(buf)) != -1) {  
                fos.write(buf, 0, rbytes);  
                recvd += rbytes;
                //同步更新数据  
                if(handler != null) {
                	handler.updateSize(recvd);  
                }
            }  
            is.close();  
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace(); 
            try {
	            if(f !=null && f.exists()){
	            	f.delete();
	            }
	            if(is != null) is.close();  
	        	if(fos != null) fos.close();
	        	urlcon.disconnect();
            }catch(Exception e1){}
        }  
		return false;
	}
	public static boolean uploadFile(String urlStr, Map<String, Object> params, String fileName, String filePath, ProgressHandler handler, ResponseHandler rspHandler){
        String BOUNDARY = UUID.randomUUID().toString();
		String PREFIX = "--", LINEND = "\r\n";
		String MULTIPART_FROM_DATA = "multipart/form-data";
		String CHARSET = "UTF-8";
		
		File f = null;
		if(filePath != null) {
			Logger.e(TAG, "uploadFile: "+filePath);
			f = new File(filePath);
			if(!f.exists() || !f.isFile()){
				f = null;
			}
		}
		DataOutputStream outStream = null;
		InputStream is = null;
		try {
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(TIMEOUIT);
    		conn.setReadTimeout(TIMEOUIT); // 缓存的最长时间
    		conn.setDoInput(true);// 允许输入
    		conn.setDoOutput(true);// 允许输出
    		conn.setUseCaches(false); // 不允许使用缓存
    		conn.setRequestMethod("POST");
    		conn.setRequestProperty("Charsert", "UTF-8");
    		conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
    				+ ";boundary=" + BOUNDARY);
    		
    		// http://www.tuicool.com/articles/7FrMVf, incase java.lang.EOFException
    		// http://stackoverflow.com/questions/12319194/android-httpurlconnection-throwing-eofexception
    		if (Build.VERSION.SDK_INT > 13) {
    			conn.setRequestProperty("Connection", "Close");
    		}else {
        		conn.setRequestProperty("Connection", "Keep-Alive");
    		}
    		// 首先组拼文本类型的参数
    		StringBuilder sb = new StringBuilder();
    		for (Map.Entry<String, Object> entry : params.entrySet()) {
    			if(entry.getValue()==null){
    				continue; // null will cause 400 error where request param is required true or false
    			}
    			sb.append(PREFIX);
    			sb.append(BOUNDARY);
    			sb.append(LINEND);
    			sb.append("Content-Disposition: form-data; name=\""
    					+ entry.getKey() + "\"" + LINEND);
    			sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
    			sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
    			sb.append(LINEND);
    			sb.append(entry.getValue());
    			sb.append(LINEND);
    		}

    		outStream = new DataOutputStream(conn.getOutputStream());
    		outStream.write(sb.toString().getBytes());
    		//write file
    		byte[] buf = new byte[4*1024];  
            int rbytes = 0;
            long written = 0;
    		if(f!= null) {
	    		outStream.writeBytes(PREFIX + BOUNDARY + LINEND);
	    		outStream.writeBytes("Content-Disposition: form-data; "
						+ "name=\""+fileName+"\";filename=\""+f.getName()+"\""
						+ LINEND);
	    		outStream.writeBytes("Content-Type: application/octet-stream"+ LINEND);
	    		outStream.writeBytes("Content-Transfer-Encoding: binary" + LINEND);
	    		outStream.writeBytes(LINEND);
	            FileInputStream fis = new FileInputStream(f); 
	            while ((rbytes = fis.read(buf)) != -1) {  
	            	outStream.write(buf, 0, rbytes);  
	            	written += rbytes;
	                //同步更新数据  
	                if(handler != null) {
	                	handler.updateSize(written);  
	                }
	            }  
	            outStream.writeBytes(LINEND);
    		}
    		// 请求结束标志
    		byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
    		outStream.write(end_data);
    		outStream.flush();
    		// 得到响应码
    		int res = conn.getResponseCode(); // java.io.EOFException offen occurs here
			is = conn.getInputStream();
    		if(res == HttpURLConnection.HTTP_OK) {
    			StringBuffer b = new StringBuffer();
    			while ((rbytes = is.read(buf)) > 0 ) {  /* != -1 */
    				b.append(new String(buf, 0, rbytes));
    			}

        		outStream.close();
    			is.close();
    			if(rspHandler != null) {
    				return rspHandler.onSuccess(b.toString());
    			}
    			return true;
    		}else{
    			Logger.e(TAG, "uploadFile failed: responseCode=" +res);
        		outStream.close();
        		is.close();
    			if(rspHandler != null) {
    				rspHandler.onFailure(res, conn.getResponseMessage());
    			}
    			return false;
    		}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.e(TAG, "uploadFile: Exception: " + e);
		}
		if(outStream != null){
			try {
				outStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		if(is != null){
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		return false;
	}
	public static boolean downloadFile(String urlStr, Map<String, Object> params, String filePath, ProgressHandler handler){
        String BOUNDARY = UUID.randomUUID().toString();
		String PREFIX = "--", LINEND = "\r\n";
		String MULTIPART_FROM_DATA = "multipart/form-data";
		String CHARSET = "UTF-8";
		Logger.e(TAG, "downloadFile: "+filePath);
		
		DataOutputStream outStream = null;
		InputStream is = null;
        try {  
        	File f = new File(filePath);
            File dir = new File(f.getParent());
			if(!dir.exists()) dir.mkdirs();
			
        	URL url = new URL(urlStr);  
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
            conn.setConnectTimeout(TIMEOUIT);
    		conn.setReadTimeout(TIMEOUIT); // 缓存的最长时间
    		conn.setDoInput(true);// 允许输入
    		conn.setDoOutput(true);// 允许输出
    		conn.setUseCaches(false); // 不允许使用缓存
    		conn.setRequestMethod("POST");
    		conn.setRequestProperty("connection", "keep-alive");
    		conn.setRequestProperty("Charsert", "UTF-8");
    		conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
    				+ ";boundary=" + BOUNDARY);
    		// 首先组拼文本类型的参数
    		StringBuilder sb = new StringBuilder();
    		for (Map.Entry<String, Object> entry : params.entrySet()) {
    			if(entry.getValue()==null){
    				continue; // null will cause 400 error where request param is required true or false
    			}
    			sb.append(PREFIX);
    			sb.append(BOUNDARY);
    			sb.append(LINEND);
    			sb.append("Content-Disposition: form-data; name=\""
    					+ entry.getKey() + "\"" + LINEND);
    			sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
    			sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
    			sb.append(LINEND);
    			sb.append(entry.getValue());
    			sb.append(LINEND);
    		}

    	    outStream = new DataOutputStream(conn.getOutputStream());
    		outStream.write(sb.toString().getBytes());
    		// 请求结束标志
    		byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
    		outStream.write(end_data);
    		outStream.flush();
    		// 得到响应码
    		int res = conn.getResponseCode();
    		is = conn.getInputStream(); 
    		if(res == 200) {
	            f.createNewFile();  
	            FileOutputStream fos = new FileOutputStream(f);  
	            byte[] buf = new byte[4*1024];  
	            int rbytes = 0;
	            long recvd = 0;
	            int  update_count = 0;
	            while ((rbytes = is.read(buf)) != -1) {  
	                fos.write(buf, 0, rbytes);  
	                recvd += rbytes;
	                //同步更新数据 
	                update_count++;
	                if(handler != null&&update_count==10) {
	                	update_count = 0;
	                	handler.updateSize(recvd);  
	                }
	            }  
	            if(handler != null&&update_count<10) {
                	update_count = 0;
                	handler.updateSize(recvd);  
                }
	            is.close();  
	            fos.flush();
	            fos.close();
	            outStream.close();
	            return recvd > 0;
    		}else{
    			is.close();
	            outStream.close();
	            Logger.e(TAG, "downloadFile failed: responseCode="+res);
    		}
        } catch (Exception e) {  
            e.printStackTrace();  
            File f = new File(filePath);
            if(f.exists()){
            	f.delete();
            }
        }  
        if(outStream != null){
			try {
				outStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		if(is != null){
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean post(String urlStr, Map<String, Object> params, ResponseHandler rspHandler){
		return uploadFile(urlStr, params, null, null, null, rspHandler);
	}
	
	public interface ProgressHandler  
    {  
        public void updateSize(long size);  
    } 
	public interface ResponseHandler  
    {  
        public boolean onSuccess(String response);
        public boolean onFailure(int statusCode, String response);
    } 
	
}
