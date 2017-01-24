/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.rowetalk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rowetalk.util.NetworkUtil;
import com.example.rowetalk.util.PlatformUtil;
import com.example.rowetalk.util.RootShellCmd;
import com.example.rowetalk.util.SleepObject;
import com.example.rowetalk.util.StringUtil;
import com.example.rowetalk.R;


/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class SetupVpnActivity extends Activity implements OnClickListener {

    /**
     * Tag for Log
     */
    private static final String TAG = "SetupVpnActivity";

    /**
     * Return Intent extra
     */
    public static String EXTRA_VPN_NAME = "vpn_name";
    public static String EXTRA_VPN_SERVER = "vpn_server";
    public static String EXTRA_VPN_USERNAME = "vpn_username";
    public static String EXTRA_VPN_PASSWORD = "vpn_password";
    public static String EXTRA_VPN_DNS = "vpn_dns";
    public static String EXTRA_VPN_PPE = "vpn_ppe";
    public static String EXTRA_VPN_CITY = "vpn_city";

    
    private EditText etVpnName;
    private EditText etVpnServer;
    private EditText etVpnUsername;
    private EditText etVpnPassword;
    private EditText etVpnDns;
    private EditText etVpnCity;
    private CheckBox cbVpnPpe;
    
    
    private Button bnCancel, bnOK;
    private SleepObject sleepObject = new SleepObject();
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_setup_vpn);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);
        etVpnName = (EditText)findViewById(R.id.etVpnName);
        etVpnCity = (EditText)findViewById(R.id.etVpnCity);
        etVpnServer = (EditText)findViewById(R.id.etVpnServer);
        etVpnUsername = (EditText)findViewById(R.id.etVpnUsername);
        etVpnPassword = (EditText)findViewById(R.id.etVpnPassword);
        etVpnDns = (EditText)findViewById(R.id.etVpnDns);
        cbVpnPpe = (CheckBox)findViewById(R.id.cbVpnPpe);

        bnCancel = (Button)findViewById(R.id.button_cancel);
        bnOK = (Button)findViewById(R.id.button_ok);
        
        bnCancel.setOnClickListener(this);
        bnOK.setOnClickListener(this);
       
        Intent intent = getIntent();
        etVpnName.setText(intent.getStringExtra(EXTRA_VPN_NAME));
        etVpnCity.setText(intent.getStringExtra(EXTRA_VPN_CITY));
        etVpnServer.setText(intent.getStringExtra(EXTRA_VPN_SERVER));
        etVpnUsername.setText(intent.getStringExtra(EXTRA_VPN_USERNAME));
        etVpnPassword.setText(intent.getStringExtra(EXTRA_VPN_PASSWORD));
        etVpnDns.setText(intent.getStringExtra(EXTRA_VPN_DNS));
        cbVpnPpe.setChecked(intent.getBooleanExtra(EXTRA_VPN_PPE, true));
        new Thread(new Runnable() {
			public void run() {
				RootShellCmd.getInstance().setImeLatin();
		        PlatformUtil.switchVPN(SetupVpnActivity.this, false, null, sleepObject);
			}
		}).start();
    }


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == bnCancel){
			finish();
		}else if(v==bnOK) {
			if(StringUtil.isEmpty(etVpnName.getText().toString()) ||
				StringUtil.isEmpty(etVpnCity.getText().toString()) ||
			    StringUtil.isEmpty(etVpnServer.getText().toString()) ||
				StringUtil.isEmpty(etVpnUsername.getText().toString()) ||
				StringUtil.isEmpty(etVpnPassword.getText().toString()) ||
				StringUtil.isEmpty(etVpnDns.getText().toString())){
				Toast.makeText(this, "未设置参数！", Toast.LENGTH_SHORT).show();
				return;
			}

			Intent intent = new Intent();
			intent.putExtra(EXTRA_VPN_NAME, etVpnName.getText().toString());
            intent.putExtra(EXTRA_VPN_CITY, etVpnCity.getText().toString());
            intent.putExtra(EXTRA_VPN_SERVER, etVpnServer.getText().toString());
            intent.putExtra(EXTRA_VPN_USERNAME, etVpnUsername.getText().toString());
            intent.putExtra(EXTRA_VPN_PASSWORD, etVpnPassword.getText().toString());
            intent.putExtra(EXTRA_VPN_DNS, etVpnDns.getText().toString());
            intent.putExtra(EXTRA_VPN_PPE, cbVpnPpe.isChecked());

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
		}
	}

    
}
