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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

import com.example.rowetalk.util.Logger;
import com.example.rowetalk.R;


/**
 * This Activity appears as a diaLogger. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class MifiDeviceListActivity extends Activity {

    /**
     * Tag for Log
     */
    private static final String TAG = "MifiDeviceListActivity";

    /**
     * Return Intent extra
     */
    public static String EXTRA_DEVICE_NAME = "device_name";
    public static String EXTRA_DEVICE_ADDR = "device_addr";
    public static String EXTRA_DEVICE_RSSI = "device_rssi";

    /**
     * Member fields
     */
    private WifiManager mWifiManager;

    /**
     * Newly discovered devices
     */
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    
    private Button mScanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_wifi_device_list);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        mScanButton = (Button) findViewById(R.id.button_scan);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mWifiManager.startScan();
            	mScanButton.setEnabled(false);
            }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
       
        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);
        
        // Get the local Bluetooth adapter
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);  

        if (!mWifiManager.isWifiEnabled()) {   
        	mWifiManager.setWifiEnabled(true);
    	}   
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }


    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
        	//mWifiManager.

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String[] arr = info.split("\n");
            //String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_NAME, arr[0]);
            intent.putExtra(EXTRA_DEVICE_ADDR, arr[1]);
            //intent.putExtra(EXTRA_DEVICE_RSSI, arr[2]);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // wifi已成功扫描到可用wifi。
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            	List<WifiConfiguration> wcList = mWifiManager.getConfiguredNetworks();
                List<ScanResult> mScanResults = mWifiManager.getScanResults();
                for(ScanResult r: mScanResults){
                	for(WifiConfiguration wc: wcList) {
                		if(wc.SSID.equals(String.format("\"%s\"", r.SSID))) {
                			 mNewDevicesArrayAdapter.add(r.SSID + "\n" + r.BSSID+"\n"+r.frequency);
                		}
                	}
                }
                mNewDevicesArrayAdapter.notifyDataSetChanged();
                mScanButton.setEnabled(true);
                Logger.e(TAG, "mScanResults.size()===" + mScanResults.size());
            } 
        }
    };
}
