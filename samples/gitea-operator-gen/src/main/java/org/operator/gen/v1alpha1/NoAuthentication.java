package org.operator.gen.v1alpha1;

import io.quarkus.arc.DefaultBean;
import io.vertx.ext.web.client.WebClientSession;
import jakarta.enterprise.context.Dependent;

@DefaultBean
@Dependent
public class NoAuthentication implements HeaderAuthentication{

	@Override
	public void addAuthHeaders(WebClientSession session) {
		// No op
	}

}
