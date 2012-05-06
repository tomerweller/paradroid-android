package org.paradroid.common;

import java.io.Serializable;

public class Settings implements Serializable{
	
	private static final long serialVersionUID = -7500896296094606073L;

	public String diverName; 
	public String provider; 
	public float minDistanceInterval;
	public long minTimeInterval; 
	public int locationAccuracy;
	public boolean altitudeRequired; 
	public int altitudeAccuracy;
	public boolean bearingRequired; 
	public int bearingAccuracy;
	public boolean speedRequired;
	public int speedAccuracy;
	
	public Settings() {
		super();
	}

	@Override
	public String toString() {
		return "Settings [diverName=" + diverName + ", provider=" + provider
				+ ", minDistanceInterval=" + minDistanceInterval
				+ ", minTimeInterval=" + minTimeInterval
				+ ", locationAccuracy=" + locationAccuracy
				+ ", altitudeRequired=" + altitudeRequired
				+ ", altitudeAccuracy=" + altitudeAccuracy
				+ ", bearingRequired=" + bearingRequired + ", bearingAccuracy="
				+ bearingAccuracy + ", speedRequired=" + speedRequired
				+ ", speedAccuracy=" + speedAccuracy + "]";
	}



	
	
	
	
}
