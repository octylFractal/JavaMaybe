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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.symbolsolver.model.typesystem.Type;
import com.techshroom.javamaybe.debug.ASTPrinter;

public class PropagateTypeFork extends ModifierVisitor<TypeForkPath> {

    private static Optional<TypeFork> getMatchingFork(Node n, TypeForkPath ctx) {
        return ctx.getForks().stream()
                .filter(tf -> tf.getConditional().getDelegate().equals(n)).findFirst();
    }

    private final Map<String, String> params;

    public PropagateTypeFork(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public Visitable visit(MethodCallExpr n, TypeForkPath ctx) {
        Optional<String> param = NodeUtil.getScopeNameIfAs(n);
        if (param.isPresent()) {
            return new NameExpr(param.get());
        }
        return super.visit(n, ctx);
    }

    @Override
    public Visitable visit(ConditionalExpr n, TypeForkPath ctx) {
        return visitFork(n, ctx).orElseGet(() -> super.visit(n, ctx));
    }

    @Override
    public Visitable visit(IfStmt n, TypeForkPath ctx) {
        return visitFork(n, ctx).orElseGet(() -> super.visit(n, ctx));
    }

    private Optional<Visitable> visitFork(Node n, TypeForkPath ctx) {
        UnifiedConditional cond = UnifiedConditional.wrap(n);
        Optional<TypeFork> forkOpt = getMatchingFork(n, ctx);
        if (forkOpt.isPresent()) {
            TypeFork fork = forkOpt.get();
            // these two conditions should be tied together
            assert cond.getElse().isPresent() == fork.getSecondaryPath().isPresent();

            if (parameterIn(fork::getPrimaryTypes)) {
                // all in primary
                Node node = cond.getThen().clone();
                node = (Node) node.accept(this, fork.getPrimaryPath());
                return Optional.of(node);
            } else if (parameterIn(fork::getSecondaryTypes)) {
                // all in secondary
                Node node = cond.getElse().get().clone();
                node = (Node) node.accept(this, fork.getSecondaryPath().get());
                return Optional.of(node);
            } else {
                params.forEach((param, type) -> {
                    System.err.println(param + "=" + type);
                    ASTPrinter.print(fork.getPrimaryTypes(param));
                    ASTPrinter.print(fork.getSecondaryTypes(param));
                });
                return Optional
                        .of(cond.getDelegate() instanceof ConditionalExpr ? new NullLiteralExpr() : new BlockStmt());
            }
        }
        return Optional.empty();
    }

    private boolean parameterIn(Function<String, Set<Type>> types) {
        return params.entrySet().stream()
                .allMatch(e -> {
                    // either in types OR types is empty
                    Set<Type> ts = types.apply(e.getKey());
                    return ts.isEmpty() || ts.stream().anyMatch(t -> e.getValue().equals(t.describe()));
                });
    }

}
