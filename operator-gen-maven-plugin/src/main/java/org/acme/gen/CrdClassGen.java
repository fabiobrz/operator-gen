package org.acme.gen;

import java.nio.file.Path;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.utils.SourceRoot;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Deprecated(forRemoval = true)
public class CrdClassGen {
	private final Path path;
	private final Name name;
	private final Name specName;
	private final String statusName;

	public CrdClassGen(Path path, Name name, Name specName, String statusName) {
		this.path = path;
		this.name = name;
		this.specName = specName;
		this.statusName = statusName;

	}

	public void create() {
		var cu = new CompilationUnit(name.getQualifier().map(Name::toString).orElse(""));

		ClassOrInterfaceType specType = new ClassOrInterfaceType(null, specName.getIdentifier());
		ClassOrInterfaceType statusType = new ClassOrInterfaceType(null, statusName);

		ClassOrInterfaceType customResourceType = new ClassOrInterfaceType(null,
				new SimpleName(CustomResource.class.getSimpleName()), new NodeList<>(specType, statusType));

		cu.addImport(Version.class);
		cu.addImport(Group.class);
		cu.addImport(CustomResource.class);

		AnnotationExpr version = new SingleMemberAnnotationExpr(new Name(Version.class.getSimpleName()),
				new StringLiteralExpr("v1alpha1"));
		AnnotationExpr group = new SingleMemberAnnotationExpr(new Name(Group.class.getSimpleName()),
				new StringLiteralExpr("opgen.io"));
		cu.addClass(name.getIdentifier(), Keyword.PUBLIC).addExtendedType(customResourceType)
				.addImplementedType(Namespaced.class).addAnnotation(version).addAnnotation(group);

		cu.setStorage(path.resolve(String.format("%s/%s.java",
				name.getQualifier().map(Name::toString).map(n -> n.replace(".", "/")).orElse(""),
				name.getIdentifier())));
		SourceRoot dest = new SourceRoot(path);
		dest.add(cu);
		dest.saveAll();
	}

}
