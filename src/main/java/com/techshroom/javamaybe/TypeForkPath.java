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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.typesystem.Type;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * A path in a fork. There is one encompassing the entire method. Each path can
 * have more forks in it, with more types. Each path can resolve all of the
 * available types in each fork to produce a single set of needed types.
 */
public final class TypeForkPath {

    public static TypeForkPath construct(JavaParserFacade typeSolver, MethodDeclaration source) {
        return new TypeForkPath(null, typeSolver, source.getParameters());
    }

    @Nullable
    private final TypeForkPath parent;
    private final JavaParserFacade typeSolver;
    private final Collection<Parameter> originalParameters;
    private final ImmutableList<String> parameters;
    private final Set<String> anyParams = new HashSet<>();
    private final Map<String, Type> parameterTypes = new HashMap<>();
    private final List<TypeFork> forks = new ArrayList<>();

    private TypeForkPath(@Nullable TypeForkPath parent, JavaParserFacade typeSolver, Collection<Parameter> parameters) {
        this.parent = parent;
        this.typeSolver = typeSolver;
        this.originalParameters = parameters;
        this.parameters = parameters.stream().map(Parameter::getNameAsString).collect(ImmutableList.toImmutableList());
        parameters.forEach(p -> {
            parameterTypes.put(p.getNameAsString(), typeSolver.getType(p));
        });
    }

    public JavaParserFacade getTypeSolver() {
        return typeSolver;
    }

    public ImmutableList<String> getParameters() {
        return parameters;
    }

    public Set<String> getAnyParams() {
        return anyParams;
    }

    public Set<Type> resolveTypesOfParameter(String parameter) {
        return doResolveTop(parameter);
    }

    private Set<Type> doResolveTop(String parameter) {
        // the type in the path, the most focused as-call
        Type typeInPath = parameterTypes.get(parameter);
        // the TOPs from subpaths
        Set<Type> subTypes = forks.stream()
                .flatMap(f -> Stream.concat(f.getPrimaryPath().doResolveTop(parameter).stream(),
                        f.getSecondaryPath().map(p -> p.doResolveTop(parameter)).orElse(ImmutableSet.of())
                                .stream()))
                .collect(ImmutableSet.toImmutableSet());

        // verify that sub types are assignable from current path
        if (typeInPath != null) {
            for (Type type : subTypes) {
                if (!(typeInPath.isAssignableBy(type))) {
                    throw new IllegalStateException(type.describe() + " is not a subtype of " + typeInPath.describe());
                }
            }
        }

        // return the sub-types list, unless it is empty, then return our type
        if (subTypes.isEmpty()) {
            return typeInPath == null ? ImmutableSet.of() : ImmutableSet.of(typeInPath);
        } else {
            return subTypes;
        }
    }

    public void putType(String parameter, Type type) {
        Type parentParams = parent == null ? null : parent.getParameterTypes().get(parameter);
        if (parentParams != null && !parentParams.isAssignableBy(type)) {
            // has parent type that is conflicting
            throw new IllegalStateException(
                    type.describe() + " is not a subtype of parent type " + parentParams.describe());
        }
        parameterTypes.compute(parameter, (k, existing) -> {
            if (existing.isAssignableBy(type)) {
                return type;
            }
            throw new IllegalStateException(type.describe() + " is not a subtype of " + existing.describe());
        });
    }

    public Map<String, Type> getParameterTypes() {
        return parameterTypes;
    }

    public List<TypeFork> getForks() {
        return forks;
    }

    public TypeForkPath createSubPath() {
        return new TypeForkPath(this, typeSolver, originalParameters);
    }

}
