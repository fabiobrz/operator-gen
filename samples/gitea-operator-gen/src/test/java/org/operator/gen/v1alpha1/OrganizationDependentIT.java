package org.operator.gen.v1alpha1;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.microsoft.kiota.ApiException;

import io.apisdk.gitea.json.ApiClient;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import io.kiota.http.vertx.VertXRequestAdapter;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientSession;
import jakarta.inject.Inject;

@QuarkusTest
public class OrganizationDependentIT {
	
	static OpenShiftClient client = new KubernetesClientBuilder().build().adapt(OpenShiftClient.class);
	
	private ApiClient apiClient;
	@Inject
	Vertx vertx;
	@Inject
	HeaderAuthentication auth;
	@ConfigProperty(name = "gitea.api.uri")
	String giteaApiUri;
	@ConfigProperty(name = "test.kubernetes.namespace")
	String namespace;
	
	@BeforeAll
	static void beforeAllTests() {
		//Deploy Gitea
	}

    @AfterAll
	static void afterAllTests() {
    	//Undeploy Gitea
	}
    
    @BeforeEach
   	void setUp() {
    	WebClient webClient = WebClient.create(vertx);
		WebClientSession webClientSession = WebClientSession.create(webClient);
		auth.addAuthHeaders(webClientSession);
		VertXRequestAdapter requestAdapter = new VertXRequestAdapter(webClientSession);
		requestAdapter.setBaseUrl(giteaApiUri);
		apiClient = new ApiClient(requestAdapter);
   	}
    
    @AfterEach
	void tearDown() {
    	while (!client.resources(Organization.class).inNamespace(namespace).list().getItems().isEmpty()) {
    		client.resources(Organization.class).inNamespace(namespace).delete();
    		System.out.println("Deleted orgs");
    	}
	}
	
	@Test
    void create() {
		Organization org = newOrg("create");
		client.resource(org).create();
		await().ignoreException(ApiException.class).atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
			io.apisdk.gitea.json.models.Organization resource = apiClient.orgs().byOrg(org.getMetadata().getName()).get();
			assertNotNull(resource);
			assertOrgEquals(org, resource);
        });
    }
	
	@Test
    void delete() {
		Organization org = newOrg("delete");
		client.resource(org).create();
		client.resource(org).waitUntilReady(10, TimeUnit.SECONDS);
		client.resource(org).delete();
		await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
			try {
				io.apisdk.gitea.json.models.Organization organization = apiClient.orgs().byOrg(org.getMetadata().getName()).get();
				System.out.println(organization);
				fail("Api Exception expected");
			} catch (ApiException e) {
				assertEquals(404, e.getResponseStatusCode());
			}
			
        });
    }
	
	
	@Test
    void update() {
		Organization org = newOrg("update");
		client.resource(org).create();
		String updateDescription = "update1";
		String updateLocation = "update2";
		String updateWebsite = "https://udpated.example.com";
		client.resource(org).edit(o -> {
	       	org.getSpec().setDescription(updateDescription);
	        org.getSpec().setLocation(updateLocation);
	        org.getSpec().setWebsite(updateWebsite);
	        return org;
		});
		await().ignoreException(ApiException.class).atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
			io.apisdk.gitea.json.models.Organization resource = apiClient.orgs().byOrg(org.getMetadata().getName()).get();
			assertNotNull(resource);
			assertEquals(updateDescription, resource.getDescription());
			assertEquals(updateLocation, resource.getLocation());
			assertEquals(updateWebsite, resource.getWebsite());
        });
    }

	private Organization newOrg(String name) {
		Organization org = new Organization();
		org.setMetadata(new ObjectMetaBuilder().withName(name).withNamespace(namespace).build());
		OrganizationSpec spec = new OrganizationSpec();
		spec.setDescription(name);
		spec.setEmail("test@example.org");
		spec.setFull_name(name);
		spec.setLocation(name);
		spec.setUsername(name);
		spec.setVisibility("public");
		spec.setWebsite("https://example.org");
		org.setSpec(spec);
		return org;
	}
	
	private void assertOrgEquals(Organization orgA, io.apisdk.gitea.json.models.Organization orgB) {
		Assertions.assertEquals(orgA.getSpec().getDescription(), orgB.getDescription());
		Assertions.assertEquals(orgA.getSpec().getEmail(), orgB.getEmail());
		Assertions.assertEquals(orgA.getSpec().getFull_name(), orgB.getFullName());
		Assertions.assertEquals(orgA.getSpec().getLocation(), orgB.getLocation());
		Assertions.assertEquals(orgA.getSpec().getUsername(), orgB.getUsername());
		Assertions.assertEquals(orgA.getSpec().getVisibility(), orgB.getVisibility());
		Assertions.assertEquals(orgA.getSpec().getWebsite(), orgB.getWebsite());
	}
}
