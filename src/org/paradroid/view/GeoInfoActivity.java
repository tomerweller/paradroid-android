package org.paradroid.view;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.paradroid.DelegatorService;
import org.paradroid.R;
import org.paradroid.Utils;
import org.paradroid.api.GeoInfoListenner;
import org.paradroid.common.LocationRecord;


import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

public class GeoInfoActivity extends Activity implements GeoInfoListenner{
	DelegatorService paradroidDelegator; 
	
	SimpleDateFormat sdf;
	DecimalFormat df;	
	
	private TextView timeStamp;
	private TextView currentLocation;
	private TextView destination;
	private TextView bearingToDestination;
	private TextView distance;
	private TextView currentBearing;
	private TextView currentSpeed; 
	private TextView horizontalSpeed;
	private TextView verticalSpeed; 
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geo);
        connectToLayout();
        sdf = new SimpleDateFormat(Utils.DATE_FORMAT);
        df = new DecimalFormat("#0.000");
        
        paradroidDelegator = DelegatorService.getInstance(); 
        paradroidDelegator.setGeoInfoListenner(this);
	}

	private void connectToLayout(){
		timeStamp = (TextView) findViewById(R.id.timeStampValue);
		currentLocation = (TextView) findViewById(R.id.currentLocationValue);
		destination = (TextView) findViewById(R.id.destinationValue);
		bearingToDestination = (TextView) findViewById(R.id.bearingToDestinationValue);
		distance = (TextView) findViewById(R.id.distanceValue);
		currentBearing = (TextView) findViewById(R.id.currentBearingValue);
		currentSpeed = (TextView) findViewById(R.id.currentSpeedValue);
		horizontalSpeed = (TextView) findViewById(R.id.horziontalSpeedValue);
		verticalSpeed = (TextView) findViewById(R.id.verticalSpeedValue);
	}
	
	private String formatLocation(double altitude, double latitude, double longtitude){
		return df.format(altitude) + " / " + df.format(latitude) + " / " + df.format(longtitude);
	}

	@Override
	public void handleNewLocationRecored(LocationRecord locationRecord) {
		Utils.log("geoInfoActivity.refresh()");
		Location destinationLocation = paradroidDelegator.getDestination();
		
		timeStamp.setText(sdf.format(new Date(locationRecord.getTimestamp())));
		currentLocation.setText(formatLocation(
				locationRecord.getAltitude(),
				locationRecord.getLatitude(),
				locationRecord.getLongtitude()));
		destination.setText(formatLocation(
				destinationLocation.getAltitude(), 
				destinationLocation.getLatitude(), 
				destinationLocation.getLongitude()));
		bearingToDestination.setText(locationRecord.getBearingToDestination() + "d");
		distance.setText(locationRecord.getDistance()+"m");
		currentBearing.setText(locationRecord.getCurrentBearing()+"d");
		currentSpeed.setText(locationRecord.getCurrentSpeed()+"m/s");				// TODO Auto-generated method stub
		horizontalSpeed.setText(df.format(locationRecord.getHorizontalSpeed())+"m/s");
		verticalSpeed.setText(df.format(locationRecord.getVerticalSpeed()) + "m/s");
	}
	
}
