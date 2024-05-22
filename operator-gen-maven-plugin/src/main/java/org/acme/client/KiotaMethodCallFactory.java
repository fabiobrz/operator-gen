package org.acme.client;

import java.util.Map.Entry;
import java.util.Optional;

import org.acme.read.crud.CrudMapper;
import org.eclipse.microprofile.openapi.models.PathItem;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;

public class KiotaMethodCallFactory implements ApiClientMethodCallFactory {
	
	private final CrudMapper mapper;

	public KiotaMethodCallFactory(CrudMapper mapper) {
		this.mapper = mapper;
	}
	
	@Override
	public Optional<MethodCallExpr> findById(NameExpr apiClient, NodeList<Expression> args) {
		Optional<Entry<String, PathItem>> byIdPath = mapper.getByIdPath();
		return byIdPath
				.map(Entry::getKey)
				.map(k -> toMethodCallExpression(apiClient, k, args));
	}
	
	@Override
	public Optional<MethodCallExpr> create(NameExpr apiClient, NodeList<Expression> byIdArgs, NodeList<Expression> postArgs) {
		Optional<Entry<String, PathItem>> createPath = mapper.createPath();
		return createPath
				.map(Entry::getKey)
				.map(k -> new MethodCallExpr(toMethodCallExpression(apiClient, k, byIdArgs), "post", new NodeList<>(postArgs)));
	}
	
	@Override
	public Optional<MethodCallExpr> update(NameExpr apiClient, NodeList<Expression> byIdArgs, NodeList<Expression> patchArgs) {
		Optional<Entry<String, PathItem>> patchPath = mapper.patchPath();
		return patchPath
				.map(Entry::getKey)
				.map(k -> new MethodCallExpr(toMethodCallExpression(apiClient, k, byIdArgs), "patch", new NodeList<>(patchArgs)));
	}
	
	@Override
	public Optional<MethodCallExpr> delete(NameExpr apiClient, NodeList<Expression> byIdArgs) {
		Optional<Entry<String, PathItem>> delete = mapper.deletePath();
		return delete
				.map(Entry::getKey)
				.map(k -> new MethodCallExpr(toMethodCallExpression(apiClient, k, byIdArgs), "delete", new NodeList<>()));
	}
	
	private MethodCallExpr toMethodCallExpression(Expression prev, String pathKey, NodeList<Expression> args) {		
		if (pathKey.startsWith("/")) {
			pathKey = pathKey.substring(1);
		}
		int firstSegmentLength = pathKey.indexOf("/");
		if (firstSegmentLength < 0) {
			firstSegmentLength = pathKey.length();
		}
		String methodName = pathKey.substring(0, firstSegmentLength);
		MethodCallExpr methodCallExpr;
		if (methodName.startsWith("{")) {
			methodName = "by" + Character.toUpperCase(methodName.charAt(1)) + methodName.substring(2, methodName.length() - 1);
			methodCallExpr = new MethodCallExpr(prev, methodName, args);
		} else {
			methodCallExpr = new MethodCallExpr(prev, methodName);
		}
		if (firstSegmentLength < pathKey.length()) {
			return toMethodCallExpression(methodCallExpr, pathKey.substring(firstSegmentLength), args);
		} else {
			return methodCallExpr;
		}
	}

}
