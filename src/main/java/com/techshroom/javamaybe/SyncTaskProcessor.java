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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.model.typesystem.Type;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.techshroom.javamaybe.compile.Any;

public class SyncTaskProcessor implements TaskProcessor {

    private final JavaParserFacade typeSolver;

    @Inject
    SyncTaskProcessor(TypeSolver typeSolver) {
        this.typeSolver = JavaParserFacade.get(typeSolver);
    }

    @Override
    public CompletableFuture<CompilationUnit> process(Task task) {
        CompilationUnit unit = task.getUnit();

        unit.accept(new AnyReplacementVisitor(typeSolver), null);

        return CompletableFuture.completedFuture(unit);
    }

    private static final class AnyReplacementVisitor extends ModifierVisitor<Void> {

        private final JavaParserFacade typeSolver;

        public AnyReplacementVisitor(JavaParserFacade typeSolver) {
            this.typeSolver = typeSolver;
        }

        @Override
        public Visitable visit(MethodCallExpr n, Void arg) {
            // any method calls to Any.wrap?
            Optional<String> scopeName = NodeUtil.getScopeNameOfMCE(n);
            if (scopeName.isPresent() && scopeName.get().equals("Any") && n.getNameAsString().equals("wrap")) {
                // yep!
                return n.getArgument(0);
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(ClassOrInterfaceDeclaration n, Void arg) {
            // do sub-processing first...
            ClassOrInterfaceDeclaration decl = (ClassOrInterfaceDeclaration) super.visit(n, arg);
            // then split methods in it...
            splitMethods(decl);

            return decl;
        }

        @Override
        public Visitable visit(EnumDeclaration n, Void arg) {
            // do sub-processing first...
            EnumDeclaration decl = (EnumDeclaration) super.visit(n, arg);
            // then split methods in it...
            splitMethods(decl);

            return decl;
        }

        private void splitMethods(TypeDeclaration<?> decl) {
            List<MethodDeclaration> methods = ImmutableList.copyOf(decl.getMethods());
            methods.forEach(m -> splitMethod(decl, m));
        }

        private void splitMethod(TypeDeclaration<?> decl, MethodDeclaration method) {
            MethodDeclaration m = MethodPreprocessor.process(method);
            SlotIteration.Builder<Parameter> iteration = SlotIteration.builder();
            List<String> parameters =
                    m.getParameters().stream().map(Parameter::getNameAsString).collect(ImmutableList.toImmutableList());
            Set<String> anyParams = m.getParameters().stream()
                    .filter(p -> {
                        Type t = typeSolver.getType(p);
                        return t.isReferenceType()
                                && t.asReferenceType().getQualifiedName().equals(Any.class.getName());
                    })
                    .map(Parameter::getNameAsString)
                    .collect(ImmutableSet.toImmutableSet());
            if (anyParams.isEmpty()) {
                return;
            }
            for (int i = 0; i < parameters.size(); i++) {
                if (!anyParams.contains(parameters.get(i))) {
                    iteration.addItemToSlot(m.getParameter(i), i);
                }
            }

            // ASTPrinter.print(m);

            TypeForkPath ctx = TypeForkPath.construct(typeSolver, m);
            m.accept(new BuildTypeFork(), ctx);

            SlotIteration.Builder<Type> paramTypeIterBuilder = SlotIteration.builder();
            anyParams.forEach(param -> {
                int index = parameters.indexOf(param);
                paramTypeIterBuilder.addItemsToSlot(ctx.resolveTypesOfParameter(param), index);
            });

            for (List<Type> types : paramTypeIterBuilder.build()) {
                List<Parameter> mParams = Streams.mapWithIndex(types.stream(),
                        (t, i) -> m.getParameter((int) i).clone().setType(JavaParser.parseType(t.describe())))
                        .collect(ImmutableList.toImmutableList());
                MethodDeclaration newDecl = m.clone();
                newDecl.accept(new PropagateTypeFork(createParameterMap(mParams, types)), ctx);
                newDecl.getParameters().clear();
                newDecl.getParameters().addAll(mParams);
                decl.getMembers().add(decl.getMembers().indexOf(m), newDecl);
            }

            m.remove();
        }

        private Map<String, String> createParameterMap(List<Parameter> mParams, List<Type> types) {
            return Streams.zip(mParams.stream().map(Parameter::getNameAsString),
                    types.stream().map(Type::describe),
                    Maps::immutableEntry)
                    .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
        }

    }

}
