package com.techshroom.javamaybe;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

        unit.accept(new MethodSplitterVisitor(typeSolver), null);

        return CompletableFuture.completedFuture(unit);
    }

    private static final class MethodSplitterVisitor extends ModifierVisitor<Void> {

        private final JavaParserFacade typeSolver;

        public MethodSplitterVisitor(JavaParserFacade typeSolver) {
            this.typeSolver = typeSolver;
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
