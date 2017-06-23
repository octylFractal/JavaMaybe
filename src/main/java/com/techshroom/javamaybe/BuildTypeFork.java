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

import java.util.Optional;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * Searches the passed method for conditionals
 */
public class BuildTypeFork extends VoidVisitorAdapter<TypeForkPath> {

    private Optional<String> getScopeNameOfCondition(UnifiedConditional cond, TypeForkPath ctx) {
        Expression e = cond.getCondition();
        if (!(e instanceof MethodCallExpr)) {
            return Optional.empty();
        }
        MethodCallExpr mce = (MethodCallExpr) e;
        return NodeUtil.getScopeNameIfFork(mce).filter(ctx.getAnyParams()::contains);
    }

    @Override
    public void visit(MethodCallExpr n, TypeForkPath ctx) {
        // param.as(/* args */)
        Optional<String> param = NodeUtil.getScopeNameIfAs(n);
        if (param.isPresent()) {
            ctx.putType(param.get(), TypeSolverUtil.getType(n, ctx.getTypeSolver()));
        } else {
            super.visit(n, ctx);
        }
    }

    @Override
    public void visit(ConditionalExpr n, TypeForkPath ctx) {
        if (!tryAddFork(UnifiedConditional.wrap(n), ctx)) {
            super.visit(n, ctx);
        }
    }

    @Override
    public void visit(IfStmt n, TypeForkPath ctx) {
        if (!tryAddFork(UnifiedConditional.wrap(n), ctx)) {
            super.visit(n, ctx);
        }
    }

    private boolean tryAddFork(UnifiedConditional cond, TypeForkPath ctx) {
        Optional<String> scopeName = getScopeNameOfCondition(cond, ctx);
        if (scopeName.isPresent()) {
            addForkPaths(cond, scopeName.get(), ctx);
            return true;
        }
        return false;
    }

    private void addForkPaths(UnifiedConditional cond, String param, TypeForkPath ctx) {
        TypeForkPath primary = createForkPath(cond.getThen(), param, ctx);
        Optional<TypeForkPath> secondary = cond.getElse().map(node -> createForkPath(node, param, ctx));
        ctx.getForks().add(TypeFork.create(cond, primary, secondary.orElse(null)));
    }

    private TypeForkPath createForkPath(Node node, String param, TypeForkPath ctx) {
        TypeForkPath forkPath = ctx.createSubPath();
        // visit node in sub-path
        node.accept(this, forkPath);
        return forkPath;
    }

}
