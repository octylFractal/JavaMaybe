package com.techshroom.javamaybe.debug;

import java.util.Collection;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.TreeVisitor;
import com.github.javaparser.symbolsolver.model.typesystem.Type;

public class ASTPrinter {

    public static void print(Node node) {
        TreeVisitor v = new TreeVisitor() {

            @Override
            public void process(Node node) {
                System.err.println(
                        "Node " + node.getClass().getSimpleName() + ": " + node.toString().replace("\n", "\\n"));
            }
        };
        v.visitPreOrder(node);
    }

    public static void print(Collection<Type> types) {
        System.err.println("Collection of types:");
        types.forEach(t -> {
            System.err.println("\t" + t.describe());
        });
    }

}
