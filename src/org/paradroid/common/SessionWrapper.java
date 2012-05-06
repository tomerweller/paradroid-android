package org.paradroid.common;

import java.io.Serializable;

public class SessionWrapper implements Serializable{
	
	private static final long serialVersionUID = 3457136853443324960L;

	private long sessionId;
	private DestinationInfo destination;
	
	public SessionWrapper() {
		super();
	}

	public SessionWrapper(long sessionId, DestinationInfo destination) {
		super();
		this.sessionId = sessionId;
		this.destination = destination;
	}

	public long getSessionId() {
		return sessionId;
	}

	public DestinationInfo getDestination() {
		return destination;
	} 
	
	
	
	
	
	
}
