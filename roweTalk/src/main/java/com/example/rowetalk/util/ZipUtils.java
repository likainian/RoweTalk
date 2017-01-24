package com.example.rowetalk.util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.v1_10.binary.Base64;

public class ZipUtils {

	private static final String TAG = ZipUtils.class.getName();
	/**
	
	* 使用gzip进行压缩
	*/
	public static String gzip(String primStr) {
		if (primStr == null || primStr.length() == 0) {
			return primStr;
		}
	
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	
		GZIPOutputStream gzip=null;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(primStr.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(gzip!=null){
				try {
					gzip.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	
		return Base64.encodeBase64String(out.toByteArray());
	}
	
	/**
	*
	* <p>Description:使用gzip进行解压缩</p>
	* @param compressedStr
	* @return
	*/
	public static String gunzip(String compressedStr){
		if(compressedStr==null){
			return null;
		}
	
		ByteArrayOutputStream out= new ByteArrayOutputStream();
		ByteArrayInputStream in=null;
		GZIPInputStream ginzip=null;
		byte[] compressed=null;
		String decompressed = null;
		try {
			compressed = Base64.decodeBase64(compressedStr);
			in=new ByteArrayInputStream(compressed);
			ginzip=new GZIPInputStream(in);
			
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = ginzip.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed=out.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ginzip != null) {
				try {
					ginzip.close();
				} catch (IOException e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		
		return decompressed;
	}
	
	/**
	* 使用zip进行压缩
	* @param str 压缩前的文本
	* @return 返回压缩后的文本
	*/
	public static final String zip(String str) {
		if (str == null)
			return null;
		byte[] compressed;
		ByteArrayOutputStream out = null;
		ZipOutputStream zout = null;
		String compressedStr = null;
		try {
			out = new ByteArrayOutputStream();
			zout = new ZipOutputStream(out);
			zout.putNextEntry(new ZipEntry("0"));
			zout.write(str.getBytes());
			zout.closeEntry();
			compressed = out.toByteArray();
			compressedStr = Base64.encodeBase64String(compressed);
		} catch (IOException e) {
			compressed = null;
		} finally {
			if (zout != null) {
				try {
					zout.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return compressedStr;
	}
	
	/**
	* 使用zip进行解压缩
	* @param compressed 压缩后的文本
	* @return 解压后的字符串
	*/
	public static final String unzip(String compressedStr) {
		if (compressedStr == null) {
			return null;
		}
	
		ByteArrayOutputStream out = null;
		ByteArrayInputStream in = null;
		ZipInputStream zin = null;
		String decompressed = null;
		try {
			byte[] compressed = Base64.decodeBase64(compressedStr);
			out = new ByteArrayOutputStream();
			in = new ByteArrayInputStream(compressed);
			zin = new ZipInputStream(in);
			zin.getNextEntry();
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = zin.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed = out.toString();
		} catch (IOException e) {
			decompressed = null;
		} finally {
			if (zin != null) {
				try {
					zin.close();
				} catch (IOException e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return decompressed;
	}
	
	public static boolean ZipDir(String zipDirectory, boolean skip_hidden){//zipDirectoryPath:需要压缩的文件夹名 
		Logger.e(TAG, "ZipDir: zipDirectory="+zipDirectory);
        //File file; 
        File zipDir = new File(zipDirectory); 
        String zipFileName = zipDir.getAbsolutePath() + ".zip";//压缩后生成的zip文件名 

        try{ 
       	    ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFileName))); 
            handleDir(zipDir , zipOut, zipDirectory, skip_hidden); 
            zipOut.close(); 
            return true;
        }catch(IOException ioe){ 
            ioe.printStackTrace(); 
            //Log.e(TAG, "doZip: IOException"+ioe);
            return false;
        } 
    } 
	
	private static void handleDir(File dir , ZipOutputStream zipOut, String prevPath, boolean skip_hidden)throws IOException{ 
		Logger.e(TAG, "handleDir: dir="+dir);
        FileInputStream fileIn; 
        File[] files; 

        files = dir.listFiles(); 
     
        if(files.length == 0 ){//如果目录为空,则单独创建之. 
            //ZipEntry的isDirectory()方法中,目录以"/"结尾. 
       	 	String relName = dir.getPath().substring(prevPath.length());
       	 	if(relName.startsWith("/")) relName = relName.substring(1);
       	 	//System.out.println("found empty: "+relName);
            zipOut.putNextEntry(new ZipEntry(relName + "/")); 
            zipOut.closeEntry(); 
            Logger.e(TAG, "handleDir: create empty: "+relName + "/");
        } 
        else{//如果目录不为空,则分别处理目录和文件. 
            for(File fileName : files){  
            	if(fileName.isHidden()&&skip_hidden) continue;
                //System.out.println(fileName);
                if(fileName.isDirectory()){ 
                    handleDir(fileName , zipOut, prevPath, skip_hidden); 
                } else { 
                	String relName = fileName.getPath().substring(prevPath.length());
               	 	if(relName.startsWith("/")) relName = relName.substring(1);
                    zipOut.putNextEntry(new ZipEntry(relName)); 
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
                    byte[] b = new byte[1024];
                    int readBytes= 0;
                    while ((readBytes=bis.read(b)) != -1) {
                   	 zipOut.write(b, 0, readBytes);
                    }

                    zipOut.closeEntry(); 
                } 
            } 
        } 
    } 
	
	public static boolean UnzipFile(String srcZipFile, String outputDir) {
		Logger.e(TAG, "UnzipFile: srcZipFile="+srcZipFile+",outputDir="+outputDir);
   		if(outputDir == null){
   			outputDir = srcZipFile.substring(0, srcZipFile.length()-4);
   		}
   		if(!outputDir.endsWith(File.separator)){
   			outputDir += File.separator;
   		}
		new File(outputDir).mkdirs();
        boolean isSuccessful = true;
        try {
             BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcZipFile));
             ZipInputStream zis = new ZipInputStream(bis);

             BufferedOutputStream bos = null;

             //byte[] b = new byte[1024];
             ZipEntry entry = null;
             while ((entry=zis.getNextEntry()) != null) {
             	String entryName = entry.getName();
             	if(entryName.endsWith("/")) {
                   	File f = new File(outputDir + entryName);
                   	f.mkdirs();
            	}else{
	                File f = new File(outputDir + entryName);
	                if(f.exists()){
	                    	f.delete();
	                }
                    //如果指定文件的目录不存在,则创建之. 
                    File parent = f.getParentFile(); 
                    if(parent!=null&&!parent.exists()){ 
                        parent.mkdirs(); 
                    } 
                    
                    f.createNewFile();
                    bos = new BufferedOutputStream(new FileOutputStream(f));
                    int b = 0;
                    while ((b = zis.read()) != -1) {
                         bos.write(b);
                    }
                    bos.flush();
                    bos.close();
            	}
             }
             zis.close();
         } catch (IOException e) {
        	 e.printStackTrace();
             isSuccessful = false;
         }
         return isSuccessful;
    }
}