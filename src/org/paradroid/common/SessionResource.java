package org.paradroid.common;

import org.restlet.resource.Post;

public interface SessionResource {
	@Post
	SessionWrapper startSession(Settings diveSettings);
}
