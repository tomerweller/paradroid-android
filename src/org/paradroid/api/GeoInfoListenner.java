package org.paradroid.api;

import org.paradroid.common.LocationRecord;

public interface GeoInfoListenner {
	public void handleNewLocationRecored(LocationRecord record);
}
