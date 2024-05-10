package org.acme.gen;

import java.nio.file.Path;
import java.util.Set;

import org.acme.client.ApiClientMethodCallFactory;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.utils.SourceRoot;

import io.javaoperatorsdk.operator.processing.dependent.external.PerResourcePollingDependentResource;

public class DependentGen {
	private static final String NAME_POSTFIX = "Dependent";
	private final Path path;
	private final Name name;
	private final Name resource;
	private final ApiClientMethodCallFactory mathodCalls;

	public DependentGen(Path path, Name name, Name resource, ApiClientMethodCallFactory mathodCalls) {
		this.path = path;
		this.name = name;
		this.resource = resource;
		this.mathodCalls = mathodCalls;
	}

	public void create() {
		String className = name.getIdentifier() + NAME_POSTFIX;
		var cu = new CompilationUnit(name.getQualifier().map(Name::toString).orElse(""));
		cu.addImport(PerResourcePollingDependentResource.class);
		cu.addImport(Set.class);
		cu.addImport(resource.toString());
		cu.addImport(resource.getQualifier().map(Name::toString).orElse("").replace(".models", "") + ".ApiClient");
		ClassOrInterfaceType crdType = new ClassOrInterfaceType(null, name.toString());
		ClassOrInterfaceType resourceType = new ClassOrInterfaceType(null, name.getIdentifier());
		ClassOrInterfaceType dependentType = new ClassOrInterfaceType(null,
				new SimpleName(PerResourcePollingDependentResource.class.getSimpleName()),
				new NodeList<>(resourceType, crdType));
		ClassOrInterfaceType setOfResourceType = new ClassOrInterfaceType(null,
				new SimpleName(Set.class.getSimpleName()), new NodeList<>(resourceType));
		ClassOrInterfaceType setType = new ClassOrInterfaceType(null, Set.class.getSimpleName());

		AnnotationExpr override = new MarkerAnnotationExpr(new Name(Override.class.getSimpleName()));

		ClassOrInterfaceDeclaration clazz = cu.addClass(className, Keyword.PUBLIC).addExtendedType(dependentType);

		clazz.addConstructor(Keyword.PUBLIC).setBody(new BlockStmt(new NodeList<>(new ExpressionStmt(
				new MethodCallExpr("super", new FieldAccessExpr(new TypeExpr(resourceType), "class"))))));

		clazz.addField("ApiClient", "apiClient", Keyword.PRIVATE);
		MethodDeclaration fetchResourcesMethod = clazz.addMethod("fetchResources", Keyword.PUBLIC)
				.addAnnotation(override).addParameter(crdType, "primaryResource").setType(setOfResourceType);

		mathodCalls.findById(new NameExpr("apiClient"),
				new NodeList<>(new MethodCallExpr(new MethodCallExpr(new NameExpr("primaryResource"), "getMetadata"),
						"getName")))
				.ifPresent(m -> fetchResourcesMethod
						.setBody(new BlockStmt(new NodeList<>(new ReturnStmt(new MethodCallExpr(new TypeExpr(setType),
								"of", new NodeList<>(new MethodCallExpr(m, "get"))))))));

		cu.setStorage(path.resolve(String.format("%s/%s.java",
				name.getQualifier().map(Name::toString).map(n -> n.replace(".", "/")).orElse(""), className)));
		SourceRoot dest = new SourceRoot(path);
		dest.add(cu);
		dest.saveAll();
	}
}
