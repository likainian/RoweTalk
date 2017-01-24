package com.example.rowetalk.thread;

import android.content.pm.PackageInfo;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.example.rowetalk.activity.MainActivity;
import com.example.rowetalk.service.MainService;
import com.example.rowetalk.util.Config;
import com.example.rowetalk.util.Constant;
import com.example.rowetalk.util.DateUtil;
import com.example.rowetalk.util.HttpUtil;
import com.example.rowetalk.util.HttpUtil.ProgressHandler;
import com.example.rowetalk.util.Logger;
import com.example.rowetalk.util.NetworkUtil;
import com.example.rowetalk.util.RootShellCmd;
import com.example.rowetalk.util.StringUtil;
import com.example.rowetalk.util.SystemUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateThread extends Thread {
    private static final String TAG = UpdateThread.class.getName();
    private MainService mService;
    private boolean isRunning = false;
    private boolean isWaiting = false;
    private Object lock = new Object();
    private Handler mHandler;

    public UpdateThread(MainService service, Handler handler) {
        mService = service;
        mHandler = handler;
        setName("UpdateThread");
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp/";
        File d = new File(dirPath);
        if (!d.exists()) d.mkdirs();
        Log.e(TAG, "start work");
        isRunning = true;
        while (isRunning) {
            isWaiting = false;
            if (NetworkUtil.isNetworkConnected(mService)) {
                if (!NetworkUtil.connectedWifi(mService, Config.getString("wifi_addr", null))) { // update should be in wifi environment
                    isWaiting = true;
                    Logger.e(TAG, "wait 5 minutes until wifi connected.");
                    waitSignal(5 * 60 * 1000);//wait 5 minutes
                    continue;
                }
                UpdateInfo info = checkVersion(SystemUtil.getPackageInfo(mService));
                if (info != null) {
                    info.filePath = dirPath + "local.apk";
                    showUpdateStatus(info, "downloading");
                    //mMainActivity.updateStatus("new version: "+info.versionName+"-"+info.versionCode);
                    if (downloadVersion(info)) {
                        mHandler.obtainMessage(MainService.MSG_UPDATE_APP, info).sendToTarget();
                    }
                }
                String pPackageName = "com.example.autobotservice";
                PackageInfo pInfo = SystemUtil.getPackageInfo(mService, pPackageName);
                if (pInfo != null) {
                    info = checkVersion(pInfo);
                    if (info != null) {
                        info.filePath = dirPath + "autobotservice.apk";
                        if (downloadVersion(info)) {
                            if (RootShellCmd.getInstance().installApp(info.filePath, pPackageName)) {
                                Logger.e(TAG, "update autoservice apk success.");
                            }
                        }
                    }
                }
            }
            isWaiting = true;
            waitSignal(DateUtil.HOUR);
        }
        isWaiting = false;
    }

    private void waitSignal(long ms) {
        synchronized (lock) {
            try {
                lock.wait(ms);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public boolean isWaiting() {
        return isWaiting;
    }

    public void immediate_check_update() {
        Logger.e(TAG, "immediate_check_update");
        synchronized (lock) {
            lock.notify();
        }
    }

    public void cancel() {
        // TODO Auto-generated method stub
        isRunning = false;
        synchronized (lock) {
            lock.notify();
        }
        interrupt();
    }

    private UpdateInfo checkVersion(PackageInfo pInfo) {
        if (pInfo == null) {
            Logger.e(TAG, "checkVersion: PackageInfo is null");
            return null;
        }
        Logger.e(TAG, "checkVersion: " + pInfo.packageName + " " + pInfo.versionCode);
        if (!NetworkUtil.isNetworkConnected(mService)) {
            return null;
        }
        try {
            OkHttpClient client = new OkHttpClient();
            FormBody.Builder builder = new FormBody.Builder()
                    .add("packageName", pInfo.packageName)
                    .add("phoneSeq", Integer.toString(MainActivity.mPhoneSeq))
                    .add("versionCode", Integer.toString(pInfo.versionCode));
            Request request = new Request.Builder()
                    .url(Constant.getURL_UPDATE_CHECK_VERSION())
                    .post(builder.build())
                    .build();
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);
            String body = response.body().string();
            JSONObject rspObject = new JSONObject(body);
            String errorNo = rspObject.optString("errorNo");
            Logger.e(TAG, "checkVersion: response erroNo=" + errorNo);
            if (errorNo != null && errorNo.equals("0")) {
                JSONObject dataObj = rspObject.optJSONObject("data");
                if (dataObj != null) {
                    String appId = dataObj.optString("app_id");
                    if (!StringUtil.isEmptyOrNull(appId)) {
                        UpdateInfo info = new UpdateInfo();
                        info.appId = appId;
                        info.packageName = dataObj.optString("package_name");
                        info.launcherActivity = dataObj.optString("launcher_activity");
                        info.versionName = dataObj.optString("version_name");
                        info.versionCode = dataObj.optInt("version_code");
                        info.updateType = dataObj.optInt("update_type");
                        info.publishType = dataObj.optInt("version_code");
                        info.fileSize = dataObj.optInt("file_size");
                        info.publishTime = new Date(dataObj.optLong("publish_time"));
                        Logger.e(TAG, "checkVersion: get info: " + info);
                        return info;
                    }
                } else {
                    Logger.e(TAG, "checkVersion: no data available");
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "checkVersion Exception: " + e);
        }
        return null;
    }

    private boolean downloadVersion(final UpdateInfo info) {
        String url = Constant.getURL_DOWNLOAD_APP_VERSION();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("app_id", info.appId);
        Logger.e(TAG, "downloadVersion app_id=" + info.appId);
        return HttpUtil.downloadFile(url, params, info.filePath, new ProgressHandler() {

            @Override
            public void updateSize(long size) {
                // TODO Auto-generated method stub
                Logger.e(TAG, "downloadVersion updateSize " + size);
                float percent = (size * 100) / info.fileSize;
                showUpdateStatus(info, String.format("%d%%", (int) percent));
            }
        });
    }

    private void showUpdateStatus(UpdateInfo info, String content) {
        mService.updateStatus("new version: " + info.versionName + "-" + info.versionCode + " " + content);
    }

    public static class UpdateInfo {
        public String appId;
        public String packageName;
        public String launcherActivity;
        public String versionName;
        public int versionCode;
        public int fileSize;
        public int updateType;
        public int publishType;
        public Date publishTime;
        public String filePath;

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return "appId=" + appId +
                    ",packageName=" + packageName +
                    ",launcherActivity=" + launcherActivity +
                    ",versionName=" + versionName +
                    ",versionCode=" + versionCode +
                    ",updateType=" + updateType +
                    ",publishType=" + publishType +
                    ",fileSize=" + fileSize +
                    ",publishTime=" + publishTime +
                    ",filePath=" + filePath;
        }


    }

}
