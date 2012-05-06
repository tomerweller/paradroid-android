package org.paradroid.adk;

import org.paradroid.common.DestinationInfo;

import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class NavigationService extends ADKService {

	public enum Mode {SLEEP, WAKE_UP, NAVIGATION, ALIGN_AGAINST_WIND, PREPARE_FLARE, FLARE };

	private static String TAG = NavigationService.class.getSimpleName();

	private static final int ALIGN_AGAINST_WIND_ALTITUDE = 80; 

	private static final int WAKE_UP_TIME = 4000; //ms
	private static final int PREPARE_FLARE_RANGE = 240; //inches
	private static final int FLARE_RANGE = 140; 			//inches
	private static final int GROUND_RANGE = 20;

	private static final float BEARING_MARGIN = 30; //degree

	static NavigationService instance = null;

	private Handler handler; 
	private int lastRange; 
	private String lastMsg; 
	private boolean lastLightIndicator;
	private Mode mode;
	private NavigationListenner listenner;
	private boolean usbConnected;
	private DestinationInfo destinationInfo;
	private Location destinationLocation;

	public static NavigationService getInstance() {
		return instance; 
	}

	@Override
	public void onCreate(){
		super.onCreate();
		destinationInfo = null;
		destinationLocation = null; 
		handler = new Handler(); 
		mode = Mode.SLEEP;
		instance = this;
		listenner = null;
		lastRange = 500;
		lastMsg = "none";
		lastLightIndicator = false;
		usbConnected = false;
	}

	public class NavigationServiceBinder extends Binder{
		public NavigationService getService(){
			return NavigationService.this; 
		}
	}
	private final IBinder binder = new NavigationServiceBinder();

	@Override
	public IBinder onBind(Intent intent){
		return binder; 
	}

	public boolean isUsbConnected() {
		return usbConnected;
	}

	public void registerListenner(NavigationListenner listenner){
		this.listenner = listenner;
		listenner.handleNewRangeFinderDistance(lastRange);
		listenner.changeStatus(lastMsg);
		listenner.handleLight(lastLightIndicator);
		listenner.onUsbConnected(usbConnected);
	}

	@Override
	protected void onUsbConncected(boolean enable) {
		usbConnected = enable; 
		if (listenner!=null)
			listenner.onUsbConnected(enable);
	}

	private class ModeChanger implements Runnable{
		Mode mode; 
		ModeChanger(Mode mode){
			this.mode = mode; 
		}

		@Override
		public void run() {
			setMode(this.mode);
		}	
	}

	@Override
	protected void handleMsg(byte rangeMsg, byte lightMsg) {
		Log.v(TAG, "Got Message with range " + rangeMsg + "and light " + lightMsg);
		int range = rangeMsg+128;
		handleRange(range);
		setLastLightIndicator(lightMsg==1);
	}

	public void handleRange(int range) {
		lastRange = range; 		

		if (listenner!=null)
			listenner.handleNewRangeFinderDistance(lastRange);

		if (mode==Mode.NAVIGATION || mode==Mode.WAKE_UP || mode==Mode.SLEEP)
			return;

		if (range>PREPARE_FLARE_RANGE){
			mode = Mode.ALIGN_AGAINST_WIND;
		} else if (range>FLARE_RANGE){
			mode = Mode.PREPARE_FLARE; 
//			resetToBottom(); :TODO: use 			
		} else if (range>GROUND_RANGE){
			mode = Mode.FLARE;
		} else {
//			resetToTop(); TODO: use
		}
	}

	public int getLastRange(){
		return lastRange;
	}

	public void setLastLightIndicator(boolean lightIndicator){
		this.lastLightIndicator = lightIndicator; 

		if (lastLightIndicator && mode==Mode.SLEEP){
			setMode(Mode.WAKE_UP);				
			resetToTop();
			handler.postDelayed(new ModeChanger(Mode.NAVIGATION), WAKE_UP_TIME);
		}

		if (listenner!=null)
			listenner.handleLight(lastLightIndicator);
	}		


	public boolean getLastLightIndicator(){
		return lastLightIndicator;
	}

	private void handleBearing(float currentBearing, float requiredBearing){

		float delta = requiredBearing - currentBearing;

		if (delta<0) delta+=360;		
		if (delta<BEARING_MARGIN || delta>(360-BEARING_MARGIN)){
			changeStatus("Static");
			//do nothing
		}
		else if (delta<180){
			pullRight();
		}
		else if (delta>180){
			pullLeft();
		}
	}

	public void handleNewPosition(Location position){
		switch (mode){
		case NAVIGATION:
			if (position.getAltitude() < ALIGN_AGAINST_WIND_ALTITUDE){	
				setMode(Mode.ALIGN_AGAINST_WIND);
			}
			handleBearing(position.getBearing(), position.bearingTo(destinationLocation));
			break;

		case ALIGN_AGAINST_WIND:
			//TODO: Change			
			//handleBearing(position.getBearing(), getAntiWindDirection());
			handleBearing(position.getBearing(), position.bearingTo(destinationLocation));
			
			break;

		default: 
			break; 
		}
	}

	private float getAntiWindDirection(){
		float antiWindDirection = destinationInfo.getWindBearing()-180;
		return antiWindDirection < 0 ? antiWindDirection + 360 : antiWindDirection; 
	}

	public void handleNewDestination(DestinationInfo destinationInfo){
		this.destinationInfo = destinationInfo;
		this.destinationLocation = new Location("Destination");
		destinationLocation.setAltitude(destinationInfo.getAltitude());
		destinationLocation.setLatitude(destinationInfo.getLatitude());
		destinationLocation.setLongitude(destinationInfo.getLongtitude());
	}

	public void pullRight(){
		sendCommand(ADKConsts.PULL_RIGHT_MSG, 1);
		changeStatus("Right");
	}

	public void pullLeft(){
		sendCommand(ADKConsts.PULL_LEFT_MSG, 1);
		changeStatus("Left");
	}

	public void flare() {
		sendCommand(ADKConsts.FLARE_MSG, 1);
		changeStatus("Flare");
	}	

	public void permFlare() {
		sendCommand(ADKConsts.RESET_MSG, 1);
		changeStatus("Perm Flare");
	}	

	public void resetToTop(){
		sendCommand(ADKConsts.RESET_MSG, 0);
		changeStatus("Reset");
	}

	public void resetToBottom(){
		sendCommand(ADKConsts.RESET_MSG, 1);
		changeStatus("Reset");
	}

	public void readyToGo(boolean ready){
		sendCommand(ADKConsts.READY_TO_DROP_MSG, ready ? 1 : 0);
		changeStatus("Ready " + ready);
	}

	private void changeStatus(String textMsg){
		lastMsg = textMsg;
		if (listenner!=null)
			listenner.changeStatus(textMsg);
	}

	public String getLastMsg(){
		return getMode() + " " + lastMsg; 
	}

	public void setMode(Mode mode){
		Mode oldMode = this.mode; 
		this.mode = mode; 
		Log.v(TAG, "Change mode from " + oldMode + " to " + mode); 
	}

	public Mode getMode(){
		return mode;
	}
}
