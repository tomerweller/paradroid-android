/*
 * Copyright (C) 2011 The Android Open Source Project
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

package org.paradroid.adk;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.paradroid.adk.NavigationService.NavigationServiceBinder;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public abstract class NavigationConnectedActivity extends Activity implements ServiceConnection{

	private static final String TAG = "AbstractADKActivity";
	private static final String ACTION_USB_PERMISSION = "org.paradroid.action.USB_PERMISSION";

	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;

	NavigationService navigationService; 

	protected NavigationService getNavigationService(){
		return navigationService; 
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.v(TAG, "onReceive with " + action);
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						navigationService.openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory "
								+ accessory);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = UsbManager.getAccessory(intent);
				if (accessory != null && accessory.equals(mAccessory)) {
					navigationService.closeAccessory();
				}
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "onCreate");
		navigationService = null; 
		super.onCreate(savedInstanceState);
		bindService(new Intent(this, NavigationService.class), this, BIND_AUTO_CREATE);
	}

	public void onServiceConnected(ComponentName arg0, IBinder service) {
		Log.v(TAG, "ADK Service connected");
		navigationService = ((NavigationServiceBinder) service).getService();

		mUsbManager = UsbManager.getInstance(this);		
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

		navigationService.init(this, mUsbManager);		
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		
		registerReceiver(mUsbReceiver, filter);

		if (getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			navigationService.openAccessory(mAccessory);
		}

		attemptConnectionToUSB();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mAccessory != null) {
			return mAccessory;
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}


	public void attemptConnectionToUSB(){
		Log.v(TAG, "Attempt connection to USB");

		@SuppressWarnings("unused")
		Intent intent = getIntent();
		if (mInputStream != null && mOutputStream != null) {
			navigationService.onUsbConncected(true);
			return;
		}

		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				navigationService.openAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory, mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}		
	}

	@Override
	public void onResume() {
		super.onResume();
		if (navigationService!=null){
			Log.v(TAG, "tryingtoConnectToUsb()");
			attemptConnectionToUSB();
		} else {
			bindService(new Intent(this, NavigationService.class), this, BIND_AUTO_CREATE);
		}
	}


	public void closeAccessory(){
		mAccessory = null; 
	}

	public void openAccessory(UsbAccessory accessory){
		mAccessory = accessory; 
	}

	@Override
	public void onPause() {
		super.onPause();
		//		if (navigationService!=null){
		//			navigationService.closeAccessory();
		//		}
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
	}

	public void setNavigationService(NavigationService navigationService){
		this.navigationService = navigationService; 
	}

	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub

	}

	public abstract void connectToLayout();

}
