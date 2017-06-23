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
