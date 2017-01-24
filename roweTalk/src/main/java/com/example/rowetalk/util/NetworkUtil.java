package com.example.rowetalk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import com.example.rowetalk.activity.MainActivity;
import com.example.rowetalk.bean.VpnProfile;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

/**
 * Created by hongbiao on 2015/10/9.
 */
public class NetworkUtil {
	private static final String TAG = NetworkUtil.class.getName();
    public static boolean isIp(final String IP_string){//判断是否是一个IP
        boolean b = false;
        String IP = IP_string.trim();
        if(IP.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")){
            String s[] = IP.split("\\.");
            int p1 = Integer.parseInt(s[0]);
            int p2 = Integer.parseInt(s[1]);
            int p3 = Integer.parseInt(s[2]);
            int p4 = Integer.parseInt(s[3]);
            if(p1>0&&p1<255)
                if(p2<255)
                    if(p3<255)
                        if(p4>0&&p4<255)
                            b = true;
        }
        return b;
    }
    
    public static String getWifiMacAddr(Context c) {
        //在wifi未开启状态下，仍然可以获取MAC地址，但是IP地址必须在已连接状态下否则为0
        WifiManager wifiMgr = (WifiManager)c.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
        if (null != info) {
            return info.getMacAddress();
        }else {
            return null;
        }

    }
    
    public static String getBtMacAddr(Context c) {
        return BluetoothAdapter.getDefaultAdapter().getAddress();

    }
    
    public static boolean isNetworkConnected(Context context) {
        boolean bConnected = false;
        if (context == null) {
            return bConnected;
        }
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            NetworkInfo[] infos = connManager.getAllNetworkInfo();
            for (NetworkInfo info : infos) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    bConnected = true;
                    break;
                }
            }
        }
        return bConnected;
    }
    
    public static boolean isWifiOpened(Context context) {
    	WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
 	   return wifiManager.isWifiEnabled();
    }
    public static boolean isWifiConnected(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isMobileOpened(Context context) {
    	final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return (networkInfo != null && networkInfo.isAvailable()) ;
    }
    public static boolean isMobileConnected(Context context) {
    	final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return (networkInfo != null && networkInfo.isConnected()) ;
    }
    
    public static boolean isSimcardAvailable(Context contex) {
    	TelephonyManager tm = (TelephonyManager)contex.getSystemService(Context.TELEPHONY_SERVICE);//取得相关系统服务
		// StringBuffer sb = new StringBuffer();
		 if (tm.getSimState()!=TelephonyManager.SIM_STATE_READY)
			 return false;
		 if(tm.getSubscriberId().equals(""))
			 return false;
		 if (tm.getNetworkOperator().equals(""))
			 return false;
		 if (tm.getNetworkOperatorName().equals(""))
			 return false;
		 if (tm.getNetworkType() == 0)
			 return false;
		 return true;
    }
    public static String getImsi(Context contex) {
    	// String IMSI =android.os.SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMSI);
    	TelephonyManager tm = (TelephonyManager)contex.getSystemService(Context.TELEPHONY_SERVICE);//取得相关系统服务
    	return tm.getSubscriberId();
    }
    public static String getImei(Context contex) {
    	// String IMSI =android.os.SystemProperties.get(android.telephony.TelephonyProperties.PROPERTY_IMEI);
    	TelephonyManager tm = (TelephonyManager)contex.getSystemService(Context.TELEPHONY_SERVICE);//取得相关系统服务
    	return tm.getDeviceId();
    }
    public static String readSIMCard(Context contex) {
		 TelephonyManager tm = (TelephonyManager)contex.getSystemService(Context.TELEPHONY_SERVICE);//取得相关系统服务
		 StringBuffer sb = new StringBuffer();
		 switch(tm.getSimState()){ //getSimState()取得sim的状态 有下面6中状态
		 case TelephonyManager.SIM_STATE_ABSENT :sb.append("无卡");break; 
		 case TelephonyManager.SIM_STATE_UNKNOWN :sb.append("未知状态");break;
		 case TelephonyManager.SIM_STATE_NETWORK_LOCKED :sb.append("需要NetworkPIN解锁");break;
		 case TelephonyManager.SIM_STATE_PIN_REQUIRED :sb.append("需要PIN解锁");break;
		 case TelephonyManager.SIM_STATE_PUK_REQUIRED :sb.append("需要PUK解锁");break;
		 case TelephonyManager.SIM_STATE_READY :sb.append("良好");break;
		 }

		 if(tm.getSimSerialNumber()!=null){
		 sb.append("@" + tm.getSimSerialNumber().toString());
		 }else{
		 sb.append("@无法取得SIM卡号");
		 }

		 if(tm.getSimOperator().equals("")){
		 sb.append("@无法取得供货商代码");
		 }else{
		 sb.append("@" + tm.getSimOperator().toString());
		 }

		 if(tm.getSimOperatorName().equals("")){
		 sb.append("@无法取得供货商");
		 }else{
		 sb.append("@" + tm.getSimOperatorName().toString());
		 }

		 if(tm.getSimCountryIso().equals("")){
		 sb.append("@无法取得国籍");
		 }else{
		 sb.append("@" + tm.getSimCountryIso().toString());
		 }

		 if (tm.getNetworkOperator().equals("")) {
		 sb.append("@无法取得网络运营商");
		 } else {
		 sb.append("@" + tm.getNetworkOperator());
		 }
		 if (tm.getNetworkOperatorName().equals("")) {
		 sb.append("@无法取得网络运营商名称");
		 } else {
		 sb.append("@" + tm.getNetworkOperatorName());
		 }
		 if (tm.getNetworkType() == 0) {
		 sb.append("@无法取得网络类型");
		 } else {
		 sb.append("@" + tm.getNetworkType());
		 }
		 return sb.toString();
    }

	public static int getSimState(Context context) {
		// TODO Auto-generated method stub
		return ((TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE)).getSimState();
	}
	
	
    public static String getLocalIpAddress(Context context) {  
       try {  
           WifiManager wifiManager = (WifiManager) context  
                   .getSystemService(Context.WIFI_SERVICE);  
           WifiInfo wifiInfo = wifiManager.getConnectionInfo();  
           return int2ip(wifiInfo.getIpAddress());  
       } catch (Exception ex) {  
           ex.printStackTrace();
       }  
       return "";  
   }  
    
    public static String getWifiSSID(Context context) {
    	boolean b = isWifiConnected(context);
    	if(b){
    		WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
    		return mWifiManager.getConnectionInfo().getRawSSID();
    	}
    	return "";
    }
    public static String getWifiBSSID(Context context) {
    	boolean b = isWifiConnected(context);
    	if(b){
    		WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
    		return mWifiManager.getConnectionInfo().getRawBSSID();
    	}
    	return "";
    }
    
   public static boolean switchWiFi(Context context, boolean onoff, SleepObject sleepObj) {
	   boolean b = isWifiConnected(context);
	   if(b == onoff){
		   return true;
	   }
	   WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
	   if(!wifiManager.setWifiEnabled(onoff)){
		   return false;
	   }
	   for(int i=0; i<10; i++){
			if(NetworkUtil.isWifiConnected(context)) {
				if(onoff) {
					return true;
				}
			}else {
				if(!onoff){
					return true;
				}
			}
			if(sleepObj != null){
				if(!sleepObj.sleep(1000)) break;
			}
	   }
	   return false;
   }
   /*public static boolean switchAPN(Context context, boolean onoff) {
	   TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);  
	   return telManager.setDataEnabled(onoff);
   }*/
   /*
   public static boolean switch4G(Context context, boolean on, SleepObject sleep){
	   boolean b = NetworkUtil.isMobileConnected(context);
	   if(on == b){
		   return true;
	   }
	   if(on){
		    Intent intent = new Intent("ty.intent.action.switch_apn");
		    intent.putExtra("switch","on");
			context.sendBroadcast(intent);
			for(int i=0; i<10; i++){
				if(NetworkUtil.isMobileConnected(context))
					return true;
				if(sleep != null){
					if(!sleep.sleep(1000)) break;
				}
			}
		}else{
			Intent intent = new Intent("ty.intent.action.switch_apn");
		    intent.putExtra("switch","off");
			context.sendBroadcast(intent);
			for(int i=0; i<10; i++){
				if(!NetworkUtil.isMobileConnected(context))
					return true;
				if(sleep != null){
					if(!sleep.sleep(1000)) break;
				}
			}
		}
		return false;
   }
   
   public static boolean switchVPN(Context context, boolean on, VpnProfile profile, SleepObject sleep){
	   boolean b = NetworkUtil.isVPNConnected(context);
	   if(on == b){
		   return true;
	   }
	   long t1 = System.currentTimeMillis(); 
	   if(on){
		    Intent intent = new Intent("ty.intent.action.switch_vpn");
		    intent.putExtra("switch","on");
		    intent.putExtra("server",profile.vpn.getServer()); //"bjlt01.adslip.cc");
		    intent.putExtra("username",profile.account.getAccount());//"xxbook1");
		    intent.putExtra("password",profile.account.getPassword());//"abc@9931");
		    intent.putExtra("dns",profile.dns);//"8.8.8.8");
		    intent.putExtra("ppe",profile.ppe);//true);
			context.sendBroadcast(intent);
			for(int i=0; i<10; i++){
				if(NetworkUtil.isVPNConnected(context)) {
					Logger.e(TAG, "switchVPN on cost "+(System.currentTimeMillis()-t1)+" ms");
					return true;
				}
				if(sleep != null){
					if(!sleep.sleep(1000)) break;
				}
			}
		}else{
			Intent intent = new Intent("ty.intent.action.switch_vpn");
		    intent.putExtra("switch","off");
			context.sendBroadcast(intent);
			for(int i=0; i<5; i++){
				if(!NetworkUtil.isVPNConnected(context)) {
					Logger.e(TAG, "switchVPN off cost "+(System.currentTimeMillis()-t1)+" ms");
					return true;
				}
				if(sleep != null){
					if(!sleep.sleep(1000)) break;
				}
			}
		}
		return false;
   }
   */
   
   public static boolean wifiConnectTo(Context context, String ssid, String bssid, SleepObject sleep){
		//postInfo("wifiConnectTo: "+ssid+", "+bssid);
	    WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
		List<WifiConfiguration> wcList = mWifiManager.getConfiguredNetworks();
		for(WifiConfiguration wc: wcList){
			//if(wc.BSSID.equals(bssid)) {
			if(wc.SSID.equals(String.format("\"%s\"", ssid))) {
				//postInfo("connecting to: "+ssid);
				mWifiManager.enableNetwork(wc.networkId, true);
				break;
			}
		}
		for(int i=0; i<10; i++){
			if(mWifiManager.getConnectionInfo().getRawBSSID().equals(bssid)) 
				break;
			if(sleep != null){
				if(!sleep.sleep(1000)) break;
			}
		}
		return mWifiManager.getConnectionInfo().getRawBSSID().equals(bssid);
	}

   public static boolean connectedWifi(Context context, String wifiAddr) {
	    Logger.e(TAG, "connectedWifi: "+wifiAddr);
		WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		return wifiAddr != null && wm.getConnectionInfo().getRawBSSID().equals(wifiAddr);
   }
   
   public static boolean testNetworkConnect(SleepObject sleep){
	   String[] urls = new String[]{"baidu.com", "qq.com", "163.com"};
	   for(String s:urls){
		   if(ping(s)){
			   return true;
		   }
		   if(sleep != null){
			   if(!sleep.sleep(1000)) break;
		   }
	   }
	   return false;
   }
   
   /*
   * @author sichard
   * @category 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
   * @return
   */ 
   public static final boolean ping(String ip) { 
      String result = null; 
      if(ip == null){
    	  ip = "qq.com";// ping 的地址，可以换成任何一种可靠的外网 
      }
      try { 
          Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次 
          // 读取ping的内容，可以不加 
          InputStream input = p.getInputStream(); 
          BufferedReader in = new BufferedReader(new InputStreamReader(input)); 
          StringBuffer stringBuffer = new StringBuffer(); 
          String content = ""; 
          while ((content = in.readLine()) != null) { 
                  stringBuffer.append(content); 
          } 
          Logger.d("------ping-----", "result content : " + stringBuffer.toString()); 
          // ping的状态 
          int status = p.waitFor(); 
          if (status == 0) { 
              result = "success"; 
              return true; 
          } else { 
              result = "failed"; 
          } 
      } catch (IOException e) { 
              result = "IOException"; 
      } catch (InterruptedException e) { 
              result = "InterruptedException"; 
      } finally { 
              Logger.d("----result---", "result = " + result); 
      } 
      return false;
  }

   public static String getRawIpV6() {
	   int loop1=0, loop2=0;
       try {
    	   Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
    	   Logger.e(TAG, "getRawIpV6: NetworkInterface "+en.hasMoreElements());
           while(en.hasMoreElements()) {
               NetworkInterface intf = en.nextElement();
               Enumeration<InetAddress> enumIpAddr = intf.getRawInetAddresses();
               Logger.e(TAG, "getRawIpV6: InetAddress "+enumIpAddr.hasMoreElements());
               while(enumIpAddr.hasMoreElements()) {
                   InetAddress inetAddress = enumIpAddr.nextElement();
                   Logger.e(TAG, "getRawIpV6: "+String.format("(%d,%d) isloopback: ", loop1, loop2)+inetAddress.isLoopbackAddress());
                   loop2++;
                   if (!inetAddress.isLoopbackAddress()) {
                       return inetAddress.getHostAddress();
                   }
               }
               loop1++;
           }
       } catch (SocketException ex) {
           ex.printStackTrace();
       }
       return null;
   }
   public static String getIpV6() {
	   int loop1=0, loop2=0;
       try {
    	   Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
    	   Logger.e(TAG, "getIpV6: NetworkInterface "+en.hasMoreElements());
           while(en.hasMoreElements()) {
               NetworkInterface intf = en.nextElement();
               Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
               Logger.e(TAG, "getIpV6: InetAddress "+enumIpAddr.hasMoreElements());
               while(enumIpAddr.hasMoreElements()) {
                   InetAddress inetAddress = enumIpAddr.nextElement();
                   Logger.e(TAG, "getIpV6: "+String.format("(%d,%d) isloopback: ", loop1, loop2)+inetAddress.isLoopbackAddress());
                   loop2++;
                   if (!inetAddress.isLoopbackAddress()) {
                       return inetAddress.getHostAddress();
                   }
               }
               loop1++;
           }
       } catch (SocketException ex) {
           ex.printStackTrace();
       }
       return null;
   }
   
   // ipv4
   public static String getIpV4(Context context) {
       WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
       try {
           if (wifi.isWifiEnabled()) {
               WifiInfo info = wifi.getConnectionInfo();
               int ipAddress = info.getIpAddress();
               return int2ip(ipAddress);
           }
       } catch (Exception e){
    	   e.printStackTrace();
       }
       return null;
   }

   public static int ip2int(String ipAddress) {  
		int result = 0;  
		try {
			String[] ipAddressInArray = ipAddress.split("\\.");  
			 
			for (int i = 0; i<4; i++) {  
			    int ip = Integer.parseInt(ipAddressInArray[i]);  
			    result |= ip << (i * 8);  
			} 
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;  
	}
   public static String int2ip(int i) {
       return (i & 0xFF) + "." +
               ((i >> 8) & 0xFF) + "." +
               ((i >> 16) & 0xFF) + "." +
               (i >> 24 & 0xFF);
   } 

}
