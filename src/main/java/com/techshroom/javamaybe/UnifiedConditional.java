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

import java.util.Optional;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.IfStmt;

public abstract class UnifiedConditional {

    public static UnifiedConditional wrap(Node node) {
        boolean ifStmt = node instanceof IfStmt;
        checkArgument(ifStmt || node instanceof ConditionalExpr, "node is not an IfStmt or CondExpr");
        if (ifStmt) {
            return new IfStmtUC((IfStmt) node);
        } else {
            return new CondExprUC((ConditionalExpr) node);
        }
    }

    private static final class IfStmtUC extends UnifiedConditional {

        private final IfStmt delegate;

        private IfStmtUC(IfStmt delegate) {
            this.delegate = delegate;
        }

        @Override
        public IfStmt getDelegate() {
            return delegate;
        }

        @Override
        public Expression getCondition() {
            return delegate.getCondition();
        }

        @Override
        public Node getThen() {
            return delegate.getThenStmt();
        }

        @Override
        public Optional<? extends Node> getElse() {
            return delegate.getElseStmt();
        }

    }

    private static final class CondExprUC extends UnifiedConditional {

        private final ConditionalExpr delegate;

        private CondExprUC(ConditionalExpr delegate) {
            this.delegate = delegate;
        }

        @Override
        public ConditionalExpr getDelegate() {
            return delegate;
        }

        @Override
        public Expression getCondition() {
            return delegate.getCondition();
        }

        @Override
        public Node getThen() {
            return delegate.getThenExpr();
        }

        @Override
        public Optional<Node> getElse() {
            return Optional.of(delegate.getElseExpr());
        }

    }

    public abstract Node getDelegate();

    public abstract Expression getCondition();

    public abstract Node getThen();

    public abstract Optional<? extends Node> getElse();

    UnifiedConditional() {
    }

}
