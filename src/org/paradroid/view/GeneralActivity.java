package org.paradroid.view;

import org.paradroid.DelegatorService;
import org.paradroid.R;
import org.paradroid.Utils;
import org.paradroid.api.GPSSafeListenner;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GeneralActivity extends Activity implements GPSSafeListenner{

	DelegatorService paradroidDelegator; 
	GeneralActivity that;
	Button powerButton;
	private TextView locationStatusValue;

	public void onCreate(Bundle savedInstanceState) {
		that = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.general);

		powerButton = (Button) findViewById(R.id.powerButton);
		locationStatusValue = (TextView) findViewById(R.id.locationStatusValue);

		paradroidDelegator = DelegatorService.getInstance();		
		paradroidDelegator.setGpsSafeListenner(this);		
		if (paradroidDelegator.inSession())
			handleGPSSafe(paradroidDelegator.isGpsReady());

		refreshLayout();
	}

	private void refreshLayout() {
		Utils.log("in refreshLayout()");
		if (paradroidDelegator.inSession()){
			powerButton.setText("End Dive");
			powerButton.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					paradroidDelegator.endSession();
					locationStatusValue.setText("");
					Utils.showLongToast(that, "Ending Dive...");
					refreshLayout();
				}
			});
		} else {
			powerButton.setText("Start Dive");
			powerButton.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					paradroidDelegator.startSession();
					Utils.showLongToast(that, "Starting Dive...");
					locationStatusValue.setText("DO NOT DROP!");
					refreshLayout();
				}
			});
		}
	}

	@Override
	public void handleGPSSafe(boolean safe) {
		if (safe){
			Utils.log("generalInfoActivity.handleGPSSafe(" + safe + ")");
			locationStatusValue.setText("Safe to drop.");		
		} else {
			locationStatusValue.setText("DO NOT DROP!");
		}
	}

}
