package org.paradroid.adk;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Service;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public abstract class ADKService extends Service implements Runnable{
	
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;

	private UsbManager mUsbManager;
	private NavigationConnectedActivity host; 

	public void init(NavigationConnectedActivity host, UsbManager usbManager) {
		this.host = host;
		this.mUsbManager = usbManager; 
	}

	private static final String TAG = "ADKService";

	public void openAccessory(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			host.openAccessory(accessory);
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			Thread thread = new Thread(null, this, "DemoKit");
			thread.start();
			Log.d(TAG, "accessory opened");
			onUsbConncected(true);
		} else {
			Log.d(TAG, "accessory open fail");
		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			handleMsg((byte) msg.what, (Byte) msg.obj);
		}
	};

	public void sendCommand(byte command, int value) {
		byte[] buffer = new byte[2];
		if (value > 255)
			value = 255;

		buffer[0] = command;
		buffer[1] = (byte) value;

		if (mOutputStream != null && buffer[1] != -1) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}

	public void run() {
		int ret = 0;
		byte[] buffer = new byte[16384];
		int i;

		while (ret >= 0) {
			try {
				ret = mInputStream.read(buffer);
			} catch (IOException e) {
				break;
			}

			i = 0;
			while (i < ret) {
				int len = ret - i;
				if (len>=2){
					Message m = Message.obtain(mHandler, buffer[0]);
					m.obj = buffer[1];
					mHandler.sendMessage(m);
				}
				i+=2;
			}
		}
	}

	void closeAccessory() {
		onUsbConncected(false);

		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			if (host!=null)
				host.closeAccessory();
		}
	}

	protected abstract void onUsbConncected(boolean enable);
	protected abstract void handleMsg(byte type, byte value);
	
}
