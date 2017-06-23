package com.techshroom.javamaybe;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;

public class NodeUtil {

    public static boolean isAsCall(MethodCallExpr mce, String param) {
        return getScopeNameOfMCE(mce).filter(Predicate.isEqual(param)).isPresent()
                && mce.getNameAsString().equals("as");
    }

    public static Optional<String> getScopeNameIfAs(MethodCallExpr mce) {
        if (mce.getNameAsString().equals("as")) {
            return mce.getScope().filter(NameExpr.class::isInstance)
                    .map(NameExpr.class::cast)
                    .map(NameExpr::getNameAsString);
        }
        return Optional.empty();
    }

    public static Optional<String> getScopeNameIfFork(MethodCallExpr mce) {
        if (mce.getNameAsString().equals("typeFork")) {
            return mce.getScope().filter(NameExpr.class::isInstance)
                    .map(NameExpr.class::cast)
                    .map(NameExpr::getNameAsString);
        }
        return Optional.empty();
    }

    public static Optional<String> getScopeNameOfMCE(MethodCallExpr mce) {
        return mce.getScope()
                .filter(NameExpr.class::isInstance)
                .map(NameExpr.class::cast)
                .map(NameExpr::getNameAsString);
    }

    public static boolean replace(Node old, Node expr) {
        checkArgument(old.getParentNode().isPresent());
        Node parent = old.getParentNode().get();
        if (parent instanceof Statement) {
            List<NodeList<?>> lists = old.getParentNode().get().getNodeLists();
            for (NodeList<?> nl : lists) {
                @SuppressWarnings("unchecked")
                // ? extends Node, so this works
                NodeList<Node> list = (NodeList<Node>) nl;
                if (list != null) {
                    if (list.replace(old, expr)) {
                        return true;
                    }
                }
            }
        }
        if (parent instanceof MethodCallExpr) {
            MethodCallExpr mce = (MethodCallExpr) parent;
            if (replaceOpt(old, expr, Expression.class, mce::getScope, mce::setScope)) {
                return true;
            }
            if (replaceNl(old, expr, Expression.class, mce.getArguments())) {
                return true;
            }
        }
        if (parent instanceof ReturnStmt) {
            ReturnStmt rs = (ReturnStmt) parent;
            return replaceOpt(old, expr, Expression.class, rs::getExpression, rs::setExpression);
        }
        if (parent instanceof ObjectCreationExpr) {
            ObjectCreationExpr oce = (ObjectCreationExpr) parent;
            return replaceNl(old, expr, Expression.class, oce.getArguments());
        }
        throw new IllegalStateException("Couldn't replace in " + parent.getClass());
    }

    private static <T> boolean replaceOpt(Object old, Object expr, Class<T> type, Supplier<Optional<T>> getter,
            Consumer<T> setter) {
        if (type.isInstance(old) && type.isInstance(expr)) {
            Optional<T> get = getter.get();
            if (get.isPresent() && get.get().equals(old)) {
                setter.accept(type.cast(expr));
                return true;
            }
        }
        return false;
    }

    private static <T extends Node> boolean replaceNl(Object old, Object expr, Class<T> type, NodeList<T> list) {
        if (type.isInstance(old) && type.isInstance(expr)) {
            return list.replace(type.cast(old), type.cast(expr));
        }
        return false;
    }

}
