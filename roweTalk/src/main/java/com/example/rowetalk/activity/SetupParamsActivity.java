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
import android.widget.EditText;
import android.widget.Toast;

import com.example.rowetalk.util.NetworkUtil;
import com.example.rowetalk.util.PlatformUtil;
import com.example.rowetalk.util.RootShellCmd;
import com.example.rowetalk.util.SleepObject;
import com.example.rowetalk.util.StringUtil;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.rowetalk.R;
import com.example.rowetalk.thread.BaseThread;


/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class SetupParamsActivity extends Activity implements OnClickListener {

    /**
     * Tag for Log
     */
    private static final String TAG = "SetupParamsActivity";

    /**
     * Return Intent extra
     */
    public static String EXTRA_POOL_SEQ = "pool_seq";
    public static String EXTRA_PHONE_SEQ = "phone_seq";
    public static String EXTRA_SLOT_SEQS = "slot_seqs";
    public static String EXTRA_SIM_START = "sim_start";
    public static String EXTRA_NOSIM_VDEVICE_NUM = "nosim_vdevice_num";
    public static String EXTRA_NOSIM_START = "nosim_start";
    public static String EXTRA_IME_WAIT_TIME = "ime_wait_time";

    public static String EXTRA_NO_SIM = "no_sim";
   
    private EditText etPhoneSeq;
    private EditText etPoolSeq;
    private EditText etSlotSeqs;
    private EditText etSimStart;
    private EditText etNosimVDeviceNum;
    private EditText etImeWaitTime;
    private EditText etNosimStart;
    
    
    private Button bnCancel, bnOK;
    private boolean noSim = false;
    private SleepObject sleepObject = new SleepObject();
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_setup_params);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);
        
        etPhoneSeq = (EditText)findViewById(R.id.etPhoneSeq);
        etPoolSeq = (EditText)findViewById(R.id.etPoolSeq);
        etSlotSeqs = (EditText)findViewById(R.id.etSlotSeqs);
        etSimStart = (EditText)findViewById(R.id.etSimStart);
        etNosimVDeviceNum = (EditText)findViewById(R.id.etNosimVDeviceNum);
        etNosimStart = (EditText)findViewById(R.id.etNosimStart);
        etImeWaitTime = (EditText)findViewById(R.id.etImeWaitTime);

        bnCancel = (Button)findViewById(R.id.button_cancel);
        bnOK = (Button)findViewById(R.id.button_ok);
        
        bnCancel.setOnClickListener(this);
        bnOK.setOnClickListener(this);
       
        Intent intent = getIntent();
        int phone_seq = intent.getIntExtra(EXTRA_PHONE_SEQ, 0);
        int pool_seq  = intent.getIntExtra(EXTRA_POOL_SEQ, 0);
        int sim_start = intent.getIntExtra(EXTRA_SIM_START, 0);
        noSim = intent.getBooleanExtra(EXTRA_NO_SIM, false);
        etPhoneSeq.setText(phone_seq==0?"":Integer.toString(phone_seq));
        if(noSim){
        	etPoolSeq.setEnabled(false);
        	etSlotSeqs.setEnabled(false);
        	etSimStart.setEnabled(false);
        }else{
	        etPoolSeq.setText(pool_seq==0?"":Integer.toString(pool_seq));
	        etSlotSeqs.setText(intent.getStringExtra(EXTRA_SLOT_SEQS));
	        etSimStart.setText(Integer.toString(sim_start));
        }
        etNosimVDeviceNum.setText(Integer.toString(intent.getIntExtra(EXTRA_NOSIM_VDEVICE_NUM, 0)));
        etNosimStart.setText(Integer.toString(intent.getIntExtra(EXTRA_NOSIM_START, 0)));
        etImeWaitTime.setText(Integer.toString(intent.getIntExtra(EXTRA_IME_WAIT_TIME, 0)));
        new Thread(new Runnable() {
			public void run() {
				RootShellCmd.getInstance().setImeLatin();
		        PlatformUtil.switchVPN(SetupParamsActivity.this, false, null, sleepObject);
			}
		}).start();
    }


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == bnCancel){
			finish();
		}else if(v==bnOK) {
			String idString = etPhoneSeq.getText().toString();
			int id = 0;
			if(!StringUtil.isEmpty(idString)){
				id = Integer.parseInt(idString);
			}
			if(id <=0 || id > 9999 ) {
				Toast.makeText(this, "手机未设置编号！", Toast.LENGTH_SHORT).show();
				return;
			}
			int nosim_num = 0;
			idString = etNosimVDeviceNum.getText().toString();
			if(!StringUtil.isEmpty(idString)){
				nosim_num = Integer.parseInt(idString);
			}
			if(nosim_num <=100 || nosim_num > 500000 ) {
				Toast.makeText(this, "无卡虚拟设备数目设置不正确！", Toast.LENGTH_SHORT).show();
				return;
			}
			int nosim_start = -1;
			idString = etNosimStart.getText().toString();
			if(!StringUtil.isEmpty(idString)){
				nosim_start = Integer.parseInt(idString);
			}
			if(nosim_start <0 || nosim_start >= nosim_num ) {
				Toast.makeText(this, "无卡起始数值设置不正确！", Toast.LENGTH_SHORT).show();
				return;
			}
			id = -1;
			idString = etImeWaitTime.getText().toString();
			if(!StringUtil.isEmpty(idString)){
				id = Integer.parseInt(idString);
			}
			if(id <0 || id > 240 ) {
				Toast.makeText(this, "输入法等待时间设置不正确！", Toast.LENGTH_SHORT).show();
				return;
			}
			int simStart = 0; 
			if(!noSim){
				id = 0;
				idString = etPoolSeq.getText().toString();
				if(!StringUtil.isEmpty(idString)){
					id = Integer.parseInt(idString);
				}
				if(id <0 || id > 9999 ) {
					Toast.makeText(this, "卡池未设置编号！", Toast.LENGTH_SHORT).show();
					return;
				}
				id = 0;
				idString = etSlotSeqs.getText().toString();
				if(StringUtil.isEmpty(idString)){
					Toast.makeText(this, "卡槽编号未设置！", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Pattern p=Pattern.compile("^[0-9|\\-|\\s]+$");  
			    Matcher m=p.matcher(idString);  
			    if(!m.matches()){  
			    	Toast.makeText(this, "卡槽编号未设置！", Toast.LENGTH_SHORT).show();
					return;
			    } 
			    ArrayList<Integer> list = new ArrayList<Integer>();
				if(!BaseThread.initSlotSeqList(list, idString)){
					Toast.makeText(this, "卡槽编号格式不正确！", Toast.LENGTH_SHORT).show();
					return;
				}
				
				idString = etSimStart.getText().toString();
				if(!StringUtil.isEmpty(idString)){
					simStart = Integer.parseInt(idString);
				}
				
			}
				
			Intent intent = new Intent();
            intent.putExtra(EXTRA_PHONE_SEQ, Integer.parseInt(etPhoneSeq.getText().toString()));
            if(!noSim){
	            intent.putExtra(EXTRA_POOL_SEQ, Integer.parseInt(etPoolSeq.getText().toString()));
	            intent.putExtra(EXTRA_SLOT_SEQS, etSlotSeqs.getText().toString());	
	            intent.putExtra(EXTRA_SIM_START, simStart);
            }
            intent.putExtra(EXTRA_NOSIM_VDEVICE_NUM, nosim_num);
            intent.putExtra(EXTRA_NOSIM_START, nosim_start);
            intent.putExtra(EXTRA_IME_WAIT_TIME, Integer.parseInt(etImeWaitTime.getText().toString()));
            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
		}
	}

    
}
