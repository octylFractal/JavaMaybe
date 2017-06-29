/*
 * This file is part of JavaMaybe, licensed under the MIT License (MIT).
 *
 * Copyright (c) TechShroom Studios <https://techshroom.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.techshroom.javamaybe;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import com.google.common.collect.ImmutableSet;

public class NodeUtil {

    private static final Set<String> AS_CALLS = ImmutableSet.of("from", "to", "convert");

    private static boolean isAsCall(MethodCallExpr mce) {
        if (mce.getScope()
                .filter(NameExpr.class::isInstance)
                .map(NameExpr.class::cast)
                .filter(ne -> ne.getNameAsString().equals("Any")).isPresent()
                && AS_CALLS.contains(mce.getNameAsString())) {
            return true;
        }
        return false;
    }

    private static int getArgAsCall(MethodCallExpr mce) {
        return mce.getNameAsString().equals("to") ? 1 : 0;
    }

    public static boolean isAsCall(MethodCallExpr mce, String param) {
        return isAsCall(mce)
                && getScopeNameOfMCE(mce, getArgAsCall(mce)).filter(Predicate.isEqual(param)).isPresent();
    }

    public static Optional<String> getScopeNameIfAs(MethodCallExpr mce) {
        if (isAsCall(mce)) {
            return getScopeNameOfMCE(mce, getArgAsCall(mce));
        }
        return Optional.empty();
    }

    public static Optional<String> getScopeNameIfFork(MethodCallExpr mce) {
        if (mce.getNameAsString().equals("typeFork")) {
            return getScopeNameOfMCE(mce, 0);
        }
        return Optional.empty();
    }

    public static Optional<String> getScopeNameOfMCE(MethodCallExpr mce, int arg) {
        return Optional.of(mce.getArgument(arg))
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
