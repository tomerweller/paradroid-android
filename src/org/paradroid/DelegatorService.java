package org.paradroid;

import java.util.List;

import org.paradroid.adk.NavigationService;
import org.paradroid.api.GPSSafeListenner;
import org.paradroid.api.GeoInfoListenner;
import org.paradroid.api.SettingsContainer;
import org.paradroid.api.TextMessageReciever;
import org.paradroid.api.WebInfoListenner;
import org.paradroid.common.LocationRecord;
import org.paradroid.common.Settings;
import org.paradroid.common.DestinationInfo;
import org.paradroid.view.MainActivity;
import org.paradroid.view.WebInfoActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DelegatorService extends Service implements LocationListener{

	private static final long GOOD_DELTA_LENGTH = 4000; 
	private static final int GOOD_DELTA_SEQ = 5;

	private static final String TAG = "DelegatorService";

	private WebInfoListenner webInfoListenner; 
	public WebInfoListenner getWebInfoListenner() {
		return webInfoListenner;
	}

	private GPSSafeListenner gpsSafeListenner; 
	private SettingsContainer settingsContainer; 
	private TextMessageReciever mainMessageReciever;
	private GeoInfoListenner geoInfoListenner; 

	private boolean inSession;
	private LocationRecord lastLocationRecord;
	private Location destinationLocation;

	private LocationManager locationManager;
	private NotificationManager notificationManager;
	private Settings settings;
	private ServerConnector serverConnector;
	private static DelegatorService instance; 

	private NavigationService navigationService;

	public static DelegatorService getInstance(){
		return instance; 
	}

	public class ParadroidDelegatorBinder extends Binder{
		public DelegatorService getDelegator(){
			return DelegatorService.this;
		}
	}

	private final IBinder binder = new ParadroidDelegatorBinder();	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate(){
		instance = this;

		gpsSafeListenner = null;
		mainMessageReciever = null; 
		geoInfoListenner = null; 
		webInfoListenner = null;
		navigationService = null; 

		inSession = false;
		lastLocationRecord = new LocationRecord();
		destinationLocation = new Location("Paradroid");


		locationManager =  (LocationManager) getSystemService(LOCATION_SERVICE);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification(int textId) {

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon, getText(textId), System.currentTimeMillis());
		notification.flags = Notification.FLAG_ONGOING_EVENT;

		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.app_name), getText(textId), contentIntent);

		// Send the notification.
		notificationManager.notify(textId, notification);		
	}

	private void switchNotification(int oldTextId, int newTextId){
		notificationManager.cancel(oldTextId);
		showNotification(newTextId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Utils.log("Service started with ID : " + startId);
		return START_STICKY;
	}

	@Override
	public void onDestroy(){
		notificationManager.cancel(R.string.notificationNotActive);
		Toast.makeText(this, "Paradroid Service Stopped", Toast.LENGTH_SHORT).show();
	}

	private DestinationInfo getDefaultDestination(){
		return new DestinationInfo("Tree", 32.648095, 34.929242, 0, 0, 0);
	}


	public void setGeoInfoListenner(GeoInfoListenner geoInfoListenner) {
		this.geoInfoListenner = geoInfoListenner;
		geoInfoListenner.handleNewLocationRecored(lastLocationRecord);
	}

	public void setWebInfoActivity(WebInfoActivity webInfoActivity) {
		this.webInfoListenner = webInfoActivity;
	}


	public void startSession(){
		Utils.log("Starting Session");

		inSession = true;

		showNotification(R.string.notificationActiveNotReady);

		settings = settingsContainer.getSettings();
		Utils.log(settings+"");

		navigationService = NavigationService.getInstance();
		updateDestination(getDefaultDestination());

		Criteria criteria = new Criteria();
		criteria.setAccuracy(settings.locationAccuracy);
		criteria.setAltitudeRequired(settings.altitudeRequired);
		criteria.setBearingRequired(settings.bearingRequired);		
		criteria.setSpeedRequired(settings.speedRequired);

		List<String> providers = locationManager.getProviders(criteria, true);
		Utils.log("Providers: " + providers);

		//start location listenning
				locationManager.requestLocationUpdates(
						settings.provider, 
						settings.minTimeInterval, 
						settings.minDistanceInterval, 
						this);

		//TODO :REMOVE TEST
//		testNavigation(); 

		serverConnector = new ServerConnector(this, webInfoListenner);
	}

	//*****************Navigation TESTER**********
	Handler handler;
	PositionSender positionSender;

	private class PositionSender implements Runnable{
		int height;

		public Location getFakeLocation(double alt, double lat, double lon,float bearing){
			Location newLocation = new Location("NavigationTest");
			newLocation.setTime(System.currentTimeMillis());
			newLocation.setLatitude(lat);
			newLocation.setLongitude(lon);
			newLocation.setAltitude(alt);
			newLocation.setBearing(bearing);
			return newLocation; 
		}

		public PositionSender(){
			height = 300;
		}

		@Override
		public void run() {
			height-=1;

			if (height==280){
				navigationService.setLastLightIndicator(true);
			}
			
			navigationService.handleRange( (int)(height*39.37) );
			
			if (height>0){
				Location newLocation = getFakeLocation(height,  32.649347, 34.925498, 90);
				onLocationChanged(newLocation);
				handler.postDelayed(this, 500);
			}
		}
	}

	private void testNavigation() {
		handler = new Handler();
		positionSender = new PositionSender();
		handler.post(positionSender);
	}
	//********************************************



	public void endSession(){		
		Utils.log("End Sampling");
		if (isGpsReady())
			notificationManager.cancel(R.string.notificationActiveReady);
		else
			notificationManager.cancel(R.string.notificationActiveNotReady);

		locationManager.removeUpdates(this);
		inSession = false;
		numOfGoodDeltas = 0;
	}

	public boolean inSession(){
		return inSession;
	}

	public LocationRecord getLastKnownLocation(){
		return lastLocationRecord;
	}

	int numOfGoodDeltas;

	public void updateGpsReady(Location location){
		long delta = location.getTime() - lastLocationRecord.getTimestamp();
		if (delta <= GOOD_DELTA_LENGTH)
			numOfGoodDeltas++;
		else
			numOfGoodDeltas = 0;		
	}

	public boolean isGpsReady(){
		return (numOfGoodDeltas > GOOD_DELTA_SEQ); // && navigationService.isUsbConnected();
	}

	@Override
	public void onLocationChanged(Location location) {
		updateGpsReady(location);
		navigationService.readyToGo(isGpsReady());
		if (!isGpsReady())
			switchNotification(R.string.notificationActiveNotReady, R.string.notificationActiveReady);

		Log.v(TAG, "Gps is Ready ? " + isGpsReady());

		Utils.log("Got new location : " + location);

		navigationService.handleNewPosition(location);

		LocationRecord newLocation = new LocationRecord(
				location.getTime(), 
				location.getAltitude(), 
				location.getLatitude(), 
				location.getLongitude(), 
				normalizeAngle(location.bearingTo(destinationLocation)), 
				location.distanceTo(destinationLocation), 
				getVerticalSpeedFromLocation(location), 
				getHorizontalSpeedFromLocation(location),
				normalizeAngle(location.getBearing()), 
				location.getSpeed(),
				navigationService.getLastLightIndicator(), 
				navigationService.getLastMsg(),  
				navigationService.getLastRange(), 
				isGpsReady(),
				isOnline(),
				navigationService.isUsbConnected());

		lastLocationRecord = newLocation; 
		serverConnector.addDate(lastLocationRecord);		

		refreshAll();		
	}

	private double getVerticalSpeedFromLocation(Location location) {
		double verticalDelta = location.getAltitude() - lastLocationRecord.getAltitude();
		double timeDelta = location.getTime() - lastLocationRecord.getTimestamp();
		return (verticalDelta/timeDelta)*1000; 
	}

	private double getHorizontalSpeedFromLocation(Location location) {
		Location lastLocation = new Location("Last");
		lastLocation.setAltitude(location.getAltitude());
		lastLocation.setLatitude(lastLocationRecord.getLatitude());
		lastLocation.setLongitude(lastLocationRecord.getLongtitude());
		double horizontalDelta = location.distanceTo(lastLocation);
		double timeDelta = location.getTime() - lastLocationRecord.getTimestamp();
		return (horizontalDelta/timeDelta)*1000; 
	}

	private float normalizeAngle(float angle){
		float normalizedAngle = angle;
		if (normalizedAngle<0) 
			normalizedAngle+=360;

		assert(normalizedAngle<=360 && normalizedAngle>=0);
		return normalizedAngle;
	}

	@Override
	public void onProviderDisabled(String provider) {
		alertMessageReciever("onProviderDisabled : " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		alertMessageReciever( "onProviderEnabled : " + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		alertMessageReciever("onStatusChanged : " + provider + ", " + extras.describeContents());
	}

	private void alertMessageReciever(String text){
		if (mainMessageReciever!=null)
			mainMessageReciever.handleTextMessage(text);
	}

	public Location getDestination() {
		return destinationLocation;
	}

	public void updateDestination(DestinationInfo destination) {
		if (navigationService!=null)
			navigationService.handleNewDestination(destination);

		this.destinationLocation.setAltitude(destination.getAltitude());
		this.destinationLocation.setLatitude(destination.getLatitude());
		this.destinationLocation.setLongitude(destination.getLongtitude());
	}

	public Settings getSettings(){
		return settings;
	}

	public ServerConnector getServerConnector(){
		return serverConnector;
	}

	public void refreshAll(){
		if (geoInfoListenner!=null)
			geoInfoListenner.handleNewLocationRecored(lastLocationRecord);
		if (gpsSafeListenner!=null)
			gpsSafeListenner.handleGPSSafe(isGpsReady());
	}

	private boolean isOnline(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info!=null && info.isConnected());
	}

	public void setGpsSafeListenner(GPSSafeListenner gpsSafeListenner) {
		this.gpsSafeListenner = gpsSafeListenner;
	}

	public void setMainMessageReciever(TextMessageReciever mainMessageReciever) {
		this.mainMessageReciever = mainMessageReciever;
	}

	public void setSettingsContainer(SettingsContainer settingsContainer) {
		this.settingsContainer = settingsContainer;
	}
}
