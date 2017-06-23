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
