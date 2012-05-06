package org.paradroid.common;

import java.io.Serializable;
import java.util.ArrayList;

public class RecordsContainer implements Serializable{

	private static final long serialVersionUID = -5391604492337144283L;
	private long timestamp;
	
	ArrayList<LocationRecord> records;

	public RecordsContainer(long timestamp) {
		super();
		this.timestamp = timestamp;
		this.records = new ArrayList<LocationRecord>();
	}

	public RecordsContainer(long timestamp, ArrayList<LocationRecord> records) {
		super();
		this.timestamp = timestamp;
		this.records = records;
	}

	public ArrayList<LocationRecord> getRecords() {
		return records;
	}

	public void setRecords(ArrayList<LocationRecord> records) {
		this.records = records;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	
	
}
