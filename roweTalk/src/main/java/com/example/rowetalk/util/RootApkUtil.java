package com.example.rowetalk.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import android.content.pm.PackageInfo;
import android.os.Environment;
import android.provider.MediaStore.Files;
import android.util.Log;

public class RootApkUtil {
	private static final String TAG = RootApkUtil.class.getName();
	private final static Set<String> ignoreDirs = new HashSet<String>(Arrays.asList(new String[]{".android_secure",
			"LOST.DIR","DCIM", "Ringtones", "Alarms", "Notifications", "Music", "Podcasts", "Pictures", "Movies", "Download", "Android",
			"AutoBot", "rowetalk", ".UTSystemConfig", ".DataStorage", "Kingroot", "Tencent", "tmp", "logs", "tesseract"})) ;
	private final static Set<String> appLibFilter = new HashSet<String>(Arrays.asList(new String[]{"lib", "app_webview"})) ;
	
	
	public static boolean RestorePackageData(RootShellCmd shell, String packageName, String backPath, boolean rmAfterCopy) {
		String privDir = backPath +"/priv";
		String pubDir = backPath +"/pub";
		
		File priv = new File(privDir);
		File pub = new File(pubDir);
		
		if(priv.exists()){
			if(!shell.copyDir(privDir, "/data/data/"+packageName)){
				Logger.e(TAG, "RestorePackageData: copyDir failed. "+privDir);
				return false;
			}
			//FileUtil.copyFolder(privDir, "/data/data/"+packageName, true);
		}
		if(pub.exists()){
			//if(!shell.copyDir(pubDir, SystemUtil.getSdCardPath())){
			if(!FileUtil.copyFolder(pubDir, SystemUtil.getSdCardPath(), true)) {
				Logger.e(TAG, "RestorePackageData: copyDir failed. "+pubDir);
				return false;
			}
		}
		return true;
	}
	
	public static int BackupPackageData(RootShellCmd shell, String packageName, String backPath, String pubDirs, boolean rmAfterCopy){
		shell.killApp(packageName);
		//shell.removeDir(backPath);
		FileUtil.deleteFile(backPath, true);
		String privDir = backPath +"/priv";
		String pubDir = backPath +"/pub";
		//shell.makeDir(privDir);
		FileUtil.mkdirs(privDir, true);
		//shell.makeDir(pubDir);
		FileUtil.mkdirs(pubDir, true);
		
		int count = 0;
		count = backupPrivateData(shell, packageName, privDir, rmAfterCopy);
		if(count < 0) return -1;
		count += backupPublicData(shell, pubDir, pubDirs, rmAfterCopy);
		if(count < 0) return -1;
		
		return count;
	}
	
	private static int backupPrivateData(RootShellCmd shell, String packageName, String outputDir, boolean rmAfterCopy){
		File d2 = new File(outputDir);
		if(d2== null || !d2.exists()){
			Logger.e(TAG, "backupPrivateData: outputDir not found. "+outputDir);
			return 0;
		}
		
		String srcDir = "/data/data/"+packageName;
		if(!shell.copyDir(srcDir, outputDir)){
		//if(!FileUtil.copyFolder(srcDir, outputDir, true)){
			Logger.e(TAG, "backupPrivateData: copyDir failed.");
			return -1;
		}
		try {
			File d = new File(outputDir);
			File[] files = d.listFiles();
			int count = 0;
			if(files != null) {
				for(File f: files){
					if(f.isDirectory()) {
						if(appLibFilter.contains(f.getName())) {
							shell.removeDir(f.getAbsolutePath());
							//FileUtil.deleteFile(f.getAbsolutePath(), true);
						}else {
							Logger.e(TAG, "backupPrivateData: "+f.getAbsolutePath());
							count++;
							if(rmAfterCopy){
								shell.removeDir(srcDir+"/"+f.getName());
								//FileUtil.deleteFile(srcDir+"/"+f.getName(), true);
							}
						}
					}else {
						Logger.e(TAG, "backupPrivateData: "+f.getAbsolutePath());
						count++;
					}
				}
			}
			return count;
		}catch(Exception e){
			Logger.e(TAG, "backupPrivateData: Exception: "+e);
			return 0;
		}
	}
	
	private static int backupPublicData(RootShellCmd shell, String outputDir, String pubDirs, boolean rmAfterCopy) {
		Logger.e(TAG, "backupPublicData: outputDir="+outputDir+",pubDirs="+pubDirs);
		if(StringUtil.isEmptyOrNull(pubDirs)||StringUtil.isEmptyOrNull(outputDir)) return 0;
		if(SystemUtil.getSdCardPath()==null){
			Logger.e(TAG, "cannot get sdcard path");
			return 0;
		}
		int count = 0;
		String[] dirs = pubDirs.split(",");
		for(String dir: dirs){
			dir = dir.trim();
			if(StringUtil.isEmptyOrNull(dir)) continue;
			File f = new File(SystemUtil.getSdCardPath()+"/"+dir);
			if(!f.exists()) continue;
			Logger.e(TAG, "backupPublicData: "+dir);
			String dirPath = f.getAbsolutePath();
			String relPath = dirPath.substring(SystemUtil.getSdCardPath().length());
			String newPath = outputDir+relPath;
			if(f.isDirectory()) {
				//shell.makeDir(newPath);
				FileUtil.mkdirs(newPath, true);
				//if(!shell.copyDir(dirPath, newPath)){
				if(!FileUtil.copyFolder(dirPath, newPath, true)){
					Logger.e(TAG, "backupPublicData: copyDir failed.");
					return -1;
				}
			}else{
				//if(!shell.copyFile(dirPath, newPath)){
				if(!FileUtil.copyFile(dirPath, newPath, true)){
					Logger.e(TAG, "backupPublicData: copyFile failed.");
					return -1;
				}
			}
			if(rmAfterCopy){
				//shell.removeDir(dirPath);
				FileUtil.deleteFile(dirPath, true);
			}
			count++;
		}
		return count;
	}
	
	private static boolean backupPublicData2(RootShellCmd shell, String dir, String outputDir, boolean rmAfterCopy) {
		File d = new File(outputDir);
		if(d== null || !d.exists()){
			Logger.e(TAG, "backupPublicData: outputDir not found. "+outputDir);
			return false;
		}
		
		if(dir == null) return true;
		
		Logger.e(TAG, "backupPublicData: "+dir);
		
		try {
			shell.clearDir(dir+"/Download");
			d = new File(dir);
			if(d.isFile()) return true;
			/*List<String> list = FileUtil.ReadTxtFile(dir+"/.keep");
			if(root && list.size() == 0){
				Logger.e(TAG, "copyModifiedPubData: error, no .keep in root dir.");
				return false;
			}*/
			// found the minimized time
			long min_time = new Date().getTime();
			File[] files = d.listFiles();
			for(File f: files){
				if(f.lastModified() < min_time){
					min_time = f.lastModified();
				}
			}
			for(File f: files){
				if(f.lastModified() <= min_time)
					continue;
				if(f.getName().equals(".") || f.getName().equals(".."))
					continue;
				if(ignoreDirs.contains(f.getName()))
					continue;
				if(f.isDirectory()){
					String dirPath = dir+"/"+f.getName();
					String relPath = dirPath.substring(SystemUtil.getSdCardPath().length());
					String newPath = outputDir+relPath;
					shell.makeDir(newPath);
					shell.copyDir(dirPath, newPath);
					if(rmAfterCopy){
						shell.removeDir(dirPath);
					}
				}else{
					String relPath = f.getAbsolutePath().substring(SystemUtil.getSdCardPath().length());
					String filePath = outputDir+relPath;
					shell.makeDir(FileUtil.getFileDir(filePath));
					shell.copyFile(f.getAbsolutePath(), filePath);
					if(rmAfterCopy){
						shell.rmFile(f.getAbsolutePath());
					}
				}
			}

			return true;
		}catch(Exception e){
			Logger.e(TAG, "keepDir "+dir+" Exception: "+e);
			return false;
		}
	}
	
}
