package com.example.rowetalk.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.PendingIntent.CanceledException;
import android.os.Environment;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class Logger {

    public static final int LOG_LEVEL_NONE = 0;
    public static final int LOG_LEVEL_ERROR = 1;
    public static final int LOG_LEVEL_WARN = 2;
    public static final int LOG_LEVEL_INFO = 3;
    public static final int LOG_LEVEL_VERBOSE = 4;
    public static final int LOG_LEVEL_DEBUG = 5;

    private static int LogLevel = LOG_LEVEL_DEBUG;

    private static boolean LOG_ERROR = LogLevel >= LOG_LEVEL_ERROR;
    private static boolean LOG_WARN = LogLevel >= LOG_LEVEL_WARN;
    private static boolean LOG_INFO = LogLevel > LOG_LEVEL_INFO;
    private static boolean LOG_VERBOSE = LogLevel >= LOG_LEVEL_VERBOSE;
    private static boolean LOG_DEBUG = LogLevel >= LOG_LEVEL_DEBUG;

    private static Boolean MYLOG_WRITE_TO_FILE = false;// 日志写入文件开关 //hongbiao.zhang@2015.9.16: change
    private static String MYLOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().getPath()+"/logs/";// 日志文件在sdcard中的路径
    private static int SDCARD_LOG_FILE_SAVE_DAYS = 2;// sd卡中日志文件的最多保存天数
    private static String MYLOGFILEName = "RoweTalk";// 本类输出的日志文件名称
      
    private static Logger instance = null;
    private FileWriterThread mWriterThread = new FileWriterThread();

    // http://xiangqianppp-163-com.iteye.com/blog/1417743 写日志文件地址

    public static int getLogLevel() {
        return Logger.LOG_LEVEL_DEBUG;
    }

    public static boolean isLogToFile() {
		return MYLOG_WRITE_TO_FILE;
	}

	public static void setLogToFile(boolean mYLOG_WRITE_TO_FILE) {
		MYLOG_WRITE_TO_FILE = mYLOG_WRITE_TO_FILE;
	}

	public static void setLogLevel(int logLevel) {
        LogLevel = logLevel;
        LOG_ERROR = LogLevel >= LOG_LEVEL_ERROR;
        LOG_WARN = LogLevel >= LOG_LEVEL_WARN;
        LOG_INFO = LogLevel > LOG_LEVEL_INFO;
        LOG_VERBOSE = LogLevel >= LOG_LEVEL_VERBOSE;
        LOG_DEBUG = LogLevel >= LOG_LEVEL_DEBUG;
    }

    public static void e(String tag, String msg) {
        if (Logger.LOG_ERROR) {
            Log.e(tag, msg);
        }
        if (MYLOG_WRITE_TO_FILE){
            writeLogtoFile(String.valueOf("e"), tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (Logger.LOG_ERROR) {
            Log.e(tag, msg, tr);
        }
        if (MYLOG_WRITE_TO_FILE){
            writeLogtoFile(String.valueOf("e"), tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (Logger.LOG_WARN) {
            Log.w(tag, msg);
        }
        if (MYLOG_WRITE_TO_FILE) {
            writeLogtoFile(String.valueOf("w"), tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (Logger.LOG_WARN) {
            Log.w(tag, msg, tr);
        }
        if (MYLOG_WRITE_TO_FILE) {
            writeLogtoFile(String.valueOf("w"), tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (Logger.LOG_INFO) {
            Log.i(tag, msg);
        }
        if (MYLOG_WRITE_TO_FILE) {
            writeLogtoFile(String.valueOf("i"), tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (Logger.LOG_VERBOSE) {
            Log.v(tag, msg);
        }
        if (MYLOG_WRITE_TO_FILE) {
            writeLogtoFile(String.valueOf("v"), tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (Logger.LOG_DEBUG) {
            Log.d(tag, msg);
        }
        if (MYLOG_WRITE_TO_FILE) {
            writeLogtoFile(String.valueOf("d"), tag, msg);
        }
    }

    private static String substringTag(String tag) {
        if (StringUtil.isNotEmpty(tag) && tag.length() > 22) {
            return tag.substring(0, 22);
        } else {
            return tag;
        }
    }

    /**
     * 打开日志文件并写入日志
     * 
     * @return
     * **/
    private static void writeLogtoFile(String mylogtype, String tag, String text) {// 新建或打开日志文件        
    	if(instance == null){
    		instance = new Logger();
    	}
    	instance.logFile(mylogtype, tag, text);
        /*try {
            boolean hasSDCard = Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED);
            if (!hasSDCard) {
                return;
            }
            File d = new File(MYLOG_PATH_SDCARD_DIR);
            if(!d.exists()) {
            	d.mkdirs();
            }
            Date nowtime = new Date();
            String needWriteFiel = logfile.format(nowtime);
            String needWriteMessage = myLogSdf.format(nowtime) + "    " + mylogtype
                    + "    " + tag + "    " + text;
            
            File file = new File(MYLOG_PATH_SDCARD_DIR, MYLOGFILEName + needWriteFiel + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(needWriteMessage);
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }*/
    }
    

    /**
     * 删除制定的日志文件
     * */
    public static void delFile() {// 删除日志文件
        String needDelFiel = DateUtil.simpleDateFormat.format(getDateBefore());
        File file = new File(MYLOG_PATH_SDCARD_DIR, MYLOGFILEName + needDelFiel
                + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
     * */
    private static Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE)
                - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }
    
    private void logFile(String type, String tag, String message){
    	if(!mWriterThread.isRunning()){
    		mWriterThread.setName("FileWriterThread");
    		mWriterThread.start();
    	}
    	mWriterThread.putMessage(new FileWriterString(new Date(), type, tag, message));
    }
    
    private class FileWriterString {
    	public Date date;
    	public String type;
    	public String tag;
    	public String message;
    	public FileWriterString(Date date, String type, String tag, String message) {
    		this.date = date;
    		this.type = type;
    		this.tag = tag;
    		this.message = message;
    	}
    	
    }
    
    private class FileWriterThread extends Thread {
    	private boolean running = false; 
    	private List<FileWriterString> msgList = new ArrayList<FileWriterString>();
    	
    	public void putMessage(FileWriterString string){
    		synchronized (msgList) {
    			if(msgList.size()>1000){
    				msgList.clear();
    				msgList.add(new FileWriterString(new Date(), "e", "", "clear list because list size exceeds 1000"));
    			}
    			msgList.add(string);
    			msgList.notifyAll();
			}
    	}
    	public boolean isRunning() {
			return running;
		}
    	public void cancel() {
    		if(running){
    			running = false;
    			synchronized (msgList) {
    				msgList.clear();
    			}
    			interrupt();
    		}
    	}
    	
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//super.run();
			running = true;
			List<FileWriterString> list = new ArrayList<Logger.FileWriterString>();
			long lastMs = System.currentTimeMillis();
			while(running) {
				synchronized (msgList) {
					if(msgList.size()==0){
						try {
							msgList.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if(msgList.size()>0 &&(System.currentTimeMillis()-lastMs)>1000){
			            boolean hasSDCard = Environment.getExternalStorageState().equals(
			                    Environment.MEDIA_MOUNTED);
			            if (!hasSDCard) {
			            	ThreadSleep.Sleep(2000);
			            }else {
				            File d = new File(MYLOG_PATH_SDCARD_DIR);
				            if(!d.exists()) {
				            	d.mkdirs();
				            }
				            for(FileWriterString s: msgList){
								list.add(s);
							}
				            msgList.clear();
			            }
					}
				}
				if(list.size()>0){
					for(FileWriterString s: list){
						write_log(s);
					}
					list.clear();
				}
			}
		}
		
		private void write_log(FileWriterString s){
			String needWriteFiel = DateUtil.simpleDateFormat.format(s.date);
            String needWriteMessage = DateUtil.simpleDateFormatLong.format(s.date) + "    " + s.type
                    + "    " + s.tag + "    " + s.message;
            try {
	            File file = new File(MYLOG_PATH_SDCARD_DIR, MYLOGFILEName + needWriteFiel + ".txt");
	            if (!file.exists()) {
	                file.createNewFile();
	            }
	            FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
	            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
	            bufWriter.write(needWriteMessage);
	            bufWriter.newLine();
	            bufWriter.close();
	            filerWriter.close();
            }catch(IOException e){
            	e.printStackTrace();
            }
		}
    	
    }
}
