package org.acme;

import java.util.List;

import org.eclipse.microprofile.config.Config;

public class Configuration {
	
	private final Config config;
	
	public Configuration(Config config) {
		this.config = config;
	}

	public String getCrdVersion() {
		return config.getValue("quarkus.operator-sdk.operator-gen.crd.version", String.class);
	}
	
	public String getCrdPackage() {
		return config.getValue("quarkus.operator-sdk.operator-gen.crd.package", String.class);
	}
	
	public List<String> getResponses() {
		return config.getValues("quarkus.operator-sdk.operator-gen.api.responses", String.class);
	}

	public Config getConfig() {
		return config;
	}

}
