package org.paradroid.view;

import org.paradroid.DelegatorService;
import org.paradroid.R;
import org.paradroid.SettingsFactory;
import org.paradroid.Utils;
import org.paradroid.adk.NavigationConnectedActivity;
import org.paradroid.api.SettingsContainer;
import org.paradroid.api.TextMessageReciever;
import org.paradroid.camera.CameraActivity;
import org.paradroid.common.Settings;
import org.paradroid.DelegatorService.ParadroidDelegatorBinder;
import android.app.LocalActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TabHost;

public class MainActivity extends NavigationConnectedActivity implements ServiceConnection, TextMessageReciever, SettingsContainer{

	private Bundle lastSavedInstanceState;
	private LocalActivityManager mlam;

	@Override
	public void connectToLayout(){
		setContentView(R.layout.main);
		
		TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
		mlam.dispatchCreate(lastSavedInstanceState);
		Log.v("MainActivity", "connectoToLayout. Tabhost is : " + tabHost);
		tabHost.setup(mlam);

		TabHost.TabSpec spec;  // Resusable TabSpec for each tab
		Intent intent;  // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
//		intent = new Intent().setClass(this, DummyActivity.class);
//		spec = tabHost.newTabSpec("Dummy").setIndicator("Dummy").setContent(intent);
//		tabHost.addTab(spec);

		intent = new Intent().setClass(this, GeneralActivity.class);
		spec = tabHost.newTabSpec("General").setIndicator("General").setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, GeoInfoActivity.class);
		spec = tabHost.newTabSpec("Geography").setIndicator("Geography").setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, WebInfoActivity.class);
		spec = tabHost.newTabSpec("Web").setIndicator("Web").setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ADKActivity.class);
		spec = tabHost.newTabSpec("ADK").setIndicator("ADK").setContent(intent);
		tabHost.addTab(spec);
		
//		intent = new Intent().setClass(this, CameraActivity.class);
//		spec = tabHost.newTabSpec("Camera").setIndicator("Camera").setContent(intent);
//		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {  
		this.lastSavedInstanceState = savedInstanceState;
		super.onCreate(savedInstanceState);
		mlam = new LocalActivityManager(this, false);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		bindService(new Intent(this, DelegatorService.class), this, BIND_AUTO_CREATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem menuItem){
		Utils.log("in loadPreferencesActivity with menuitem: " + menuItem);
		this.startActivity(new Intent().setClass(this, PreferencesActivity.class));
		return true;
	}

	@Override
	public void onServiceConnected(ComponentName arg0, IBinder service) {
		Utils.log("OnServiceConnected : " + service);

		if (service instanceof ParadroidDelegatorBinder){
			DelegatorService delegator = ((ParadroidDelegatorBinder) service).getDelegator(); 		

			delegator.setMainMessageReciever(this);		
			delegator.setSettingsContainer(this);
			connectToLayout();
		} else
			super.onServiceConnected(arg0, service);
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleTextMessage(String textMessage) {
		Utils.showLongToast(this, textMessage);
	}

	@Override
	public Settings getSettings() {
		return SettingsFactory.getSettingsFromPreferences(this);	
	}

	@Override
	public void onPause(){
		super.onPause();
		mlam.dispatchPause(isFinishing());
	}

	@Override
	public void onResume() {
		super.onResume();
		mlam.dispatchResume(); 
	}

}