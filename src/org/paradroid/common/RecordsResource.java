package org.paradroid.common;

import org.restlet.resource.Get;
import org.restlet.resource.Post;

public interface RecordsResource {
	@Post
	public void addRecords(RecordsContainer records);
	
	@Get
	public RecordsContainer getRecords();
	
}
