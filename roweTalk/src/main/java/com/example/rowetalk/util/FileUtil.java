package com.example.rowetalk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
	private static final String TAG = FileUtil.class.getName();
	
	public static List<String> ReadTxtLines(String strFilePath)
    {
		List<String> list = new ArrayList<String>();
        File f = new File(strFilePath);

        if (f.exists() && !f.isDirectory())
        {
            try {
                InputStream instream = new FileInputStream(f);
                if (instream != null)
                {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while (( line = buffreader.readLine()) != null) {
                        list.add(line);
                    }                
                    instream.close();
                }
            }
            catch (Exception e)
            {
                 Logger.e(TAG, "ReadTxtFile Exception: "+e);
            }
        }
        return list;
    }
	
	public static String ReadTxtFile(String strFilePath)
    {
		
        File f = new File(strFilePath);

        if (f.exists() && !f.isDirectory())
        {
            try {
                InputStream instream = new FileInputStream(f);
                if (instream != null)
                {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    StringBuilder sb = new StringBuilder();
                    while (( line = buffreader.readLine()) != null) {
                        sb.append(line);
                    }                
                    instream.close();
                    String s = sb.toString();
                    Logger.e(TAG, "ReadTxtFile: "+strFilePath+"="+s);
                    return s;
                }
            }
            catch (Exception e)
            {
                 Logger.e(TAG, "ReadTxtFile Exception: "+e);
            }
        }
        return null;
    }
	public static boolean writeTxtFile(String filePath, String dec, boolean append) {
		// TODO Auto-generated method stub
		if(StringUtil.isEmptyOrNull(dec)) return false;
		Logger.e(TAG, "writeTxtFile： filePath="+filePath+",text="+dec+",append="+append);
        try {
        	File file = new File(filePath);  
        	if(!file.exists()){
        		File dir = new File(file.getParent());
    			if(!dir.exists()) dir.mkdirs();
        		file.createNewFile();
        		append = false;
        	}
			OutputStreamWriter out = new OutputStreamWriter(
            		new FileOutputStream(file, append),"utf8");//考虑到编码格式
			out.write(dec,0,dec.length());    
			out.flush();  
			out.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        return false;
	}
	
	public static boolean saveTxtFile(String filePath, String dec) {
		// TODO Auto-generated method stub
		if(StringUtil.isEmptyOrNull(dec)) return false;
        try {
        	File file = new File(filePath);  
        	File dir = new File(file.getParent());
			if(!dir.exists()) dir.mkdirs();
			file.createNewFile();
			OutputStreamWriter out = new OutputStreamWriter(
            		new FileOutputStream(file),"utf8");//考虑到编码格式
			out.write(dec,0,dec.length());    
			out.flush();  
			out.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        return false;
	}
	public static String getFileDir(String filePath){
		int i = filePath.lastIndexOf(File.separator);
		if(i>=0){
			return filePath.substring(0,i);
		}else{
			return "";
		}
	}

	public static boolean deleteFile(String path, boolean force_delete) {
		Logger.e(TAG, "deleteFile: "+path);
		// TODO Auto-generated method stub
		File f = new File(path);
		if(!f.isDirectory()) {
			return f.delete();
		}
		String[] children = f.list();
		if(children!=null && children.length>0 && force_delete){
			for (String child_path: children) {
                if(!deleteFile(path + "/" + child_path, force_delete))
                	return false;
            }
			
		}
		return f.delete();
	}

	public static boolean mkdirs(String filePath, boolean isDir) {
		// TODO Auto-generated method stub
		Logger.e(TAG, "mkdirs: "+filePath);
		File f = new File(filePath);
		if(f.exists()) return true;
		if(!f.exists()) {
			if(isDir){
				f.mkdirs();
			}else {
				File d = f.getParentFile();
				if(d != null && !d.exists()){
					d.mkdirs();
				}
			}
		}
		return true;
	}
	
	public static boolean copyFile(String srcPath, String destPath, boolean overwrite_force){ 
		Logger.e(TAG, "copyFile: srcPath="+srcPath+",dest="+destPath+",overwrite_force="+overwrite_force);
		//if(!src.isFile()||!dest.isFile()) return false;
		try {
			File src = new File(srcPath);
			if(!src.exists()) {
				Logger.e(TAG, "copyFile: srcPath not exist.");
				return false;
			}
			File dest = new File(destPath);
			if(dest.exists() && overwrite_force){
				dest.delete();
			}
	        InputStream in = new FileInputStream(src);  
	        OutputStream out = new FileOutputStream(dest);  
	  
	        byte[] buffer = new byte[1024];  
	  
	        int length;  
	          
	        while ((length = in.read(buffer)) > 0) {  
	            out.write(buffer, 0, length);  
	        }  
	        in.close();  
	        out.close(); 
		    return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	} 
	public static boolean copyFolder(String srcPath, String destPath, boolean overwrite_force){ 
		Logger.e(TAG, "copyFolder: srcPath="+srcPath+",dest="+destPath+",overwrite_force="+overwrite_force);
		//if(!src.isDirectory()||!dest.isDirectory()) return false;
		try {
			File src = new File(srcPath);
			if(!src.exists()) {
				Logger.e(TAG, "copyFolder: srcPath not exist.");
				return false;
			}
			File dest = new File(destPath);
	        if (!dest.exists()) {  
	            dest.mkdirs();  
	        }  
	        File[] files = src.listFiles();  
	        if(files != null) {
		        for (File file : files) {
		            // 递归复制  
		            if(file.isDirectory()){
		            	copyFolder(file.getAbsolutePath(), destPath+"/"+file.getName(), overwrite_force);  
		            }else{
		            	copyFile(file.getAbsolutePath(), destPath+"/"+file.getName(), overwrite_force);  
		            }
		        }  
	        }else {
	        	Logger.e(TAG, "copyFolder: src.list()=null");
	        }
		    
		    return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}  
	public static boolean copyFolderOrFile(File src, File dest){ 
		try {
		    if (src.isDirectory()) {  
		        if (!dest.exists()) {  
		            dest.mkdirs();  
		        }  
		        String files[] = src.list();  
		        for (String file : files) {  
		            File srcFile = new File(src, file);  
		            File destFile = new File(dest, file);  
		            // 递归复制  
		            copyFolderOrFile(srcFile, destFile);  
		        }  
		    } else {  
		        InputStream in = new FileInputStream(src);  
		        OutputStream out = new FileOutputStream(dest);  
		  
		        byte[] buffer = new byte[1024];  
		  
		        int length;  
		          
		        while ((length = in.read(buffer)) > 0) {  
		            out.write(buffer, 0, length);  
		        }  
		        in.close();  
		        out.close();  
		    } 
		    return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}  
	
}
