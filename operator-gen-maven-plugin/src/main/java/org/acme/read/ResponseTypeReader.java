package org.acme.read;

import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;

public class ResponseTypeReader {
	
	private final OpenAPI api;

	public ResponseTypeReader(OpenAPI api) {
		super();
		this.api = api;
	}
	
	public Stream<String> getResponseTypeNames() {
		return getResponseTypeNames(e -> true);
	}
	
	public Stream<String> getResponseTypeNames(Predicate<Entry<String, APIResponse>> filter) {
		return api.getComponents().getResponses().entrySet().stream().filter(filter).map(Entry::getKey);
	}
	
}
