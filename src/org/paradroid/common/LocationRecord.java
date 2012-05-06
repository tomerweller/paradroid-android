package org.paradroid.common;

import java.io.Serializable;

public class LocationRecord implements Serializable{
	
	private static final long serialVersionUID = -6183527739612208413L;

	private long timestamp;
	
	private double altitude;
	private double latitude;
	private double longtitude;
	
	private float bearingToDestination;
	private float distance; 

	private boolean isOnline;
	
	private float currentBearing;
	private float currentSpeed;
	
	private double horizontalSpeed;
	private double verticalSpeed; 
	
	private boolean chuteOpen; 	
	private String lastAdkMessage; 
	private double lastRange;
	private boolean gpsReady;
	private boolean adkConnected; 
	

	public LocationRecord() {
		this(1l, 0, 0, 0, 0, 0, 0, 0, 0, 0, false, "", 0, false, false, false);
	}

	public LocationRecord(
			long timestamp, 
			double altitude, 
			double latitude,
			double longtitude, 
			float bearingToDestination, 
			float distance,
			double horizontalSpeed, 
			double verticalSpeed, 
			float currentBearing,
			float currentSpeed, 
			boolean chuteOpen, 
			String lastAdkMessage,
			double lastRange, 
			boolean gpsReady, 
			boolean isOnline, 
			boolean adkConnected) {
		super();
		this.timestamp = timestamp;
		this.altitude = altitude;
		this.latitude = latitude;
		this.longtitude = longtitude;
		this.bearingToDestination = bearingToDestination;
		this.distance = distance;
		this.horizontalSpeed = horizontalSpeed;
		this.verticalSpeed = verticalSpeed;
		this.currentBearing = currentBearing;
		this.currentSpeed = currentSpeed;
		this.chuteOpen = chuteOpen;
		this.lastAdkMessage = lastAdkMessage;
		this.lastRange = lastRange;
		this.gpsReady = gpsReady;
		this.isOnline = isOnline;
		this.adkConnected = adkConnected; 
	}

	public long getTimestamp() {
		return timestamp;
	}

	public double getAltitude() {
		return altitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongtitude() {
		return longtitude;
	}

	public float getBearingToDestination() {
		return bearingToDestination;
	}

	public float getDistance() {
		return distance;
	}

	public double getHorizontalSpeed() {
		return horizontalSpeed;
	}

	public double getVerticalSpeed() {
		return verticalSpeed;
	}

	public float getCurrentBearing() {
		return currentBearing;
	}

	public float getCurrentSpeed() {
		return currentSpeed;
	}

	public boolean isChuteOpen() {
		return chuteOpen;
	}

	public String getLastAdkMessage() {
		return lastAdkMessage;
	}

	public double getLastRange() {
		return lastRange;
	}

	public boolean isGpsReady() {
		return gpsReady;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public boolean isAdkConnected() {
		return adkConnected;
	}




}
