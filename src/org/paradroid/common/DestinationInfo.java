package org.paradroid.common;

import java.io.Serializable;

public class DestinationInfo implements Serializable{

	private static final long serialVersionUID = 7373394440593911959L;
	
	private String name; 
	private double latitude; 
	private double longtitude;
	private double altitude;
	
	private float windSpeed;
	private float windBearing;
	
	public DestinationInfo(String name, double latitude, double longtitude,
			double altitude, float windSpeed, float windBearing) {
		super();
		this.name = name;
		this.latitude = latitude;
		this.longtitude = longtitude;
		this.altitude = altitude;
		this.windSpeed = windSpeed;
		this.windBearing = windBearing;
	}

	public String getName() {
		return name;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongtitude() {
		return longtitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public float getWindSpeed() {
		return windSpeed;
	}

	public float getWindBearing() {
		return windBearing;
	}

	@Override
	public String toString() {
		return "DestinationInfo [name=" + name + ", latitude=" + latitude
				+ ", longtitude=" + longtitude + ", altitude=" + altitude
				+ ", windSpeed=" + windSpeed + ", windBearing=" + windBearing
				+ "]";
	}
	
}
