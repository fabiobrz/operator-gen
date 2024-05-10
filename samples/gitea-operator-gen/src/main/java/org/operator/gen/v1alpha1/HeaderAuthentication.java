package org.operator.gen.v1alpha1;

import io.vertx.ext.web.client.WebClientSession;

public interface HeaderAuthentication {
	
	public void addAuthHeaders(WebClientSession session); 
}
