package org.acme.gen;

import java.nio.file.Path;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.utils.SourceRoot;

import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;

public class ReconcilerGen {
	private static final String NAME_POSTFIX = "Reconciler";
	private final Path path;
	private final Name name;

	public ReconcilerGen(Path path, Name name) {
		this.path = path;
		this.name = name;
	}

	public void create() {
		String className = name.getIdentifier() + NAME_POSTFIX;
		var cu = new CompilationUnit(name.getQualifier().map(Name::toString).orElse(""));

		ClassOrInterfaceType crdType = new ClassOrInterfaceType(null, name.getIdentifier());
		ClassOrInterfaceType reconcilerType = new ClassOrInterfaceType(null,
				new SimpleName(Reconciler.class.getSimpleName()), new NodeList<>(crdType));

		cu.addImport(Reconciler.class);
		cu.addImport(UpdateControl.class);
		cu.addImport(Context.class);
		cu.addImport(ControllerConfiguration.class);
		cu.addImport(Dependent.class);

		ClassOrInterfaceType reconcileReturnType = new ClassOrInterfaceType(null,
				new SimpleName(UpdateControl.class.getSimpleName()), new NodeList<>(crdType));
		ClassOrInterfaceType contextParameterType = new ClassOrInterfaceType(null,
				new SimpleName(Context.class.getSimpleName()), new NodeList<>(crdType));
		ClassOrInterfaceType updateControlType = new ClassOrInterfaceType(null, UpdateControl.class.getSimpleName());

		ClassOrInterfaceDeclaration clazz = cu.addClass(className, Keyword.PUBLIC).addImplementedType(reconcilerType);
		createControllerConfigAnnotation(clazz);
		clazz.addMethod("reconcile", Keyword.PUBLIC).addParameter(crdType, "resource")
				.addParameter(contextParameterType, "context").addAnnotation(Override.class)
				.setBody(new BlockStmt(new NodeList<>(
						new ReturnStmt(new MethodCallExpr(new TypeExpr(updateControlType), "noUpdate")))))
				.setType(reconcileReturnType);

		cu.setStorage(path.resolve(String.format("%s/%s.java",
				name.getQualifier().map(Name::toString).map(n -> n.replace(".", "/")).orElse(""), className)));
		SourceRoot dest = new SourceRoot(path);
		dest.add(cu);
		dest.saveAll();
	}

	private void createControllerConfigAnnotation(ClassOrInterfaceDeclaration cu) {
		NodeList<MemberValuePair> dependentMembers = new NodeList<>();
		dependentMembers.add(new MemberValuePair(new SimpleName("name"), new StringLiteralExpr(name.getIdentifier())));
		dependentMembers.add(new MemberValuePair(new SimpleName("type"),
				new ClassExpr(new ClassOrInterfaceType(null, name.getIdentifier() + "Dependent"))));
		NormalAnnotationExpr resourceDependent = new NormalAnnotationExpr(new Name(Dependent.class.getSimpleName()),
				dependentMembers);
		NodeList<Expression> dependents = new NodeList<>();
		dependents.add(resourceDependent);
		NodeList<MemberValuePair> controllerConfigMembers = new NodeList<>();
		controllerConfigMembers
				.add(new MemberValuePair(new SimpleName("dependents"), new ArrayInitializerExpr(dependents)));
		AnnotationExpr controllerConfiguration = new NormalAnnotationExpr(
				new Name(ControllerConfiguration.class.getSimpleName()), controllerConfigMembers);
		cu.addAnnotation(controllerConfiguration);
	}
}
