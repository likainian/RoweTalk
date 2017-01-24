package com.example.rowetalk.receiver;

import com.example.rowetalk.service.MainService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MainReceiver extends BroadcastReceiver {
	public MainReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO: This method is called when the BroadcastReceiver is receiving
		// an Intent broadcast.
		//throw new UnsupportedOperationException("Not yet implemented");
		intent.setClass(context, MainService.class);
        context.startService(intent); 
	}
}
