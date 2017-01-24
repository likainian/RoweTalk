package com.example.rowetalk.util;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.res.Configuration;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;
import java.util.Locale;

import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.example.rowetalk.bean.VpnProfile;

import android.security.Credentials;
import android.security.KeyStore;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;


public class PlatformUtil {
	private static final String TAG = PlatformUtil.class.getName();

	public static boolean switch4G(Context context, boolean on, SleepObject sleep){
	    TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	    if(tm == null){
			Log.e(TAG, "switch4G: telMgr is null");
		    return false;
	    }
	    try {
        	boolean b = tm.getDataEnabled();
        	if(b == on){
        		return true;
        	}else {
    			tm.setDataEnabled(on);
        		for(int i=0; i<10; i++){
        			if(sleep != null){
    					if(!sleep.sleep(1000)) break;
    				}
    				if(tm.getDataEnabled()==on)
    					return true;
    			}
        	}
        } catch (Exception e) {
        	e.printStackTrace();
            Log.e(TAG, "switch4G: exception" + e);
        }
	   
		return false;
    }
	   
	public static boolean isVPNConnected(Context context) {
    	return RootShellCmd.getInstance().checkIpRouteDeviceExist("ppp"); //ppp0
    }
	
    public static boolean switchVPN(Context context, boolean on, VpnProfile vpn, SleepObject sleep){
    	Logger.e(TAG, "switchVPN "+on);
    	boolean b = isVPNConnected(context);
	    if(on == b){
		   return true;
	    } 
	    IConnectivityManager mService = IConnectivityManager.Stub
	            .asInterface(ServiceManager.getService(Context.CONNECTIVITY_SERVICE)); 
	    if(mService == null){
			Log.e(TAG, "switchVPN: mService is null");
		   return false;
	    }
	   
	    long t1 = System.currentTimeMillis(); 
	    if(on){
		   try {
			    com.android.internal.net.VpnProfile profile = new com.android.internal.net.VpnProfile(String.format("%s-%s", vpn.account.getServer(), vpn.account.getUsername()));
			    profile.name = vpn.account.getServer();
		        profile.type = com.android.internal.net.VpnProfile.TYPE_PPTP;
		        profile.server = vpn.account.getServer();
		        profile.username = vpn.account.getUsername();
		        profile.password = vpn.account.getPassword();
		        profile.searchDomains = "";
		        profile.dnsServers = vpn.account.getDns()!=null?vpn.account.getDns():"8.8.8.8";
		        profile.routes = "";
		        profile.mppe = vpn.account.getPpe()!=0;
		        profile.saveLogin = false;
	            mService.startLegacyVpn(profile);
	            Logger.e(TAG, "switchVPN on: "+profile.server +" "+profile.username);
	            
	            for(int i=0; i<10; i++){
	            	if(sleep != null){
						if(!sleep.sleep(3000)) break;
					}
					if(isVPNConnected(context)) {
						Logger.e(TAG, "switchVPN on cost "+(System.currentTimeMillis()-t1)+" ms");
						return true;
					}
					Logger.e(TAG, "switchVPN on retry: "+i);
				}
	        } catch (Exception e) {
	            e.printStackTrace();
				Log.e(TAG, "connectVPN Exception: "+e);
	        }
		}else{
			try {
				/*
				LegacyVpnInfo info = mService.getLegacyVpnInfo();
				if (info != null) {
					Log.e(TAG, "disconnectVPN: "+info.key);
					mService.prepareVpn(VpnConfig.LEGACY_VPN, VpnConfig.LEGACY_VPN);
				}else{
					Log.e(TAG, "disconnectVPN: not found legacy");
				}*/
				Logger.e(TAG, "switchVPN off");
				mService.prepareVpn(VpnConfig.LEGACY_VPN, VpnConfig.LEGACY_VPN);
				for(int i=0; i<5; i++){
					if(sleep != null){
						if(!sleep.sleep(2000)) break;
					}
					if(!isVPNConnected(context)) {
						Logger.e(TAG, "switchVPN off cost "+(System.currentTimeMillis()-t1)+" ms");
						return true;
					}
					Logger.e(TAG, "switchVPN off retry: "+i);
				}
			} catch (Exception e) {
                Log.e(TAG, "disconnectVPN Exception: "+e);
            }
		}
		return false;
   }
    
    /**
     * Requests the system to update the system locale. Note that the system looks halted
     * for a while during the Locale migration, so the caller need to take care of it.
     */
    public static boolean updateLocale(Locale locale) {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();

            // Will set userSetLocale to indicate this isn't some passing default - the user
            // wants this remembered
            config.setLocale(locale);

            am.updateConfiguration(config);
            // Trigger the dirty bit for the Settings Provider.
            BackupManager.dataChanged("com.android.providers.settings");
            return true;
        } catch (RemoteException e) {
            // Intentionally left blank
        	e.printStackTrace();
        	return false;
        }
    }

}
