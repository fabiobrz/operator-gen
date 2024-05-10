package org.acme.client;

import java.util.Optional;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;

public interface ApiClientMethodCallFactory {

	Optional<MethodCallExpr> findById(NameExpr apiClient, NodeList<Expression> args);

}
