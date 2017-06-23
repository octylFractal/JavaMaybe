package com.techshroom.javamaybe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import com.techshroom.javamaybe.compile.Any;

/**
 * A path in a fork. There is one encompassing the entire method. Each path can
 * have more forks in it, with more types. Each path can resolve all of the
 * available types in each fork to produce a single set of needed types.
 */
public final class TypeForkPath {

    public static TypeForkPath construct(JavaParserFacade typeSolver, MethodDeclaration source) {
        return new TypeForkPath(null, typeSolver,
                source.getParameters().stream()
                        .map(Parameter::getNameAsString)
                        .collect(ImmutableList.toImmutableList()),
                source.getParameters().stream()
                        .filter(p -> {
                            Type t = typeSolver.getType(p);
                            return t.isReferenceType()
                                    && t.asReferenceType().getQualifiedName().equals(Any.class.getName());
                        })
                        .map(Parameter::getNameAsString)
                        .collect(ImmutableSet.toImmutableSet()));
    }

    @Nullable
    private final TypeForkPath parent;
    private final JavaParserFacade typeSolver;
    private final ImmutableList<String> parameters;
    private final ImmutableSet<String> anyParams;
    private final Map<String, Type> parameterTypes = new HashMap<>();
    private final List<TypeFork> forks = new ArrayList<>();

    private TypeForkPath(@Nullable TypeForkPath parent, JavaParserFacade typeSolver, Collection<String> parameters,
            Collection<String> anyParams) {
        this.parent = parent;
        this.typeSolver = typeSolver;
        this.parameters = ImmutableList.copyOf(parameters);
        this.anyParams = ImmutableSet.copyOf(anyParams);
    }

    public JavaParserFacade getTypeSolver() {
        return typeSolver;
    }

    public ImmutableList<String> getParameters() {
        return parameters;
    }

    public ImmutableSet<String> getAnyParams() {
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
            if (existing == null) {
                return type;
            }
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
        return new TypeForkPath(this, typeSolver, parameters, anyParams);
    }

}
