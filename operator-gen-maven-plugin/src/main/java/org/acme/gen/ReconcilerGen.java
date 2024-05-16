package org.acme.gen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.utils.SourceRoot;
import com.microsoft.kiota.serialization.KiotaJsonSerialization;

import io.fabric8.kubernetes.client.utils.KubernetesSerialization;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.processing.dependent.workflow.WorkflowReconcileResult;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class ReconcilerGen {
	private static final String NAME_POSTFIX = "Reconciler";
	private final Path path;
	private final Name name;
	private Name resource;

	public ReconcilerGen(Path path, Name name, Name resource) {
		this.path = path;
		this.name = name;
		this.resource = resource;
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
		cu.addImport(ObjectMapper.class);
		cu.addImport(Optional.class);
		cu.addImport(KubernetesSerialization.class);
		cu.addImport(InputStream.class);
		cu.addImport(KiotaJsonSerialization.class);
		cu.addImport(IOException.class);
		cu.addImport(WorkflowReconcileResult.class);

		ClassOrInterfaceType reconcileReturnType = new ClassOrInterfaceType(null,
				new SimpleName(UpdateControl.class.getSimpleName()), new NodeList<>(crdType));
		ClassOrInterfaceType contextParameterType = new ClassOrInterfaceType(null,
				new SimpleName(Context.class.getSimpleName()), new NodeList<>(crdType));
		ClassOrInterfaceType updateControlType = new ClassOrInterfaceType(null, UpdateControl.class.getSimpleName());
		
		ClassOrInterfaceDeclaration clazz = cu.addClass(className, Keyword.PUBLIC).addImplementedType(reconcilerType);
		
		createControllerConfigAnnotation(clazz);
		
		ClassOrInterfaceType kubernetesSerializationType = new ClassOrInterfaceType(null, KubernetesSerialization.class.getSimpleName());
		clazz.addField(ObjectMapper.class, "mapper").addAnnotation(Inject.class);
		clazz.addField(KubernetesSerialization.class, "serialization").setModifiers(Keyword.PRIVATE);
		
		MethodDeclaration initMethod = clazz.addMethod("init", Keyword.PUBLIC)
				.addAnnotation(PostConstruct.class);
		initMethod.setBody(new BlockStmt(new NodeList<>(new ExpressionStmt(new AssignExpr(new NameExpr("serialization"), new ObjectCreationExpr(null, kubernetesSerializationType, new NodeList<>(new NameExpr("mapper"), new BooleanLiteralExpr(true))), Operator.ASSIGN)))));
		
		ClassOrInterfaceType workflowReconcileResultType = new ClassOrInterfaceType(null, WorkflowReconcileResult.class.getSimpleName());
		ClassOrInterfaceType optionalworkflowReconcileResultType = new ClassOrInterfaceType(null,
				new SimpleName(Optional.class.getSimpleName()), new NodeList<>(workflowReconcileResultType));
		ClassOrInterfaceType resourceType = new ClassOrInterfaceType(null, resource.toString());
		ClassOrInterfaceType optionalResourceType = new ClassOrInterfaceType(null,
				new SimpleName(Optional.class.getSimpleName()), new NodeList<>(resourceType));
		ClassOrInterfaceType dependentType = new ClassOrInterfaceType(null, resource.getId() + "Dependent");
		
		MethodCallExpr noUpdate = new MethodCallExpr(new TypeExpr(updateControlType), "noUpdate");
		AssignExpr workflowReconcileResult = new AssignExpr(new VariableDeclarationExpr(optionalworkflowReconcileResultType, "workflowReconcileResult"), new MethodCallExpr(new MethodCallExpr(new NameExpr("context"), "managedDependentResourceContext"), "getWorkflowReconcileResult"), Operator.ASSIGN);
		
		MethodCallExpr getReconciledDependents = new MethodCallExpr(new MethodCallExpr(new MethodCallExpr(new NameExpr("r"), "getReconciledDependents"), "stream"), "filter", new NodeList<>(new LambdaExpr(new Parameter(new UnknownType(), "d"), new InstanceOfExpr(new NameExpr("d"), dependentType))));
		MethodCallExpr cast = new MethodCallExpr(getReconciledDependents, "map", new NodeList<>(new LambdaExpr(new Parameter(new UnknownType(), "d"), new CastExpr(dependentType, new NameExpr("d")))));
		MethodCallExpr getSecondaryResource = new MethodCallExpr(new MethodCallExpr(cast, "findAny"), "flatMap", new NodeList<>(new LambdaExpr(new Parameter(new UnknownType(), "d"), new MethodCallExpr(new NameExpr("d"), "getSecondaryResource", new NodeList<>(new NameExpr("resource"), new NameExpr("context"))))));
		MethodCallExpr filterUserDependent = new MethodCallExpr(new NameExpr("workflowReconcileResult"), "flatMap", new NodeList<>(new LambdaExpr(new Parameter(new UnknownType(), "r"), getSecondaryResource)));
		AssignExpr userFromReconcilation = new AssignExpr(new VariableDeclarationExpr(optionalResourceType, "model"), filterUserDependent, Operator.ASSIGN);
		
		MethodCallExpr setStatus = new MethodCallExpr(new NameExpr("resource"), "setStatus", new NodeList<>(new NameExpr("s")));
		MethodCallExpr mapModelLambda = new MethodCallExpr(new MethodCallExpr("readStatus", new NameExpr("r"), new NameExpr("context")), "ifPresent", new NodeList<>(new LambdaExpr(new Parameter(new UnknownType(), "s"), setStatus)));
		MethodCallExpr patchStatus = new MethodCallExpr(new TypeExpr(updateControlType), "patchStatus", new NodeList<>(new NameExpr("resource")));
		MethodCallExpr mapModel = new MethodCallExpr(new NameExpr("model"), "map", new NodeList<>(new LambdaExpr(new Parameter(new UnknownType(), "r"), new BlockStmt(new NodeList<>(new ExpressionStmt(mapModelLambda), new ReturnStmt(patchStatus))))));
		MethodCallExpr mapModelOrElse = new MethodCallExpr(mapModel, "orElse", new NodeList<>(noUpdate));
		
		clazz.addMethod("reconcile", Keyword.PUBLIC).addParameter(crdType, "resource")
				.addParameter(contextParameterType, "context").addAnnotation(Override.class)
				.setBody(new BlockStmt(new NodeList<>(
						new ExpressionStmt(workflowReconcileResult),
						new ExpressionStmt(userFromReconcilation),
						new ReturnStmt(mapModelOrElse)
						)))
				.setType(reconcileReturnType);
		
		
		ClassOrInterfaceType inputStreamType = new ClassOrInterfaceType(null, InputStream.class.getSimpleName());
		ClassOrInterfaceType kiotaJsonSerializationType = new ClassOrInterfaceType(null, KiotaJsonSerialization.class.getSimpleName());
		ClassOrInterfaceType ioExceptionType = new ClassOrInterfaceType(null, IOException.class.getSimpleName());
		ClassOrInterfaceType optionalType = new ClassOrInterfaceType(null, Optional.class.getSimpleName());
		ClassOrInterfaceType crdStatusType = new ClassOrInterfaceType(null, name.getIdentifier() + "Status");
		ClassOrInterfaceType optionalCrdStatusType = new ClassOrInterfaceType(null,
				new SimpleName(Optional.class.getSimpleName()), new NodeList<>(crdStatusType));
		
		MethodCallExpr unmarshalToUserStatus = new MethodCallExpr(new TypeExpr(optionalType), "of", new NodeList<>(new MethodCallExpr(new NameExpr("serialization"), "unmarshal", new NodeList<>(new NameExpr("jsonStream"), new FieldAccessExpr(new TypeExpr(crdStatusType), "class")))));
		MethodCallExpr optionalEmpty = new MethodCallExpr(new TypeExpr(optionalType), "empty");
		clazz.addMethod("readStatus", Keyword.PUBLIC)
			.addParameter(resourceType, "model")
			.addParameter(contextParameterType, "context")
		.setBody(new BlockStmt(new NodeList<>(
			new TryStmt(new NodeList<>(new AssignExpr(new VariableDeclarationExpr(inputStreamType, "jsonStream"), new MethodCallExpr(new TypeExpr(kiotaJsonSerializationType), "serializeAsStream", new NodeList<>(new NameExpr("model"))), Operator.ASSIGN)), new BlockStmt(new NodeList<>(new ReturnStmt(unmarshalToUserStatus))), new NodeList<>(new CatchClause(new Parameter(ioExceptionType, "e"), new BlockStmt(new NodeList<>(new ReturnStmt(optionalEmpty))))), null))))
		.setType(optionalCrdStatusType);
		
		/*@Inject
	ObjectMapper mapper;
	
	
    @Override()
    public UpdateControl<User> reconcile(User resource, Context<User> context) {
    	Optional<WorkflowReconcileResult> workflowReconcileResult = context.managedDependentResourceContext().getWorkflowReconcileResult();
    	Optional<io.apisdk.gitea.json.models.User> user = workflowReconcileResult.flatMap(r -> r.getReconciledDependents().stream().filter(d -> d instanceof UserDependent).map(d -> (UserDependent) d).findAny().flatMap(d -> d.getSecondaryResource(resource, context)));
		return user.map(r -> {
			toCrd(r, context).ifPresent(s -> resource.setStatus(s));
		    return UpdateControl.patchStatus(resource);
		}).orElse(UpdateControl.noUpdate());
    }
    
    private Optional<UserStatus> toCrd(io.apisdk.gitea.json.models.User primary, Context<User> context) {	
    	KubernetesSerialization serialization = new KubernetesSerialization(mapper, true);
    	try (InputStream jsonStream = KiotaJsonSerialization.serializeAsStream(primary)){
			return Optional.of(serialization.unmarshal(jsonStream, UserStatus.class));
		} catch (IOException e) {
			return Optional.empty();
		}
    	
    }*/

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
