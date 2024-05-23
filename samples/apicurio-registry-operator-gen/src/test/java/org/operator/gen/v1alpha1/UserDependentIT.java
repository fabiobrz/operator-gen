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
public class UserDependentIT {
	
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
    	while (!client.resources(User.class).inNamespace(namespace).list().getItems().isEmpty()) {
    		client.resources(User.class).inNamespace(namespace).delete();
    		System.out.println("Deleted users");
    	}
	}
	
	@Test
    void create() {
		User user = newUser("create");
		client.resource(user).create();
		await().ignoreException(ApiException.class).atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
			io.apisdk.gitea.json.models.User resource = apiClient.users().byUsername(user.getMetadata().getName()).get();
			assertNotNull(resource);
			assertUserEquals(user, resource);
        });
    }
	
	@Test
    void delete() {
		User user = newUser("delete");
		client.resource(user).create();
		client.resource(user).waitUntilReady(10, TimeUnit.SECONDS);
		client.resource(user).delete();
		await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
			try {
				io.apisdk.gitea.json.models.User resUser = apiClient.users().byUsername(user.getMetadata().getName()).get();
				System.out.println(resUser);
				fail("Api Exception expected");
			} catch (ApiException e) {
				assertEquals(404, e.getResponseStatusCode());
			}
			
        });
    }
	
	
	@Test
    void update() {
		User user = newUser("update");
		client.resource(user).create();
		String updateDescription = "update1";
		String updateLocation = "update2";
		client.resource(user).edit(o -> {
	       	user.getSpec().setDescription(updateDescription);
	        user.getSpec().setLocation(updateLocation);
	        return user;
		});
		await().ignoreException(ApiException.class).atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
			io.apisdk.gitea.json.models.User resource = apiClient.users().byUsername(user.getMetadata().getName()).get();
			assertNotNull(resource);
			assertEquals(updateDescription, resource.getDescription());
			assertEquals(updateLocation, resource.getLocation());
        });
    }

	private User newUser(String name) {
		User user = new User();
		user.setMetadata(new ObjectMetaBuilder().withName(name).withNamespace(namespace).build());
		UserSpec spec = new UserSpec();
		spec.setDescription(name);
		spec.setPassword("Test124125");
		spec.setEmail("test@example.org");
		spec.setVisibility("public");
		spec.setFull_name(name);
		spec.setLogin_name(name);
		spec.setLocation("testloc");
		spec.setUsername(name);
		spec.setMust_change_password(false);
		user.setSpec(spec);
		return user;
	}
	
	private void assertUserEquals(User userA, io.apisdk.gitea.json.models.User user) {
		Assertions.assertEquals(userA.getSpec().getDescription(), user.getDescription());
		Assertions.assertEquals(userA.getSpec().getEmail(), user.getEmail());
		Assertions.assertEquals(userA.getSpec().getVisibility(), user.getVisibility());
		Assertions.assertEquals(userA.getSpec().getFull_name(), user.getFullName());
		Assertions.assertEquals(userA.getSpec().getLogin_name(), user.getLoginName());
		Assertions.assertEquals(userA.getSpec().getLocation(), user.getLocation());
	}
}
