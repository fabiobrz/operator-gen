package org.operator.gen.v1alpha1;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.vertx.ext.web.client.WebClientSession;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TokenAuthentication implements HeaderAuthentication{

	@ConfigProperty(name = "gitea.api.token")
	private String token;
	
	@Override
	public void addAuthHeaders(WebClientSession session) {
		session.addHeader("Authorization", "token " + token);
	}

}
