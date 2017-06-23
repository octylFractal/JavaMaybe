package com.techshroom.javamaybe;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.typesystem.Type;

public class TypeFinder extends GenericVisitorAdapter<Type, String> {

    private final JavaParserFacade typeSolver;

    public TypeFinder(JavaParserFacade typeSolver) {
        this.typeSolver = typeSolver;
    }

    @Override
    public Type visit(MethodCallExpr n, String param) {
        // param.as(/* args */)
        if (NodeUtil.isAsCall(n, param)) {
            return TypeSolverUtil.getType(n, typeSolver);
        }
        return super.visit(n, param);
    }

}
