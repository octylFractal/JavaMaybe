package com.techshroom.javamaybe;

import java.util.WeakHashMap;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.typesystem.Type;

public class TypeSolverUtil {

    private static final Node OBJECT_REFERENCE;
    static {
        // Extracts an in-context ObjectCreationExpr from the code
        CompilationUnit unit = JavaParser.parse("class T{{new Object().clone();}}");
        OBJECT_REFERENCE = unit.accept(new GenericVisitorAdapter<Node, Void>() {

            @Override
            public Node visit(ObjectCreationExpr n, Void arg) {
                return n;
            }
        }, null);
    }
    private static final WeakHashMap<JavaParserFacade, Type> SOLVED_OBJECT = new WeakHashMap<>();

    public static Type getType(Node node, JavaParserFacade typeSolver) {
        return runtimeType(typeSolver.getType(node), typeSolver);
    }

    public static Type runtimeType(Type type, JavaParserFacade typeSolver) {
        if (type.isTypeVariable()) {
            return SOLVED_OBJECT.computeIfAbsent(typeSolver, t -> t.getType(OBJECT_REFERENCE));
        }
        return type;
    }

}
