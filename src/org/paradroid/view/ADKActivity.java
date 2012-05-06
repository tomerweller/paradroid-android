package org.paradroid.view;

import java.text.DecimalFormat;

import org.paradroid.R;
import org.paradroid.adk.NavigationListenner;
import org.paradroid.adk.NavigationService;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ADKActivity extends Activity implements NavigationListenner{

	TextView distanceTextView;
	TextView statusTextView; 
	TextView lightIndicatorTextView;

	Button testResetButton;
	Button testLeftServoButton;
	Button testRightServoButton; 	
	Button testFlareButton;
	Button testGoButton;
	Button testNoGoButton; 

	NavigationService navigationService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.adk);
		distanceTextView = (TextView) findViewById(R.id.distanceValueTextField);
		statusTextView = (TextView) findViewById(R.id.statusTextView);
		lightIndicatorTextView = (TextView) findViewById(R.id.lightIndicatorTextview);


		testResetButton = (Button) findViewById(R.id.testResetButton);
		testLeftServoButton = (Button) findViewById(R.id.testLeftServoButton);
		testRightServoButton = (Button) findViewById(R.id.testRightServoButton);
		testFlareButton = (Button) findViewById(R.id.testFlareButton);
		testGoButton = (Button) findViewById(R.id.testGoButton);
		testNoGoButton = (Button) findViewById(R.id.testNoGoButton);
		navigationService = NavigationService.getInstance();
		connectToLayout();
	}

	public void changeStatus(String statusMsg){
		if (statusTextView!=null)
			statusTextView.setText(statusMsg);
	}

	public void handleNewRangeFinderDistance(int distance) {
		DecimalFormat df = new DecimalFormat("#0.000");
		distanceTextView.setText(df.format(distance)+"\" , " + df.format(distance*2.54) + "cm");
	}

	public void connectToLayout() {
		testResetButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				navigationService.resetToTop();
			}
		});

		testLeftServoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				navigationService.pullLeft();
			}
		});

		testRightServoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				navigationService.pullRight();
			}
		});

		testFlareButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				navigationService.permFlare();
			}
		});		

		testGoButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				navigationService.readyToGo(true);
			}
		});

		testNoGoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				navigationService.readyToGo(false);
			}
		});		

		navigationService.registerListenner(this);		
	}

	@Override
	public void handleLight(boolean on) {
		lightIndicatorTextView.setText(on ? "Light On" : "Light Off");
	}
	
	@Override
	public void onUsbConnected(boolean enable){
		changeStatus(enable ? "USB Connected" : "USB Disconnected");
	}
	

}
