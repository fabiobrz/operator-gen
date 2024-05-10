package org.acme.gen;

import java.nio.file.Path;
import java.util.Set;

import org.acme.client.ApiClientMethodCallFactory;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.utils.SourceRoot;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.external.PerResourcePollingDependentResource;
import io.kiota.http.vertx.VertXRequestAdapter;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientSession;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class DependentGen {
	private static final String FIELD_AUTHENTICATION = "authentication";
	private static final String VAR_WEB_CLIENT_SESSION = "webClientSession";
	private static final String FIELD_API_CLIENT = "apiClient";
	private static final String FIELD_NAME_VERTX = "vertx";
	private static final String NAME_POSTFIX = "Dependent";
	private final Path path;
	private final Name name;
	private final Name resource;
	private final ApiClientMethodCallFactory methodCalls;

	public DependentGen(Path path, Name name, Name resource, ApiClientMethodCallFactory methodCalls) {
		this.path = path;
		this.name = name;
		this.resource = resource;
		this.methodCalls = methodCalls;
	}

	public void create() {
		String className = name.getIdentifier() + NAME_POSTFIX;
		var cu = new CompilationUnit(name.getQualifier().map(Name::toString).orElse(""));
		cu.addImport(PerResourcePollingDependentResource.class);
		cu.addImport(Set.class);
		cu.addImport(resource.toString());
		cu.addImport(Vertx.class);
		cu.addImport(PostConstruct.class);
		cu.addImport(WebClientSession.class);
		cu.addImport(WebClient.class);
		cu.addImport(VertXRequestAdapter.class);
		cu.addImport(Inject.class);
		cu.addImport(Creator.class);
		cu.addImport(Context.class);
		cu.addImport(resource.getQualifier().map(Name::toString).orElse("").replace(".models", "") + ".ApiClient");
		ClassOrInterfaceType crdType = new ClassOrInterfaceType(null, name.toString());
		ClassOrInterfaceType resourceType = new ClassOrInterfaceType(null, name.getIdentifier());
		ClassOrInterfaceType dependentType = new ClassOrInterfaceType(null,
				new SimpleName(PerResourcePollingDependentResource.class.getSimpleName()),
				new NodeList<>(resourceType, crdType));
		
		ClassOrInterfaceType creatorType = new ClassOrInterfaceType(null,
				new SimpleName(Creator.class.getSimpleName()),
				new NodeList<>(resourceType, crdType));
		
		
		ClassOrInterfaceType setOfResourceType = new ClassOrInterfaceType(null,
				new SimpleName(Set.class.getSimpleName()), new NodeList<>(resourceType));
		ClassOrInterfaceType setType = new ClassOrInterfaceType(null, Set.class.getSimpleName());

		ClassOrInterfaceDeclaration clazz = cu.addClass(className, Keyword.PUBLIC)
				.addExtendedType(dependentType)
				.addImplementedType(creatorType);

		clazz.addConstructor(Keyword.PUBLIC).setBody(new BlockStmt(new NodeList<>(new ExpressionStmt(
				new MethodCallExpr("super", new FieldAccessExpr(new TypeExpr(resourceType), "class"))))));

		clazz.addField(Vertx.class, FIELD_NAME_VERTX).addAnnotation(Inject.class);
		clazz.addField("ApiClient", FIELD_API_CLIENT, Keyword.PRIVATE);
		clazz.addField("HeaderAuthentication", FIELD_AUTHENTICATION).addAnnotation(Inject.class);
		clazz.addField("BaseUrlProvider", "urlProvider").addAnnotation(Inject.class);

	
		MethodDeclaration initClientMethod = clazz.addMethod("initClient", Keyword.PUBLIC)
				.addAnnotation(PostConstruct.class);
		
		
		ClassOrInterfaceType webClientSessionType = new ClassOrInterfaceType(null, WebClientSession.class.getSimpleName());
		ClassOrInterfaceType webClientType = new ClassOrInterfaceType(null, WebClient.class.getSimpleName());
		ClassOrInterfaceType vertxRequestAdapterType = new ClassOrInterfaceType(null, VertXRequestAdapter.class.getSimpleName());
		ClassOrInterfaceType apiClientType = new ClassOrInterfaceType(null, "ApiClient");
		
		NodeList<Statement> initClientStatements = new NodeList<>();
		
		initClientStatements.add(new ExpressionStmt(new AssignExpr(new VariableDeclarationExpr(webClientSessionType, VAR_WEB_CLIENT_SESSION), new MethodCallExpr(new TypeExpr(webClientSessionType), "create", new NodeList<>(new MethodCallExpr(new TypeExpr(webClientType), "create", new NodeList<>(new NameExpr(FIELD_NAME_VERTX))))), Operator.ASSIGN)));
		initClientStatements.add(new ExpressionStmt(new MethodCallExpr(new NameExpr(FIELD_AUTHENTICATION), "addAuthHeaders", new NodeList<>(new NameExpr(VAR_WEB_CLIENT_SESSION)))));
		initClientStatements.add(new ExpressionStmt(new AssignExpr(new VariableDeclarationExpr(vertxRequestAdapterType, "requestAdapter"), new ObjectCreationExpr(null, vertxRequestAdapterType, new NodeList<>(new NameExpr(VAR_WEB_CLIENT_SESSION))), Operator.ASSIGN)));
		initClientStatements.add(new ExpressionStmt(new MethodCallExpr(new NameExpr("urlProvider"), "provide", new NodeList<>(new NameExpr("requestAdapter")))));
		initClientStatements.add(new ExpressionStmt(new AssignExpr(new NameExpr(FIELD_API_CLIENT), new ObjectCreationExpr(null, apiClientType, new NodeList<>(new NameExpr("requestAdapter"))), Operator.ASSIGN)));
		initClientMethod.setBody(new BlockStmt(initClientStatements));
	
		
		MethodDeclaration fetchResourcesMethod = clazz.addMethod("fetchResources", Keyword.PUBLIC)
				.addAnnotation(Override.class).addParameter(crdType, "primaryResource").setType(setOfResourceType);
		setFetchResourcesBody(setType, fetchResourcesMethod);
		
		ClassOrInterfaceType contextType = new ClassOrInterfaceType(null,
				new SimpleName(Context.class.getSimpleName()),
				new NodeList<>(crdType));
		MethodDeclaration createMethod = clazz.addMethod("create", Keyword.PUBLIC)
				.addAnnotation(Override.class)
				.addParameter(resourceType, "desired")
				.addParameter(crdType, "primaryResource")
				.addParameter(contextType, "context")
				.setType(resourceType);
		//TODO We will add the real client method invocation
		createMethod.setBody(new BlockStmt(new NodeList<>(new ReturnStmt(new NullLiteralExpr()))));

		cu.setStorage(path.resolve(String.format("%s/%s.java",
				name.getQualifier().map(Name::toString).map(n -> n.replace(".", "/")).orElse(""), className)));
		SourceRoot dest = new SourceRoot(path);
		dest.add(cu);
		dest.saveAll();
	}

	private void setFetchResourcesBody(ClassOrInterfaceType setType, MethodDeclaration fetchResourcesMethod) {
		methodCalls.findById(new NameExpr(FIELD_API_CLIENT),
				new NodeList<>(new MethodCallExpr(new MethodCallExpr(new NameExpr("primaryResource"), "getMetadata"),
						"getName")))
				.ifPresent(m -> fetchResourcesMethod
						.setBody(new BlockStmt(new NodeList<>(new ReturnStmt(new MethodCallExpr(new TypeExpr(setType),
								"of", new NodeList<>(new MethodCallExpr(m, "get"))))))));
	}
}
