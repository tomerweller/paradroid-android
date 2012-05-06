package org.paradroid;

import java.util.ArrayList;
import java.util.Date;

import org.paradroid.api.WebInfoListenner;
import org.paradroid.common.LocationRecord;
import org.paradroid.common.RecordsContainer;
import org.paradroid.common.RecordsResource;
import org.paradroid.common.SessionResource;
import org.paradroid.common.SessionWrapper;
import org.paradroid.common.Settings;
import org.paradroid.common.DestinationInfo;
import org.restlet.resource.ClientResource;


import android.util.Log;

public class ServerConnector {

	private ArrayList<LocationRecord> dataQueue; 
	private boolean isCommunicating;
	private Long sessionId;
	private DelegatorService delegator;
	private Settings diveSettings;
	private int totalSent;
	private int totalRecieved;
	private Date lastReportOn;

	private class FlushThread extends Thread{
		
		private ServerConnector recordSender;
		
		FlushThread(ServerConnector recordSender){
			this.recordSender = recordSender;
		}

		@Override
		public void run(){
			recordSender.flushData();
		}
	}

	public ServerConnector(DelegatorService delegator, WebInfoListenner webInfoListenner) {
		super();
		totalRecieved = 0;
		totalSent = 0;
		lastReportOn = null;
		
		dataQueue = new ArrayList<LocationRecord>();
		
		isCommunicating = false; 
		sessionId = null;
		
		this.delegator = delegator;
		diveSettings = delegator.getSettings();
	}

	private void log(String info){
		Utils.log("REST :" + info);
	}

	private String getBaseRestURI(){
		return "http://6.android-paradroid.appspot.com/rest";
	}
	
	private String getSessionResourceAddress(){
		return getBaseRestURI() + "/session";
	}

	private String getReportResourceAddress(){
		return getBaseRestURI() + "/records/" + sessionId;
	}

	private void getSessionID() {
		Utils.log("getSessionID()");
		log("getSessionID()");
		try{
			ClientResource cr = new ClientResource(getSessionResourceAddress());
			cr.setEntityBuffering(true);
			SessionResource resource = cr.wrap(SessionResource.class);
			SessionWrapper session = resource.startSession(diveSettings);
			sessionId = session.getSessionId();
			DestinationInfo destination = session.getDestination();
			delegator.updateDestination(destination);
			log("Got session id : " + sessionId + " and destination : " + destination);
			
		} catch (Exception e){
			log("Can't get session :" + e.getMessage());
		}
	}

	public void addDate(LocationRecord record){
		totalRecieved++;
		dataQueue.add(record);
		if (!isCommunicating)
			new FlushThread(this).start();
		if (delegator.getWebInfoListenner()!=null)
			delegator.getWebInfoListenner().handleInfoSent();
	}

	private void flushData(){
		isCommunicating = true;
		if (sessionId==null){
			getSessionID();
			if (sessionId==null){
				isCommunicating = false;
				return;
			}
		}

		//replace current queue with new
		RecordsContainer recordsContainer = new RecordsContainer(System.currentTimeMillis(), dataQueue);		
		dataQueue = new ArrayList<LocationRecord>();
		
		Log.d("RestWriter", "flushData() with " + recordsContainer.getRecords().size() + " records");
		log("flushData() with " + recordsContainer.getRecords().size() + " records");
		try{
			ClientResource cr = new ClientResource(getReportResourceAddress());
			cr.setEntityBuffering(true);
			RecordsResource resource = cr.wrap(RecordsResource.class);
			resource.addRecords(recordsContainer);			
			log("Reported " + recordsContainer.getRecords().size() + " records");
			totalSent+=recordsContainer.getRecords().size();
			lastReportOn = new Date();
		} catch (Exception e){
			log("Can't report :" + e.getMessage());
			log("Rolling back...");
			dataQueue.addAll(recordsContainer.getRecords());
		}
		isCommunicating = false;

	}

	public int getTotalSent() {
		return totalSent;
	}

	public int getTotalRecieved() {
		return totalRecieved;
	}

	public Date getLastReportOn() {
		return lastReportOn;
	}
	
	
}
