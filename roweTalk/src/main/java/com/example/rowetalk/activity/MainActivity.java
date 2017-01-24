package com.example.rowetalk.activity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import com.example.rowetalk.R;
import com.example.rowetalk.bean.Device;
import com.example.rowetalk.bean.EnumType;
import com.example.rowetalk.bean.EnvProfile;
import com.example.rowetalk.bean.Platform;
import com.example.rowetalk.bean.SystemUpdate;
import com.example.rowetalk.bean.VpnAccount;
import com.example.rowetalk.bean.VpnProfile;
import com.example.rowetalk.service.MainService;
import com.example.rowetalk.thread.BaseThread;
import com.example.rowetalk.thread.VerifyCardThread;
import com.example.rowetalk.util.Config;
import com.example.rowetalk.util.Constant;
import com.example.rowetalk.util.Generator;
import com.example.rowetalk.util.Logger;
import com.example.rowetalk.util.NetworkUtil;
import com.example.rowetalk.util.PlatformUtil;
import com.example.rowetalk.util.RootShellCmd;
import com.example.rowetalk.util.ServerUtil;
import com.example.rowetalk.util.SleepObject;
import com.example.rowetalk.util.StringUtil;
import com.example.rowetalk.util.SystemUpdateUtil;
import com.example.rowetalk.util.SystemUtil;
import com.liblua.LuaEnv;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnCheckedChangeListener {
	private static final String TAG = MainActivity.class.getName();
	private Button bnStartTask;
	private Button bnStop;
	private TextView tvServerAddr;
	private TextView tvBtDev;
	private Button bnSelectBt;
	private Button bnSysProps;
	private Button bnExecutScript;
	private CheckBox cbLogFile;


	private Pattern mUrlPattern = Pattern.compile("http://(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?", Pattern.CASE_INSENSITIVE); //(http|ftp|https)

	private String mBtName, mBtAddr;
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothDevice mBluetoothDevice;
	
	private static final int REQUEST_BROWSE_BT_DEVICES = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_BROWSE_MIFI_DEVICES = 3;
    private static final int REQUEST_BROWSE_WIFI_DEVICES = 4;
    private static final int REQUEST_SETUP_PARAMS = 5;
    private static final int REQUEST_SETUP_VPN = 6;


	
	private TextView tvSimInfo;
	private TextView tvSimStart;
	private TextView tvFalseSlots;
	private TextView tvFalseTasks;
	private TextView tvCurrentSim;
	
	private Button bnClearLog;
	private Button bnSwitchCard;
	private CheckBox cbEnableMifi;
	private TextView tvMifiDev;
	private Button bnSelectMifi;
	private String mMifiName, mMifiAddr;
	private CheckBox cbEnable4G;
	private CheckBox cbEnableTest;
	private TextView tvWifiDev;
	private Button bnSelectWifi;
	private String mWifiName, mWifiAddr;
	private Button bnSetupParams;
	private TextView tvParams;
	private CheckBox cbAutoStart;
	private CheckBox cbEnableVPN;
	private Button bnSelectVpn;
	private TextView tvVpnDev;
	private CheckBox cbSpecifyVPN;
	private CheckBox cbTestVPN;
	private CheckBox cbNoSim;
	//private CheckBox cbOverseas;
	private CheckBox cbOcrEnglish;
	private CheckBox cbOcrChinese;
	private CheckBox cbReportIP;
	
	

	private int mNoSimVdevieNum;
	private int mNoSimStart;
	private int mSimStart;
	public static int mPhoneSeq = 0;
	private int mPoolSeq = 0;
	private String mSlotSeqs;
	private String mFalseSlots, mFalseTasks, mSuccessTasks;
    private final DateFormat logDF = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.CHINA);
    private ListView mListView;
    private List<String> mLogList = new ArrayList<String>();
    private ArrayAdapter<String> mListAdapter;
	
	private String mVpnName;
	private String mVpnCity;
	private String mVpnServer;
	private String mVpnUsername;
	private String mVpnPassword;
	private String mVpnDns;
	private boolean mVpnPpe;
	private int mImeWaitTime = 0;

	public static final String ACTION_UPDATE_APP = "ty.intent.action.update_app";
	private WifiManager mWifiManager;
	
	public static boolean exist = false;
	private VerifyCardThread mVerifyCardThread;
	private TextView tvSuccessTasks;
	private SleepObject sleepObject = new SleepObject();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.e(TAG, "onCreate");


		bnStartTask = (Button)findViewById(R.id.bnSelectBT);
		bnStartTask = (Button)findViewById(R.id.bnStartTask);
		bnStop = (Button)findViewById(R.id.bnStop);
		bnClearLog = (Button)findViewById(R.id.bnClear);
		bnExecutScript = (Button)findViewById(R.id.bnExecutScript);
		bnSwitchCard = (Button)findViewById(R.id.bnSwitchCard);
		bnSysProps = (Button)findViewById(R.id.bnSysProps);
		bnSelectMifi = (Button)findViewById(R.id.bnSelectMifi);
		bnSelectWifi = (Button)findViewById(R.id.bnSelectWifi);
		bnSetupParams = (Button)findViewById(R.id.bnSetupParams);
		bnSelectVpn = (Button)findViewById(R.id.bnSelectVpn);
		mListView = (ListView)findViewById(R.id.lvLogs);
		
		bnStartTask.setOnClickListener(this);
		bnStop.setOnClickListener(this);
		bnClearLog.setOnClickListener(this);
		bnStop.setEnabled(false);
		bnExecutScript.setOnClickListener(this);
		bnSwitchCard.setOnClickListener(this);
		bnSysProps.setOnClickListener(this);
		bnSelectMifi.setOnClickListener(this);
		bnSelectWifi.setOnClickListener(this);
		bnSetupParams.setOnClickListener(this);
		bnSelectVpn.setOnClickListener(this);
		bnSelectBt.setOnClickListener(this);
		bnSelectBt.setEnabled(false);
				
		// 头部view：设置view的点击时间，用于屏蔽listview的item的点击事件
        View headerView = getLayoutInflater().inflate(R.layout.main_header, null);
        tvBtDev = (TextView)headerView.findViewById(R.id.tvBtDev);
        tvServerAddr = (TextView)headerView.findViewById(R.id.tvServerAddr);
		tvSimInfo = (TextView)headerView.findViewById(R.id.tvSimInfo);
		tvSimStart = (TextView)headerView.findViewById(R.id.tvSimStart);
		tvFalseSlots = (TextView)headerView.findViewById(R.id.tvFalseSlots);
		tvFalseTasks = (TextView)headerView.findViewById(R.id.tvFalseTasks);
		tvCurrentSim = (TextView)headerView.findViewById(R.id.tvCurrentSim);
		tvParams = (TextView)headerView.findViewById(R.id.tvParams);
		cbLogFile = (CheckBox)headerView.findViewById(R.id.cbLogFile);
		cbAutoStart = (CheckBox)headerView.findViewById(R.id.cbAutoStart);
		cbEnableVPN = (CheckBox)headerView.findViewById(R.id.cbEnableVPN);
		cbEnableMifi = (CheckBox)headerView.findViewById(R.id.cbEnableMifi);
		tvMifiDev = (TextView)headerView.findViewById(R.id.tvMifiDev);
		cbEnable4G = (CheckBox)headerView.findViewById(R.id.cbEnable4G);
		cbEnableTest = (CheckBox)headerView.findViewById(R.id.cbEnableTest);
		tvWifiDev = (TextView)headerView.findViewById(R.id.tvWifiDev);
		cbSpecifyVPN = (CheckBox)headerView.findViewById(R.id.cbSpecifyVPN);
		cbTestVPN = (CheckBox)headerView.findViewById(R.id.cbTestVPN);
		cbNoSim = (CheckBox)headerView.findViewById(R.id.cbNoSim);
		tvVpnDev = (TextView)headerView.findViewById(R.id.tvVpnDev);
		//cbOverseas = (CheckBox)headerView.findViewById(R.id.cbOverseas);
		tvSuccessTasks = (TextView)headerView.findViewById(R.id.tvSuccessTasks);
		cbOcrEnglish = (CheckBox)headerView.findViewById(R.id.cbOcrEnglish);
		cbOcrChinese = (CheckBox)headerView.findViewById(R.id.cbOcrChinese);
		cbReportIP = (CheckBox)headerView.findViewById(R.id.cbReportIP);
		
        mListView.addHeaderView(headerView);
	    mListAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,mLogList);  
	    mListView.setAdapter(mListAdapter);
		
		// init server host
		String s = Config.getString("server_host", null);
		if(!StringUtil.isEmptyOrNull(s)) {
			Constant.setSERVER_HOST(s);
		}
		tvServerAddr.setText(Constant.getSERVER_HOST());
		
		// init bt
		mBtName = Config.getString("bt_name", null);
        mBtAddr = Config.getString("bt_addr", null);
    	tvBtDev.setText(mBtName!=null?mBtName+" "+mBtAddr:"");
    	// init mifi
    	mMifiName = Config.getString("mifi_name", null);
        mMifiAddr = Config.getString("mifi_addr", null);
		tvMifiDev.setText(mMifiName!=null?mMifiName+" "+mMifiAddr:"");
		// init wifi
		mWifiName = Config.getString("wifi_name", null);
        mWifiAddr = Config.getString("wifi_addr", null);
        tvWifiDev.setText(mWifiName!=null?mWifiName+" "+mWifiAddr:"");
        // init params
		mPhoneSeq = Config.getInt("phone_seq", 0);
		mPoolSeq = Config.getInt("pool_seq", 0);
		mSlotSeqs = Config.getString("slot_seqs", "");
		mNoSimVdevieNum = Config.getInt("nosim_vdevice_num", 0);
		mNoSimStart = Config.getInt("nosim_start", 0);
		mImeWaitTime = Config.getInt("ime_wait_time", 0);

		mSimStart = Config.getInt("sim_start", 0);
		setupParams(false);
		// init vpn
		mVpnName = Config.getString("vpn_name", null);
        mVpnCity = Config.getString("vpn_city", null);
        mVpnServer = Config.getString("vpn_server", null);
        mVpnUsername = Config.getString("vpn_username", null);
        mVpnPassword = Config.getString("vpn_password", null);
        mVpnDns = Config.getString("vpn_dns", "8.8.8.8");
        mVpnPpe = Config.getBoolean("vpn_ppe", true);
        setupVpn(false);
        // init checkbox
        initCheckbox();
        
		
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE); 
		
		showVersion("");
        startService(new Intent(this, MainService.class));
        
	}
	
	@Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
        	bnSelectBt.setEnabled(true);
        }
        Logger.e(TAG, "onStart");
        
        
        IntentFilter intentFilter = new IntentFilter();   
        intentFilter.addAction(MainService.ACTION_UI_MSG);
        registerReceiver(mBroadcastReceiver, intentFilter);  
    }
	
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if(!SystemUtil.isTyImeEnabled(this)){
        	Toast.makeText(this, "TyIME APP not installed or not enabled, quit!", Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
		synchronized (MainService.mReservedUIMsgs) {
			while(MainService.mReservedUIMsgs.size()>0){
				Message msg = MainService.mReservedUIMsgs.remove(0);
                mHandler.handleMessage(msg);
			}
		}
		exist = true;
		
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
		        if(Config.getBoolean("server_running", false)){
		        	disableWhenTaskRunning();
				}else{
					enableWhenTaskStopped();
				}
				
			}
		}, 1000);
	}
	
	

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		exist = false;
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		unregisterReceiver(mBroadcastReceiver);  
		Logger.e(TAG, "onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {  
        case REQUEST_BROWSE_BT_DEVICES:
        	if (resultCode == Activity.RESULT_OK) {
        		mBtName = data.getStringExtra(BtDeviceListActivity.EXTRA_DEVICE_NAME);
            	mBtAddr = data.getStringExtra(BtDeviceListActivity.EXTRA_DEVICE_ADDR);
            	tvBtDev.setText(mBtName!=null?mBtName+" "+mBtAddr:"");
        		Config.putString("bt_name", mBtName);
        		Config.putString("bt_addr", mBtAddr);
            }        	
        	break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                bnSelectBt.setEnabled(true);
            } else {
                // User did not enable Bluetooth or an error occurred
                Logger.e(TAG, "BT not enabled");
                Toast.makeText(this, "蓝牙未能打开，APP退出！",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        case REQUEST_BROWSE_MIFI_DEVICES:
        	if (resultCode == Activity.RESULT_OK) {
        		mMifiName = data.getStringExtra(MifiDeviceListActivity.EXTRA_DEVICE_NAME);
            	mMifiAddr = data.getStringExtra(MifiDeviceListActivity.EXTRA_DEVICE_ADDR);
            	tvMifiDev.setText(mMifiName!=null?mMifiName+" "+mMifiAddr:"");
            	Config.putString("mifi_name", mMifiName);
            	Config.putString("mifi_addr", mMifiAddr);
            }     
        	break;
        case REQUEST_BROWSE_WIFI_DEVICES:
        	if (resultCode == Activity.RESULT_OK) {
        		mWifiName = data.getStringExtra(MifiDeviceListActivity.EXTRA_DEVICE_NAME);
            	mWifiAddr = data.getStringExtra(MifiDeviceListActivity.EXTRA_DEVICE_ADDR);
            	tvWifiDev.setText(mWifiName!=null?mWifiName+" "+mWifiAddr:"");
        		Config.putString("wifi_name", mWifiName);
        		Config.putString("wifi_addr", mWifiAddr);
            }     
        	break;
        case REQUEST_SETUP_PARAMS:
        	if (resultCode == Activity.RESULT_OK) {
        		mPhoneSeq = data.getIntExtra(SetupParamsActivity.EXTRA_PHONE_SEQ, 0);
        		if(!cbNoSim.isChecked()){
	        		mPoolSeq = data.getIntExtra(SetupParamsActivity.EXTRA_POOL_SEQ, 0);
	        		mSlotSeqs = data.getStringExtra(SetupParamsActivity.EXTRA_SLOT_SEQS);
	 	        	mSimStart = data.getIntExtra(SetupParamsActivity.EXTRA_SIM_START, 0);
        		}
        		mNoSimVdevieNum = data.getIntExtra(SetupParamsActivity.EXTRA_NOSIM_VDEVICE_NUM, 0);
        		mNoSimStart = data.getIntExtra(SetupParamsActivity.EXTRA_NOSIM_START, 0);
        		mImeWaitTime = data.getIntExtra(SetupParamsActivity.EXTRA_IME_WAIT_TIME, 0);
        		setupParams(true);
        	}
        	break;
        case REQUEST_SETUP_VPN:
        	if (resultCode == Activity.RESULT_OK) {
        		mVpnName = data.getStringExtra(SetupVpnActivity.EXTRA_VPN_NAME);
        		mVpnCity = data.getStringExtra(SetupVpnActivity.EXTRA_VPN_CITY);
        		mVpnServer = data.getStringExtra(SetupVpnActivity.EXTRA_VPN_SERVER);
        		mVpnUsername = data.getStringExtra(SetupVpnActivity.EXTRA_VPN_USERNAME);
        		mVpnPassword = data.getStringExtra(SetupVpnActivity.EXTRA_VPN_PASSWORD);
        		mVpnDns = data.getStringExtra(SetupVpnActivity.EXTRA_VPN_DNS);
        		mVpnPpe = data.getBooleanExtra(SetupVpnActivity.EXTRA_VPN_PPE, true);
        		setupVpn(true);
        	}
        	break;
        }
    }  
	
	private void initCheckbox(){
		cbEnableMifi.setChecked(Config.getBoolean("enable_mifi", false));
        cbEnable4G.setChecked(Config.getBoolean("enable_4g", false));
        cbEnableVPN.setChecked(Config.getBoolean("enable_vpn", false));
        cbNoSim.setChecked(Config.getBoolean("no_sim", false));
        cbEnableTest.setChecked(Config.getBoolean("enable_test", true));
        cbAutoStart.setChecked(Config.getBoolean("auto_start", false));
        cbOcrEnglish.setChecked(Config.getBoolean("ocr_english", true));
        cbOcrChinese.setChecked(Config.getBoolean("ocr_chinese", true));

        cbTestVPN.setChecked(Config.getBoolean("test_vpn", false));
        cbSpecifyVPN.setChecked(Config.getBoolean("specify_vpn", false));
        //cbOverseas.setChecked(Config.getBoolean("overseas", false));
        cbReportIP.setChecked(Config.getBoolean("report_ip", true));
        
        cbEnableMifi.setOnCheckedChangeListener(this);
        cbEnable4G.setOnCheckedChangeListener(this);
        cbEnableVPN.setOnCheckedChangeListener(this);
        cbNoSim.setOnCheckedChangeListener(this);
        cbEnableTest.setOnCheckedChangeListener(this);
        cbAutoStart.setOnCheckedChangeListener(this);
        cbTestVPN.setOnCheckedChangeListener(this);
        cbSpecifyVPN.setOnCheckedChangeListener(this);
        //cbOverseas.setOnCheckedChangeListener(this);
        cbOcrEnglish.setOnCheckedChangeListener(this);
        cbOcrChinese.setOnCheckedChangeListener(this);
        cbReportIP.setOnCheckedChangeListener(this);
	}
		
	private void showVersion(String serverOrClient) {
        // 在Activity中可以直接调用getPackageManager()，获取PackageManager实例。
        PackageManager packageManager = getPackageManager();
        // 在Activity中可以直接调用getPackageName()，获取安装包全名。
        String packageName = getPackageName();
        // flags提供了10种选项，及其组合，如果只是获取版本号，flags=0即可
        int flags = 0;
        PackageInfo packageInfo = null;
        try {
            // 通过packageInfo即可获取AndroidManifest.xml中的信息。
            packageInfo = packageManager.getPackageInfo(packageName, flags);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (packageInfo != null) {
            // 这里就拿到版本信息了。
            int versionCode = packageInfo.versionCode;
            String versionName = packageInfo.versionName;
            setTitle(getString(R.string.app_name)+" ["+versionName+" - "+versionCode+"] "+ serverOrClient);
        }
    }
	
	private void showMessageLog(String data) {
		showMessageLog(null, data);
	}

	private void showMessageLog(Date d, String data) {
        if (data == null) return;
    	if(mLogList.size()>200){
    		for(int i=0; i<50; i++){
    			mLogList.remove(0);
    		}
    	}
    	if(d == null){
    		d = new Date();
    	}
    	String formattedDate = logDF.format(d);
    	mLogList.add("<" + formattedDate + "> "+data);
    	mListAdapter.notifyDataSetChanged();   
    	mListView.setSelection(mListAdapter.getCount() - 1);
    }

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver () {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if(action == null) return;
			//Log.e(TAG, "mBroadcastReceiver: onReceive "+intent.getAction());
			if (intent.getAction().equals(MainService.ACTION_UI_MSG)) {
				int what = intent.getIntExtra("what", -1);
				String msg = intent.getStringExtra("msg");
				mHandler.obtainMessage(what, msg).sendToTarget();
			}
		}
		
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(bnStartTask == v){
			Log.e(TAG, "onClick bnStartTask");
			String workDir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/rowetalk/";
			File d = new File(workDir);
			if(!d.exists()){
				d.mkdirs();
			}
			d = new File(workDir);
			if(!d.exists()|| !d.isDirectory()){
				Toast.makeText(this, "无法创建工作目录:"+workDir, Toast.LENGTH_SHORT).show();
				return;
			}
	        if(cbEnableTest.isChecked()){
	        	tvServerAddr.setText(Constant.getSERVER_TEST());
	        }else {
	        	tvServerAddr.setText(Constant.getSERVER_RELEASE());
	        }
			
	        if(mWifiAddr == null) {
				Toast.makeText(this, "Wifi设备未选定!", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(!NetworkUtil.isWifiConnected(this)){
				Toast.makeText(this, "Wifi未连接!", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(!verifyServerAddr()){
				Toast.makeText(this, "服务器地址不合法!", Toast.LENGTH_SHORT).show();
				return;
			}
			String url = tvServerAddr.getText().toString();
			if(!url.equals(Constant.getSERVER_HOST())) {
				Config.putString("server_host", url);
				Constant.setSERVER_HOST(url);
			}
			
			if(mPhoneSeq <= 1000) {
				Toast.makeText(this, "手机编号未正确设置!", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(mNoSimVdevieNum < 100){
				Toast.makeText(this, "无卡虚拟设备数目设置不正确!", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(!cbNoSim.isChecked()) {
				if(cbEnable4G.isChecked() ){
					Toast.makeText(this, "无SIM卡时无法激活4G!", Toast.LENGTH_SHORT).show();
					return;
				}
				if(mPoolSeq <= 1000 || StringUtil.isEmpty(mSlotSeqs)){
					Toast.makeText(this, "卡槽参数未正确设置!", Toast.LENGTH_SHORT).show();
					return;
				}
				if(mBtAddr == null){
					Toast.makeText(this, "蓝牙设备未选定!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				//bluetooth
				Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		        // If there are paired devices, add each one to the ArrayAdapter
				mBluetoothDevice = null;
		        if (pairedDevices.size() > 0) {
		            for (BluetoothDevice bd : pairedDevices) {
		                if(bd.getAddress().equals(mBtAddr)){
		                	mBluetoothDevice = bd;
		                	break;
		                }
		            }
		        }
		        if(mBluetoothDevice == null){
		        	Toast.makeText(this, "BT未正确设置!", Toast.LENGTH_SHORT).show();
					return;
		        }

				mSimStart = Config.getInt("sim_start", mSimStart);
				tvSimStart.setText(Integer.toString(mSimStart));
		     
			}
			int checked = 0;
			if(cbEnableMifi.isChecked() ){ checked++; }
			if(cbEnable4G.isChecked() ){ checked++; }
			if(cbEnableVPN.isChecked() ){ checked++; }
			if(checked>=2){
				Toast.makeText(this, "启用Mifi,启用4G,启用VPN 只能三选一!", Toast.LENGTH_SHORT).show();
				return;
			}

			if(cbEnableMifi.isChecked() ){
				
				if(mMifiAddr == null) {
					Toast.makeText(this, "Mifi设备未选定!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				
				if(mWifiAddr.equals(mMifiAddr)){
					Toast.makeText(this, " WiFi 和 MiFi 地址相同!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(!mWifiManager.getConnectionInfo().getRawBSSID().equals(mWifiAddr)) {
					if(!NetworkUtil.wifiConnectTo(this, mWifiName, mWifiAddr, null)) {
						Toast.makeText(this, "连接到 WiFi 失败！", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				
			}

	        if(cbSpecifyVPN.isChecked() && StringUtil.isEmptyOrNull(mVpnServer)){
	        	Toast.makeText(this, "指定VPN未正确设置!", Toast.LENGTH_SHORT).show();
				return;
	        }
			
			startTask();
			
		}else if(bnStop == v){
			Log.e(TAG, "onClick bnStop");
			Intent intent = new Intent(MainService.ACTION_ROWETALK_STOP_SERVICE);
			sendBroadcast(intent);
		}else if(v == bnSelectBt){
			Log.e(TAG, "onClick bnSelectBt");
			Intent serverIntent = new Intent(this, BtDeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_BROWSE_BT_DEVICES);
		}else if(v== bnClearLog){
			Log.e(TAG, "onClick bnClearLog");
			mLogList.clear();
			mListAdapter.notifyDataSetChanged();
		}else if(v == bnExecutScript){
			Log.e(TAG, "onClick bnExecutScript");
			LinearLayout linearLayoutMain = new LinearLayout(this);//自定义一个布局文件  
			linearLayoutMain.setLayoutParams(new LayoutParams(  
			        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));  
			ListView listView = new ListView(this);//this为获取当前的上下文  
			listView.setFadingEdgeLength(0);  
			  
			List<String> nameList = new ArrayList<String>();//建立一个数组存储listview上显示的数据  
			nameList.add(getResources().getString(R.string.random_emulation));
			nameList.add(getResources().getString(R.string.turnon_vpn));
			nameList.add(getResources().getString(R.string.turnoff_vpn));
			nameList.add(getResources().getString(R.string.read_specific_dir, "APP"));
			
			  
			ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, nameList);  
			listView.setAdapter(adapter);  
			  
			linearLayoutMain.addView(listView);//往这个布局中加入listview  
			  
			final AlertDialog dialog = new AlertDialog.Builder(this)  
			        .setTitle(R.string.select_title).setView(linearLayoutMain)//在这里把写好的这个listview的布局加载dialog中  
			        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {  
			  
			            @Override  
			            public void onClick(DialogInterface dialog, int which) {  
			                // TODO Auto-generated method stub  
			                dialog.cancel();  
			            }  
			        }).create();  
			dialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击  
			dialog.show();  
			listView.setOnItemClickListener(new OnItemClickListener() {//响应listview中的item的点击事件  
			  
			    @Override  
			    public void onItemClick(AdapterView<?> pParent, View pView,
	                    int position, long pId) {  
		    		dialog.cancel(); 
			    	if(position == 0){
			    		new Thread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								final String word = ServerUtil.RandomWord();
								final Platform platform = ServerUtil.RandomPlatform();
								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										if(word == null){
							    			Toast.makeText(MainActivity.this, "RandomWord failed.", Toast.LENGTH_SHORT).show();
							    			return;
							    		}
										if(platform == null){
							    			Toast.makeText(MainActivity.this, "RandomPlatform failed.", Toast.LENGTH_SHORT).show();
							    			return;
							    		}
										Random r = new Random();
										r.setSeed(System.currentTimeMillis());
							    		final EnvProfile p = new EnvProfile();
							    		p.platform = platform;
							    		
							    		p.device = new Device();
							    		p.device.setAid(Generator.RandomAID(r,p.platform));
							    		p.device.setBmac(Generator.RandomMac(r,p.platform));
							    		p.device.setWmac(Generator.RandomMac(r,p.platform));
							    		p.device.setEssid(Generator.RandomName(r,word));
							    		p.device.setBssid(Generator.RandomMac(r,p.platform));
							    		p.device.setImei(Generator.RandomImei(r,p.platform));
							    		p.device.setSerial(Generator.RandomSerial(r,p.platform));
							    		p.device.setIpv4(Generator.RandomIpV4(r));
							    		p.device.setIpv6(Generator.RandomIpV6(r));
							    		
							    		final StringBuilder sb = new StringBuilder();
							    		sb.append("\nAID: "+p.device.getAid()).append("\n");
							    		sb.append("BMAC: "+p.device.getBmac()).append("\n");
							    		sb.append("WMAC: "+p.device.getWmac()).append("\n");
							    		sb.append("SSID: "+p.device.getEssid()).append("\n");
							    		sb.append("BSSID: "+p.device.getBssid()).append("\n");
							    		sb.append("IMEI: "+p.device.getImei()).append("\n");
							    		sb.append("SERIAL: "+p.device.getSerial()).append("\n");
							    		sb.append("IpV4: "+p.device.getIpv4()).append("\n");
							    		sb.append("IpV6: "+p.device.getIpv6()).append("\n");


							    		sb.append("Brand: "+platform.getBdBrand()).append("\n");
							    		sb.append("Product: "+platform.getBdProduct()).append("\n");
							    		sb.append("Model: "+platform.getBdModel()).append("\n");
							    		sb.append("Manufacture: "+platform.getBdManufacture()).append("\n");
							    		sb.append("Hardware: "+platform.getBdHardware()).append("\n");
							    		

							    		final AlertDialog dialog2 = new AlertDialog.Builder(MainActivity.this)  
										        .setTitle(R.string.important_hint).setMessage(
										        		getString(R.string.random_emulation_hint)+sb.toString())
										        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {  
										  
										            @Override  
										            public void onClick(DialogInterface dialog, int which) {  
										                // TODO Auto-generated method stub  
										                dialog.cancel(); 
										            }  
										        })
										        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
													
													@Override
													public void onClick(DialogInterface dialog, int which) {
														dialog.cancel();
														// TODO Auto-generated method stub
														if(BaseThread.updateEmuData(true, p, sleepObject)){
										                	Toast.makeText(MainActivity.this, "updateEmuData sucess.", Toast.LENGTH_SHORT).show();
										                }else{
										                	Toast.makeText(MainActivity.this, "updateEmuData failed.", Toast.LENGTH_SHORT).show();
										                }
													}
												}).create();  
										dialog2.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击  
										dialog2.show();
									}
								});
							}
						}).start();
			    		
			    	}else if(position == 1){
			    		//String ipv6 = NetworkUtil.getIpV6();
			    		//Logger.e(TAG, "getIpV6="+ipv6);
			    		//Toast.makeText(MainActivity.this, "getIpV6="+ipv6, Toast.LENGTH_SHORT).show();
			    		if(mVpnServer==null || mVpnUsername == null || mVpnPassword == null){
			    			Toast.makeText(MainActivity.this, "VPN 未设置！", Toast.LENGTH_SHORT).show();
			    			return;
			    		}
			    		final VpnProfile v = new VpnProfile();
						v.account = new VpnAccount();
						v.account.setUsername(mVpnUsername);
						v.account.setPassword(mVpnPassword);
						v.account.setCity(mVpnCity);
						v.account.setServer(mVpnServer);
						v.account.setDescription(mVpnName+" "+mVpnServer);
						v.account.setDns(mVpnDns);
						v.account.setPpe((byte)(mVpnPpe?1:0));
						v.specified = true;
						final SleepObject sleepObj = sleepObject;
			    		new Thread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								final boolean b = PlatformUtil.switchVPN(getApplicationContext(), true, v, sleepObj);
								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										Toast.makeText(MainActivity.this, b?"VPN打开成功！":"VPN打开失败！", Toast.LENGTH_SHORT).show();
									}
								});
							}
						}).start();
			    	}else if(position == 2){
			    		final SleepObject sleepObj = sleepObject;
			    		new Thread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								final boolean b = PlatformUtil.switchVPN(getApplicationContext(), false, null, sleepObj);
								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										Toast.makeText(MainActivity.this, b?"VPN关闭成功！":"VPN关闭失败！", Toast.LENGTH_SHORT).show();
									}
								});
							}
						}).start();
			    	}else if(position == 3){ //read_specific_dir
			    		final EditText inputBox = new EditText(MainActivity.this);
						final AlertDialog dialog2 = new AlertDialog.Builder(MainActivity.this)  
						        .setTitle(R.string.input_package_name).setView(inputBox)//在这里把写好的这个listview的布局加载dialog中  
						        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {  
						  
						            @Override  
						            public void onClick(DialogInterface dialog, int which) {  
						                // TODO Auto-generated method stub  
						                dialog.cancel();  
						            }  
						        })
						        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.cancel();
										// TODO Auto-generated method stub
										String inputName = inputBox.getText().toString();
										if(StringUtil.isEmpty(inputName)){
											Toast.makeText(MainActivity.this, "输入为空！", Toast.LENGTH_SHORT).show();
											return;
										}
										String s = getString(R.string.read_specific_dir, inputName);
										File dir = new File("/data/data/"+inputName);
							    		StringBuilder sb = new StringBuilder();
							    		String files[] = dir.list();  
								        if(files != null) {
									        for (String file : files) {  
									            sb.append(file).append("\n");
									        }  
								        }
								        s+=":\n"+sb.toString();
								        s+="\nShell List:\n";
								        sb = new StringBuilder();
								        if(RootShellCmd.getInstance().exec("ls /data/data/"+inputName, sb)){
								        	s+=sb.toString();
								        }else{
								        	s+="ls /data/data/"+inputName+ " failed.";
								        }
								        
							    		
							    		final AlertDialog dialog3 = new AlertDialog.Builder(MainActivity.this)  
										        .setTitle(R.string.important_hint).setMessage(s)
										        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {  
										  
										            @Override  
										            public void onClick(DialogInterface dialog, int which) {  
										                // TODO Auto-generated method stub  
										                dialog.cancel(); 
										            }  
										        }).create();  
										dialog3.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击  
										dialog3.show();
									}
								}).create();  
						dialog2.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击  
						dialog2.show();
			    		
			    	}
			    }  
			}); 
			
		}else if(v == bnSwitchCard) {
			if(bnStop.isEnabled()){
				Toast.makeText(this, "请先停止任务", Toast.LENGTH_SHORT).show();
				return;
			}
			Log.e(TAG, "onClick bnSwitchCard");
			if(mVerifyCardThread != null){
				mVerifyCardThread.cancel(false);
				mVerifyCardThread = null;
				bnStartTask.setEnabled(true);
				bnSwitchCard.setText(R.string.start_verify_cards);
			}else{
				LinearLayout linearLayoutMain = new LinearLayout(this);//自定义一个布局文件  
				linearLayoutMain.setLayoutParams(new LayoutParams(  
				        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));  
				ListView listView = new ListView(this);//this为获取当前的上下文  
				listView.setFadingEdgeLength(0);  
				  
				List<String> nameList = new ArrayList<String>();//建立一个数组存储listview上显示的数据  
				nameList.add(getResources().getString(R.string.start_verify_cards));
				nameList.add(getResources().getString(R.string.start_mark_cards));
				nameList.add(getResources().getString(R.string.switch_specifiy_card1));
				nameList.add(getResources().getString(R.string.switch_specifiy_card2));
				
				  
				ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, nameList);  
				listView.setAdapter(adapter);  
				  
				linearLayoutMain.addView(listView);//往这个布局中加入listview  
				  
				final AlertDialog dialog = new AlertDialog.Builder(this)  
				        .setTitle(R.string.select_title).setView(linearLayoutMain)//在这里把写好的这个listview的布局加载dialog中  
				        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {  
				  
				            @Override  
				            public void onClick(DialogInterface dialog, int which) {  
				                // TODO Auto-generated method stub  
				                dialog.cancel();  
				            }  
				        }).create();  
				dialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击  
				dialog.show();  
				listView.setOnItemClickListener(new OnItemClickListener() {//响应listview中的item的点击事件  
				  
				    @Override  
				    public void onItemClick(AdapterView<?> pParent, View pView,
		                    int position, long pId) {  
			    		dialog.cancel(); 
			    		if(position == 2){ 
				    		View v = MainActivity.this.getLayoutInflater().inflate(R.layout.input_slot_sim_seq, null);
							final EditText etInputSlotSeq = (EditText)v.findViewById(R.id.etInputSlotSeq);
							final EditText etInputSimSeq = (EditText)v.findViewById(R.id.etInputSimSeq);
							final AlertDialog dialog2 = new AlertDialog.Builder(MainActivity.this)  
							        .setTitle(R.string.select_title).setView(v)//在这里把写好的这个listview的布局加载dialog中  
							        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {  
							  
							            @Override  
							            public void onClick(DialogInterface dialog, int which) {  
							                // TODO Auto-generated method stub  
							                dialog.cancel();  
							            }  
							        })
							        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
											// TODO Auto-generated method stub
											String s1 = etInputSlotSeq.getText().toString();
											String s2 = etInputSimSeq.getText().toString();
											int slot_seq = -1;
											int sim_seq = -1;
											if(StringUtil.isNotEmpty(s1)) {
												slot_seq = Integer.parseInt(s1);
											}
											if(StringUtil.isNotEmpty(s2)) {
												sim_seq = Integer.parseInt(s2);
											}
											if(slot_seq <=0 || sim_seq <=0){
												Toast.makeText(MainActivity.this, R.string.invalid_params, Toast.LENGTH_SHORT).show();
												return;
											}
											Map<String, Object> map = new HashMap<String, Object>();
											map.put("slot_seq", slot_seq);
											map.put("sim_seq", sim_seq);
											map.put("specify_card", true);
											startVerifyCardThread(true, map);
										}
									}).create();  
							dialog2.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击  
							dialog2.show();  
				    	}else if(position == 3){
				    		View v = MainActivity.this.getLayoutInflater().inflate(R.layout.input_sim_index, null);
							final EditText etInputSimIndex = (EditText)v.findViewById(R.id.etInputSimIndex);
							final AlertDialog dialog2 = new AlertDialog.Builder(MainActivity.this)  
							        .setTitle(R.string.select_title).setView(v)//在这里把写好的这个listview的布局加载dialog中  
							        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {  
							  
							            @Override  
							            public void onClick(DialogInterface dialog, int which) {  
							                // TODO Auto-generated method stub  
							                dialog.cancel();  
							            }  
							        })
							        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
											// TODO Auto-generated method stub
											String s1 = etInputSimIndex.getText().toString();
											int sim_index = -1;
											if(StringUtil.isNotEmpty(s1)) {
												sim_index = Integer.parseInt(s1);
											}
											if(sim_index <0 ){
												Toast.makeText(MainActivity.this, R.string.invalid_params, Toast.LENGTH_SHORT).show();
												return;
											}
											Map<String, Object> map = new HashMap<String, Object>();
											map.put("sim_index", sim_index);
											map.put("specify_card", true);
											startVerifyCardThread(true, map);
										}
									}).create();  
							dialog2.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击  
							dialog2.show();
				    	}else if(position == 1){
				    		final AlertDialog dialog2 = new AlertDialog.Builder(MainActivity.this)  
							        .setTitle(R.string.important_hint).setMessage(R.string.mark_card_notice)
							        .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {  
							  
							            @Override  
							            public void onClick(DialogInterface dialog, int which) {  
							                // TODO Auto-generated method stub  
							                dialog.cancel();  
							            }  
							        })
							        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
											// TODO Auto-generated method stub
											startVerifyCardThread(false, null);
										}
									}).create();  
							dialog2.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击  
							dialog2.show();
				    	}else {
					        // TODO Auto-generated method stub  
					        startVerifyCardThread(true, null);
				    	}
				    }  
				});  
				
			}
		}else if(v == bnSysProps){
			Log.e(TAG, "onClick bnSysProps");
			
			showMessageLog("ID: "+Build.ID);
			showMessageLog("DISPLAY: "+Build.DISPLAY);
			showMessageLog("Product: "+Build.PRODUCT);
			showMessageLog("Device: "+Build.DEVICE);
			showMessageLog("Board: "+Build.BOARD);
			showMessageLog("MANUFACTURER: "+Build.MANUFACTURER);
			showMessageLog("Brand: "+Build.BRAND);
			showMessageLog("MODEL: "+Build.MODEL);
			showMessageLog("BOOTLOADER: "+Build.BOOTLOADER);
			showMessageLog("HARDWARE: "+Build.HARDWARE);
			showMessageLog("SERIAL: "+Build.SERIAL);
			showMessageLog("VERSION.INCREMENTAL: "+Build.VERSION.INCREMENTAL);
			showMessageLog("VERSION.RELEASE: "+Build.VERSION.RELEASE);
			//showMessageLog("VERSION.BASE_OS: "+Build.VERSION.BASE_OS);
			//showMessageLog("VERSION.SECURITY_PATCH: "+Build.VERSION.SECURITY_PATCH);
			showMessageLog("VERSION.SDK_INT: "+Build.VERSION.SDK_INT);
			//showMessageLog("VERSION.PREVIEW_SDK_INT: "+Build.VERSION.PREVIEW_SDK_INT);
			showMessageLog("VERSION.CODENAME: "+Build.VERSION.CODENAME);
			showMessageLog("TYPE: "+Build.TYPE);
			showMessageLog("TAGS: "+Build.TAGS);
			showMessageLog("FINGERPRINT: "+Build.FINGERPRINT);
			
			showMessageLog("TIME: "+Build.TIME);
			showMessageLog("USER: "+Build.USER);
			showMessageLog("HOST: "+Build.HOST);
			
			showMessageLog("RadioVersion: "+Build.getRadioVersion());
			
			showMessageLog("ANDROID_ID: "+Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID));
			
			showMessageLog("MemTotal: "+SystemUtil.getmem_TOLAL());
			showMessageLog("");
			TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			if(tm != null) {
				showMessageLog("IMEI: "+tm.getRawDeviceId()+" / "+tm.getDeviceId());
				showMessageLog("IMSI: "+tm.getRawSubscriberId()+" / "+tm.getSubscriberId());
				showMessageLog("Operator: "+tm.getRawNetworkOperator()+" / "+tm.getNetworkOperator());
			}
			showMessageLog("BT Mac: "+BluetoothAdapter.getDefaultAdapter().getRawAddress()+" / "+BluetoothAdapter.getDefaultAdapter().getAddress());
			WifiManager mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
	        WifiInfo info = (null == mWifiManager ? null : mWifiManager.getConnectionInfo());
	        if(info != null) {
				showMessageLog("WiFi Mac: "+info.getRawMacAddress()+" / "+info.getMacAddress());
				showMessageLog("WiFi SSID: "+info.getRawSSID()+" / "+info.getSSID());
				showMessageLog("WiFi BSSID: "+info.getRawBSSID()+" / "+info.getBSSID());
				showMessageLog("WiFi IpV4: "+NetworkUtil.int2ip(info.getRawIpAddress())+" / "+NetworkUtil.int2ip(info.getIpAddress()));
				showMessageLog("WiFi IpV6: "+NetworkUtil.getRawIpV6()+" / "+NetworkUtil.getIpV6());
				List<WifiConfiguration> wcList = mWifiManager.getConfiguredNetworks();
				showMessageLog("");
				for(WifiConfiguration wc: wcList){
					showMessageLog("Scanned WiFi: "+wc.SSID+" / "+wc.BSSID);
				}
			}

			// check version
			sendBroadcast(new Intent(MainService.ACTION_ROWETALK_UPDATE_CHECK));
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					final boolean b = ServerUtil.testServerConnect();
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(MainActivity.this, "服务器连接测试："+(b?"成功":"失败"), Toast.LENGTH_SHORT).show();
						}
					});
					SystemUpdate su = SystemUpdateUtil.checkUpdate(mPhoneSeq, null);
					if(su != null){
						final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tmp/update.zip";
						final boolean force_update = su.getUpdateType()==1;
						boolean success = SystemUpdateUtil.downloadUpdate(su, filePath);
						if(success){
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									if(force_update) {
										Intent i = new Intent("ty.intent.action.system_update");
										i.putExtra("update_file", filePath);
										sendBroadcast(i);
										Toast.makeText(MainActivity.this, "已发送强制更新系统广播.", Toast.LENGTH_SHORT).show();
									}else{
										Toast.makeText(MainActivity.this, "系统固件已经下载，请手动升级.", Toast.LENGTH_SHORT).show();
									}
								}
							});
						}
					}
					
				}
			}).start();
		} else if (v == bnSelectMifi){
			Log.e(TAG, "onClick bnSelectMifi");
			Intent serverIntent = new Intent(this, MifiDeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_BROWSE_MIFI_DEVICES);
		} else if (v == bnSelectWifi){
			Log.e(TAG, "onClick bnSelectWifi");
			Intent serverIntent = new Intent(this, MifiDeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_BROWSE_WIFI_DEVICES);
		} else if (v == bnSetupParams) {
			Log.e(TAG, "onClick bnSetupParams");
			mSimStart = Config.getInt("sim_start", mSimStart);
			Intent serverIntent = new Intent(this, SetupParamsActivity.class);
			serverIntent.putExtra(SetupParamsActivity.EXTRA_PHONE_SEQ, mPhoneSeq);
			serverIntent.putExtra(SetupParamsActivity.EXTRA_POOL_SEQ, mPoolSeq);
			serverIntent.putExtra(SetupParamsActivity.EXTRA_SLOT_SEQS, mSlotSeqs);
			serverIntent.putExtra(SetupParamsActivity.EXTRA_NO_SIM, cbNoSim.isChecked());
			serverIntent.putExtra(SetupParamsActivity.EXTRA_NOSIM_VDEVICE_NUM, mNoSimVdevieNum);
			serverIntent.putExtra(SetupParamsActivity.EXTRA_NOSIM_START, mNoSimStart);
			serverIntent.putExtra(SetupParamsActivity.EXTRA_SIM_START, mSimStart);
			serverIntent.putExtra(SetupParamsActivity.EXTRA_IME_WAIT_TIME, mImeWaitTime);
			
            startActivityForResult(serverIntent, REQUEST_SETUP_PARAMS);
		}else if (v == bnSelectVpn) {
			Log.e(TAG, "onClick bnSelectVpn");
			Intent serverIntent = new Intent(this, SetupVpnActivity.class);
			serverIntent.putExtra(SetupVpnActivity.EXTRA_VPN_NAME, mVpnName);
			serverIntent.putExtra(SetupVpnActivity.EXTRA_VPN_SERVER, mVpnServer);
			serverIntent.putExtra(SetupVpnActivity.EXTRA_VPN_USERNAME, mVpnUsername);
			serverIntent.putExtra(SetupVpnActivity.EXTRA_VPN_PASSWORD, mVpnPassword);
			serverIntent.putExtra(SetupVpnActivity.EXTRA_VPN_DNS, mVpnDns);
			serverIntent.putExtra(SetupVpnActivity.EXTRA_VPN_PPE, mVpnPpe);
            startActivityForResult(serverIntent, REQUEST_SETUP_VPN);
		}
	}
	
	
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.e(TAG, "onConfigurationChanged");
	}
	
	private void enableWhenTaskStopped(){
		bnStartTask.setEnabled(true);
		bnExecutScript.setEnabled(true);
		bnSwitchCard.setEnabled(true);
		bnSelectBt.setEnabled(true);
		bnSelectMifi.setEnabled(true);
		bnSelectWifi.setEnabled(true);
		bnSetupParams.setEnabled(true);
		cbLogFile.setEnabled(true);
		cbEnable4G.setEnabled(true);
		cbEnableMifi.setEnabled(true);
		cbEnableVPN.setEnabled(true);
		cbTestVPN.setEnabled(true);
		bnSelectVpn.setEnabled(true);
		cbSpecifyVPN.setEnabled(true);
		cbEnableTest.setEnabled(true);
		cbNoSim.setEnabled(true);
		bnStop.setEnabled(false);
	}
	private void disableWhenTaskRunning(){
		bnStartTask.setEnabled(false);
		bnExecutScript.setEnabled(false);
		bnSwitchCard.setEnabled(false);
		bnSelectBt.setEnabled(false);
		bnSelectMifi.setEnabled(false);
		bnSelectWifi.setEnabled(false);
		bnSetupParams.setEnabled(false);
		cbLogFile.setEnabled(false);
		bnSelectVpn.setEnabled(false);
		cbSpecifyVPN.setEnabled(false);
		cbEnable4G.setEnabled(false);
		cbEnableMifi.setEnabled(false);
		cbEnableVPN.setEnabled(false);
		cbEnableTest.setEnabled(false);
		cbTestVPN.setEnabled(false);
		cbNoSim.setEnabled(false);
		bnStop.setEnabled(true);
		
		tvFalseSlots.setText("");
		tvFalseTasks.setText("");
		tvCurrentSim.setText("");
		
		//mLogField.setText("");
		mLogList.clear();
		mListAdapter.notifyDataSetChanged();
	}
	
	private void startTask(){
		if(Config.getBoolean("server_running", false)) {
			disableWhenTaskRunning();
			Logger.e(TAG, "startTask failed: task already running");
			return;
		}
		String msg = null;
		if(!cbNoSim.isChecked()){
			msg = String.format("手机编号：%d\n卡池编号：%d\n卡池范围（%s）\n无卡虚拟设备数目: %d", mPhoneSeq, mPoolSeq, mSlotSeqs, mNoSimVdevieNum);
		}else{
			msg = String.format("手机编号：%d\n无SIM卡池\n无卡虚拟设备数目: %d", mPhoneSeq, mNoSimVdevieNum);
		}
		new AlertDialog.Builder(this)
		.setTitle("请确认参数配置")
		.setMessage(msg)
		.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				disableWhenTaskRunning();
				sendBroadcast(new Intent(MainService.ACTION_ROWETALK_START_SERVICE));
			}
		})
	  	.setNegativeButton("取消", null)
	  	.show();
		
	}
	private void stopTask(){
		sendBroadcast(new Intent(MainService.ACTION_ROWETALK_STOP_SERVICE));
	}

	private void setupParams(boolean save) {	
		if(cbNoSim.isChecked()){
			tvParams.setText(String.format("Phone(%d), vNum(%d), Start(%d), No sim support, ImeWaitTime(%d)", mPhoneSeq, mNoSimVdevieNum, mNoSimStart, mImeWaitTime));
		}else {
			tvParams.setText(String.format("Phone(%d), vNum(%d), Pool(%d), Slots(%s), ImeWaitTime(%d)", mPhoneSeq, mNoSimVdevieNum, mPoolSeq, mSlotSeqs, mImeWaitTime));
			tvCurrentSim.setText(Integer.toString(mSimStart));
			tvFalseSlots.setText("");
			tvFalseTasks.setText("");
			ArrayList<Integer> list = new ArrayList<Integer>();
			if(BaseThread.initSlotSeqList(list, mSlotSeqs)){
				int s_start = -1;
				int s_end = -1;
				for(int i=0;i<list.size();i++){
					showMessageLog(String.format("Slot %d = %d", i, list.get(i)));
					int num = list.get(i);
					if(num != 0){
						if(s_start < 0){
							s_start = i*16;
						}
						s_end = (i+1) * 16;
					}
				}
				if(s_start < 0 || s_end <1 || s_start > s_end){
					showMessageLog("initSlotSeqList: bad sim range: "+String.format("%d,%d", s_start, s_end));						
					tvSimInfo.setText("bad data");
					tvSimStart.setText("");
				}else{
					if(mSimStart < s_start){
						mSimStart = s_start;
					}
					if(mSimStart>= s_end){
						mSimStart = s_end-1;
					}
					showMessageLog("initSlotSeqList: "+String.format("sim range (%d,%d) start %d", s_start, s_end, mSimStart));
					tvSimInfo.setText(String.format("%d-%d  ", s_start, s_end));
					tvSimStart.setText(Integer.toString(mSimStart));
					tvCurrentSim.setText(Integer.toString(mSimStart));
				}
				
				
			}else{
				tvSimInfo.setText("");
				tvSimStart.setText("");
			}
		}
		if(save) {
			Config.putInt("phone_seq", mPhoneSeq);
			if(!cbNoSim.isChecked()){
				Config.putInt("pool_seq", mPoolSeq);
				Config.putString("slot_seqs", mSlotSeqs);
				Config.putInt("sim_start", mSimStart);
			}
				
			Config.putInt("nosim_vdevice_num", mNoSimVdevieNum);
			Config.putInt("nosim_start", mNoSimStart);
			Config.putInt("ime_wait_time", mImeWaitTime);
			LuaEnv.getInstance().setImeWaitTime(mImeWaitTime);
		}
	}
	private void setupVpn(boolean save) {
		tvVpnDev.setText(String.format("Name: %s, City: %s, Server: %s, User: %s, PPE: %s", mVpnName, mVpnCity, mVpnServer, mVpnUsername, mVpnPpe?"true":"false"));
		if(save) {
			Config.putString("vpn_name",  mVpnName);
			Config.putString("vpn_city",  mVpnCity);
			Config.putString("vpn_server",  mVpnServer);
			Config.putString("vpn_username",  mVpnUsername);
			Config.putString("vpn_password",  mVpnPassword);
			Config.putString("vpn_dns",  mVpnDns);
			Config.putBoolean("vpn_ppe",  mVpnPpe);
		}
	}
	private boolean verifyServerAddr() {
		String ip = tvServerAddr.getText().toString();
		if(StringUtil.isEmptyOrNull(ip) )
			return false; 
		if(!mUrlPattern.matcher(ip).find())
			return false;
		
		return true;
	}
	
	private void saveLogContent(List<String> logs){
		if(logs != null && logs.size() > 0){
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				String logdir = Environment.getExternalStorageDirectory().getPath()+"/logs/";
				String logfile = logdir + "/work_log.txt";
				File d = new File(logdir);
				if(!d.exists()) {
	            	d.mkdirs();
	            }
				File f = new File(logfile);
				if(!f.exists() || f.length()>(1*1024*1024)){
					try {
						f.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					FileWriter filerWriter = new FileWriter(f, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
		            for(String s: logs){
		            	filerWriter.write(s);;
		            }
		            filerWriter.flush();
		            filerWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		// TODO Auto-generated method stub
		if(v== cbEnableMifi){
			Config.putBoolean("enable_mifi", isChecked);
		}else if(v== cbEnable4G){
			Config.putBoolean("enable_4g", isChecked);
		}else if(v== cbEnableVPN){
			Config.putBoolean("enable_vpn", isChecked);
		}else if(v== cbNoSim){
			Config.putBoolean("no_sim", isChecked);
		}else if(v== cbEnableTest){
			Config.putBoolean("enable_test", isChecked);
		}else if(v== cbAutoStart){
			Config.putBoolean("auto_start", isChecked);
		}else if(v== cbTestVPN){
			Config.putBoolean("test_vpn", isChecked);
		}else if(v== cbSpecifyVPN){
			Config.putBoolean("specify_vpn", isChecked);
		//}else if(v== cbOverseas){
		//	Config.putBoolean("overseas", isChecked);
		}else if(v== cbOcrEnglish){
			Config.putBoolean("ocr_english", isChecked);
		}else if(v== cbOcrChinese){
			Config.putBoolean("ocr_chinese", isChecked);
		}else if(v== cbReportIP){
			Config.putBoolean("report_ip", isChecked);
		}
	}
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//super.handleMessage(msg);
			long l1 = (msg.arg2 & 0x000000ffffffffL) << 32;  
		    long l2 = msg.arg1 & 0x00000000ffffffffL;  
		    long l = l1 | l2;  
		    Date d = null;
		    if(l>0){
		    	d = new Date(l); 
		    }
			
			switch(msg.what){
			case BaseThread.MSG_THREAD_STARTED:
				showMessageLog(d, (String)msg.obj);
				break;
			case BaseThread.MSG_THREAD_ENDED:
				showMessageLog(d, (String)msg.obj);
				enableWhenTaskStopped();
				showVersion("");
				saveLogContent(mLogList);
				if(mVerifyCardThread != null){
					mVerifyCardThread = null;
					bnStartTask.setEnabled(true);
					bnSwitchCard.setText(R.string.start_verify_cards);
				}
				break;
			case BaseThread.MSG_THREAD_INFO:
				showMessageLog(d, (String)msg.obj);
				break;
			case BaseThread.MSG_DIAL_NUMBER:
				//showMessageLog("info: dial "+edtPhoneNumber.getText().toString());
				//RootShellCmd.getInstance().dial(edtPhoneNumber.getText().toString());
				break;
			case BaseThread.MSG_FALSE_SLOTS:
				mFalseSlots = (String)msg.obj;
				tvFalseSlots.setText(mFalseSlots);
				break;
			case BaseThread.MSG_FALSE_TASKS:
				mFalseTasks = (String)msg.obj;
				tvFalseTasks.setText(mFalseTasks);
				break;
			case BaseThread.MSG_SUCCESS_TASKS:
				mSuccessTasks = (String)msg.obj;
				tvSuccessTasks.setText(mSuccessTasks);
				break;
			case BaseThread.MSG_CARD_IN_JOB:
				tvCurrentSim.setText("@ "+(String)msg.obj);
				break;
			case BaseThread.MSG_VPN_INFO:
				Log.e(TAG, "MSG_VPN_INFO: "+(String)msg.obj);
				tvVpnDev.setText((String)msg.obj);
				break;
			case MainService.MSG_UPDATE_THREAD_STATUS:
				showVersion((String)msg.obj);
				break;
			case MainService.MSG_SCRIPT_FINISHED:
				showMessageLog(d, (String)msg.obj);
				break;
			}
		}
		
	};
	
	private void startVerifyCardThread(boolean verify_card, Map<String, Object> extra_map){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("slot_seqs", mSlotSeqs);
		map.put("bt_addr", mBtAddr);
		map.put("phone_seq", mPhoneSeq);
		map.put("pool_seq", mPoolSeq);
		map.put("mark_cards", !verify_card);
		if(extra_map != null){
			map.putAll(extra_map);
		}
		
		mSimStart = Config.getInt("sim_start", mSimStart);
		tvSimStart.setText(Integer.toString(mSimStart));
		
		map.put("sim_start", mSimStart);
		
		mVerifyCardThread = new VerifyCardThread(MainActivity.this, mHandler, sleepObject, map);
		mVerifyCardThread.start();
		bnStartTask.setEnabled(false);
		if(verify_card){
			bnSwitchCard.setText(R.string.stop_verify_cards);
		}else{
			bnSwitchCard.setText(R.string.stop_mark_cards);
		}
	}
	
}
