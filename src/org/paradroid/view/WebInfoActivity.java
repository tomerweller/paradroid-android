package org.paradroid.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.paradroid.DelegatorService;
import org.paradroid.R;
import org.paradroid.ServerConnector;
import org.paradroid.Utils;
import org.paradroid.api.WebInfoListenner;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class WebInfoActivity extends Activity implements WebInfoListenner{
	SimpleDateFormat sdf;
	DelegatorService paradroidDelegator; 
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sdf = new SimpleDateFormat(Utils.DATE_FORMAT);
		setContentView(R.layout.web);
		connectToLayout();
		
		paradroidDelegator = DelegatorService.getInstance();
		paradroidDelegator.setWebInfoActivity(this);
		handleInfoSent();
	}

	TextView dataConnectionAvailabilityText;
	TextView sentAndTotalText;
	TextView lastReportOnText;

	private void connectToLayout() {
		dataConnectionAvailabilityText = (TextView) findViewById(R.id.dataConnectionAvailabilityValue);
		sentAndTotalText = (TextView) findViewById(R.id.sentAndTotalValue);
		lastReportOnText = (TextView) findViewById(R.id.lastReportedOnValue);
	}

	@Override
	public void handleInfoSent() {
		Utils.log("webInfoActivity.refresh()");
		ServerConnector serverConnector = paradroidDelegator.getServerConnector();
		if (serverConnector == null){
			Utils.log("ServerConnector is null");
			return;		
		}
		
		dataConnectionAvailabilityText.setText(paradroidDelegator.getLastKnownLocation().isOnline()+"");
		sentAndTotalText.setText(serverConnector.getTotalSent() + " / " + serverConnector.getTotalRecieved());
		Date date = serverConnector.getLastReportOn();
		if (date!=null)
			lastReportOnText.setText(sdf.format(date));
		
	}
}
