package com.techshroom.javamaybe;

import java.util.Set;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.typesystem.Type;
import com.techshroom.javamaybe.compile.TypeCap;

public class CompileOnlyCleanupVisitor extends ModifierVisitor<Set<String>> {

    private final JavaParserFacade typeSolver;

    public CompileOnlyCleanupVisitor(JavaParserFacade typeSolver) {
        this.typeSolver = typeSolver;
    }

    @Override
    public Visitable visit(VariableDeclarator n, Set<String> destroyedFields) {
        if (n.getParentNode().filter(p -> p instanceof FieldDeclaration).isPresent()) {
            // Parent is field, check it!
            Type type = typeSolver.getType(n);
            if (type.isReferenceType() && type.asReferenceType().getQualifiedName().equals(TypeCap.class.getName())) {
                // destroy this field
                destroyedFields.add(n.getNameAsString());
                return null;
            }
        }
        return super.visit(n, destroyedFields);
    }

}
