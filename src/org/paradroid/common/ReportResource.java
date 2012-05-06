package org.paradroid.common;

import org.restlet.resource.Post;

public interface ReportResource {
	@Post
	void report(RecordsContainer records);
}
