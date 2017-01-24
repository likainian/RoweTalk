package com.example.rowetalk.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.UUID;

import com.example.rowetalk.util.Logger;
import com.example.rowetalk.util.StringUtil;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class BtThread extends Thread {
	private static final String TAG = BtThread.class.getName();
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;
    private InputStream mInStream;
    private OutputStream mOutStream;
    private boolean mConnected = false;
    private Handler mHandler;
    private Object lockConnect = new Object();
    private Object lock = new Object();
    private String lockWait= "";
    private int lockResult = -1;
    
            
    public BtThread(Handler handler, BluetoothDevice dev) {
    	mHandler = handler;
    	mDevice = dev;
        BluetoothSocket tmp = null;

        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            tmp = mDevice.createRfcommSocketToServiceRecord(SPP_UUID);
        } catch (IOException e) {
            Logger.e(TAG, "Socket create() failed", e);
        }
        mSocket = tmp;
    }
    
    
    public boolean isConnected() { return mConnected;} 
    
    public String getBtAddr() {
    	return mDevice.getAddress();
    }
    
    

	public void run() {
        Logger.i(TAG, "BEGIN mConnectThread");
        setName("ConnectThread");
        if(mSocket == null){
        	Logger.e(TAG, "BtThread run failed: mSocket is null");
        	mHandler.sendEmptyMessage(BaseThread.MSG_BT_CONNECT_FAILED);
        	return;
        }

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            mSocket.connect();
        } catch (IOException e) {
            // Close the socket
            try {
                mSocket.close();
            } catch (IOException e2) {
                Logger.e(TAG, "unable to close() socket during connection failure", e2);
            }
            Logger.e(TAG, "BtThread run failed: mSocket connect exception, "+e);
            mHandler.sendEmptyMessage(BaseThread.MSG_BT_CONNECT_FAILED);
        	return;
        }
        
        
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = mSocket.getInputStream();
            tmpOut = mSocket.getOutputStream();
        } catch (IOException e) {
            try {
                mSocket.close();
            } catch (IOException e2) {
            	Logger.e(TAG, "unable to close() socket during connection failure", e2);
            }
            Logger.e(TAG, "BtThread run failed: mSocket getInputStream exception, "+e);
            mHandler.sendEmptyMessage(BaseThread.MSG_BT_CONNECT_FAILED);
        	return;
        }            
        mInStream = tmpIn;
        mOutStream = tmpOut;
        
        mConnected = true;
        mHandler.sendEmptyMessage(BaseThread.MSG_BT_CONNECT_SUCCESS);
    	try {
        	synchronized(lockConnect) {
        		lockConnect.notifyAll();
        	}
        } catch (Exception e) {
        	
        }
        
        byte[] buffer = new byte[512];
        int bytes;
        int rdlen=0;
        while (true) {
            try {
            	// Read from the InputStream
            	bytes = mInStream.read(buffer, rdlen, buffer.length-rdlen);
            	if(bytes <= 0){
            		Logger.e(TAG, "bt_read "+bytes+" bytes.");
            		continue;
            	}
            	rdlen += bytes;
            	int found = -1;
            	for(int i=0; i<rdlen-1; i++){
            		if(buffer[i]=='\r' && buffer[i+1]=='\n'){
            			found = i;
            			break;
            		}
            	}
            	if(found>=0){
            		if(found != 0){
            			String s = new String(buffer, 0, found);
            			Logger.e(TAG, "bt_read "+s);
            			synchronized(lock) {
            				if(lockWait.equals(s)){
            					Logger.e(TAG, "lock notify");
            					lockResult = 0;
            					lock.notifyAll();
            				}
            			}
            		}
            		rdlen-= (found+2);
            		if(rdlen > 0) {
            			System.arraycopy(buffer, found+2, buffer, 0, rdlen);
            		}
            	}
            	
            } catch (IOException e) {
                Logger.e(TAG, "read exception: "+ e);  
                break;
            }
        }
        try {
            mSocket.close();
        } catch (IOException e2) {
        	Logger.e(TAG, "mmSocket close failure", e2);
        }
        mConnected = false;
    }

	public boolean write_and_wait(String cmd, String resp){
		if(StringUtil.isEmptyOrNull(cmd) || StringUtil.isEmptyOrNull(resp)){
			return false;
		}
		if(!mConnected){
			return false;
		}
		try {  
			Logger.e(TAG, "write_and_wait: cmd="+cmd+", wait="+resp);
            mOutStream.write(cmd.getBytes()); 
            synchronized(lock) {
            	lockResult = -1;
				lockWait = resp;
				lock.wait(2*1000);
				if(lockResult == 0){
					lockResult = -1;
					lockWait = "";
					return true;
				}
			}
        } catch (Exception e) {
        	Logger.e(TAG, "Exception during write", e);
        } 
		
		return false;
		
	}
    
    public void cancel() {
    	Logger.e(TAG, "cancel");
        try {
        	synchronized(lock) {
        		lock.notifyAll();
        	}
            mSocket.close();
        } catch (IOException e) {
        	Logger.e(TAG, "close() of connect socket failed", e);
        }
        mConnected = false;
    }

	public boolean wait4connect(long ms) {
		// TODO Auto-generated method stub
		if(!mConnected){
			try {  
	            synchronized(lockConnect) {
	            	lockConnect.wait(ms);
				}
	        } catch (Exception e) {
	        	Logger.e(TAG, "wait4connect Exception:"+ e);
	        } 
		}
		return mConnected;
	}
}
