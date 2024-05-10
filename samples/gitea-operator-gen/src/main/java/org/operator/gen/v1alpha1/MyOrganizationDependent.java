package org.operator.gen.v1alpha1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.kiota.ApiException;
import com.microsoft.kiota.serialization.KiotaJsonSerialization;
import com.microsoft.kiota.serialization.Parsable;
import com.microsoft.kiota.serialization.ParsableFactory;
import com.microsoft.kiota.serialization.ParseNode;

import io.apisdk.gitea.json.ApiClient;
import io.apisdk.gitea.json.models.CreateOrgOption;
import io.apisdk.gitea.json.models.EditOrgOption;
import io.apisdk.gitea.json.models.Organization;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Deleter;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.Updater;
import io.javaoperatorsdk.operator.processing.dependent.external.PerResourcePollingDependentResource;
import io.kiota.http.vertx.VertXRequestAdapter;
import io.kiota.serialization.json.JsonParseNodeFactory;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientSession;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class MyOrganizationDependent extends PerResourcePollingDependentResource<Organization, org.operator.gen.v1alpha1.Organization> 
implements Creator<Organization, org.operator.gen.v1alpha1.Organization>, Updater<Organization, org.operator.gen.v1alpha1.Organization>, Deleter<org.operator.gen.v1alpha1.Organization> {

	private static final Logger LOG = LoggerFactory.getLogger(MyOrganizationDependent.class);
	
	@Inject
    Vertx vertx;
	@Inject
	HeaderAuthentication auth;
	@Inject
	BaseUrlProvider urlProvider;
	
	private ApiClient apiClient;
	
	@PostConstruct
	public void initOauth() {
		WebClientSession webClientSession = WebClientSession.create(WebClient.create(vertx));
		auth.addAuthHeaders(webClientSession);
		VertXRequestAdapter requestAdapter = new VertXRequestAdapter(webClientSession);
		urlProvider.provide(requestAdapter);
		apiClient = new ApiClient(requestAdapter);
	}
	
	public MyOrganizationDependent() {
		super(Organization.class);
	}
	

	@Override
	protected Organization desired(org.operator.gen.v1alpha1.Organization primary,
			Context<org.operator.gen.v1alpha1.Organization> context) {
		return fromResource(primary, Organization::createFromDiscriminatorValue);
	}
	
	private <T extends Parsable> T fromResource(org.operator.gen.v1alpha1.Organization primary, ParsableFactory<T> parsableFactory) {
		String asJson = Serialization.asJson(primary);
		ParseNode parseNode = new JsonParseNodeFactory().getParseNode("application/json", new ByteArrayInputStream(asJson.getBytes(StandardCharsets.UTF_8)));
		return parseNode.getChildNode("spec").getObjectValue(parsableFactory);
	}
	
	@Override
	public Set<Organization> fetchResources(org.operator.gen.v1alpha1.Organization primaryResource) {
		
		try {
			LOG.info("Fetching org with name {}", primaryResource.getMetadata().getName());
			return Set.of(apiClient.orgs().byOrg(primaryResource.getMetadata().getName()).get());
		} catch (ApiException e) {
			return Collections.emptySet();
		}
		//https://github.com/kiota-community/kiota-java-extra?tab=readme-ov-file#serialization-jackson
	}

	@Override
	public Organization update(Organization actual, Organization desired, org.operator.gen.v1alpha1.Organization primary,
			Context<org.operator.gen.v1alpha1.Organization> context) {
		EditOrgOption editOrgOption = fromResource(primary, EditOrgOption::createFromDiscriminatorValue);
		try {
			LOG.info(KiotaJsonSerialization.serializeAsString(editOrgOption));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			return apiClient.orgs().byOrg(primary.getMetadata().getName()).patch(editOrgOption);
		} catch (ApiException e) {
			LOG.error("Error updating resource", e);
			throw e;
		}
	}

	@Override
	public Organization create(Organization desired, org.operator.gen.v1alpha1.Organization primary,
			Context<org.operator.gen.v1alpha1.Organization> context) {
		
		CreateOrgOption createOrgOption = fromResource(primary, CreateOrgOption::createFromDiscriminatorValue);
		try {
			LOG.info(KiotaJsonSerialization.serializeAsString(createOrgOption));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			return apiClient.orgs()
				.post(createOrgOption);
		} catch (ApiException e) {
			LOG.error("Error creating resource", e);
			
			throw e;
		}
	}

	@Override
	public void delete(org.operator.gen.v1alpha1.Organization primary,
			Context<org.operator.gen.v1alpha1.Organization> context) {
		LOG.info("Deleting {}", primary.getMetadata().getName());
		try {
			apiClient.orgs().byOrg(primary.getMetadata().getName()).delete();
			LOG.info("Done");
		} catch (ApiException e) {
			LOG.error("Error deleting resource", e);
			
			throw e;
		}
	}

}
