package com.techshroom.javamaybe;

import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;

public class MethodPreprocessor {

    /**
     * Restructures the method in preparation for processing.
     */
    public static MethodDeclaration process(MethodDeclaration method) {
        MethodDeclaration copy = method.clone();
        copy.getBody().ifPresent(block -> {
            NodeList<Statement> stmts = block.getStatements();
            for (int i = 0; i < stmts.size(); i++) {
                Statement stmt = stmts.get(i);
                Optional<IfStmt> endOfChain = getEndOfElseChain(stmt);
                if (endOfChain.isPresent()) {
                    BlockStmt sub = new BlockStmt();
                    List<Statement> subList = stmts.subList(i + 1, stmts.size());
                    sub.getStatements().addAll(subList);
                    subList.clear();
                    endOfChain.get().setElseStmt(sub);
                    break;
                }
             }
        });
        TypeDeclaration<?> decl = (TypeDeclaration<?>) method.getParentNode().get();
        decl.getMembers().replace(method, copy);
        return copy;
    }

    private static Optional<IfStmt> getEndOfElseChain(Statement stmt) {
        return Optional.of(stmt)
                .filter(IfStmt.class::isInstance)
                .map(IfStmt.class::cast)
                .map(ifStmt -> {
                    while (ifStmt.hasElseBlock() && ifStmt.getElseStmt().get() instanceof IfStmt) {
                        ifStmt = (IfStmt) ifStmt.getElseStmt().get();
                    }
                    return ifStmt;
                }).filter(ifStmt -> !ifStmt.getElseStmt().isPresent());
    }

}
