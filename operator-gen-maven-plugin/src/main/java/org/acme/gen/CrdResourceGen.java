package org.acme.gen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.expr.Name;

import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionVersionBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.v1.JSONSchemaPropsBuilder;
import io.fabric8.kubernetes.client.utils.Serialization;


public class CrdResourceGen {

	private final Path path;
	private final Path openApiJson;
	private final Name name;

	public CrdResourceGen(Path path, Path openApiJson, Name name) {
		super();
		this.path = path;
		this.openApiJson = openApiJson;
		this.name = name;
	}

	public void create() {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNodeTree;
		try {
			jsonNodeTree = objectMapper.readTree(Files.newInputStream(openApiJson));
			JsonNode schema = jsonNodeTree.get("components").get("schemas").get("Organization");

			JSONSchemaPropsBuilder specBuilder = new JSONSchemaPropsBuilder().withType("object");
			schema.get("properties").fields().forEachRemaining(f -> specBuilder.addToProperties(f.getKey(),
					new JSONSchemaPropsBuilder().withType(f.getValue().get("type").asText()).build()));
			CustomResourceDefinitionBuilder defBuilder = new CustomResourceDefinitionBuilder();
			CustomResourceDefinition customResourceDefinition = defBuilder.editOrNewMetadata()
					.withName(name.getIdentifier()).endMetadata().editOrNewSpec()
					.withGroup(name.getQualifier().map(Name::toString).map(this::reverseQualifier).orElse("opgen.io"))
					.withNewNames().withKind(name.getIdentifier()).endNames().withScope("Namespaced")
					.withVersions(new CustomResourceDefinitionVersionBuilder().withName("v1alpha1").withServed(true)
							.withStorage(true).withNewSchema().editOrNewOpenAPIV3Schema()
							.addToProperties("status", new JSONSchemaPropsBuilder().withType("object").build())
							.addToProperties("spec", specBuilder.build()).endOpenAPIV3Schema().and().build())
					.endSpec().build();
			Files.write(path, Serialization.asYaml(customResourceDefinition).getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private String reverseQualifier(String qualifier) {
		return Arrays.asList(qualifier.split("\\.")).reversed().stream().collect(Collectors.joining("."));
	}

}
