package com.techshroom.javamaybe;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.javaparser.symbolsolver.model.typesystem.Type;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.techshroom.javamaybe.compile.Any;

/**
 * A fork due to {@link Any#typeFork()}. Stores the two paths that result from
 * the call.
 */
@AutoValue
public abstract class TypeFork {

    public static TypeFork create(UnifiedConditional conditional, TypeForkPath primaryPath,
            @Nullable TypeForkPath secondaryPath) {
        return new AutoValue_TypeFork(conditional, primaryPath, Optional.ofNullable(secondaryPath));
    }

    TypeFork() {
    }

    public abstract UnifiedConditional getConditional();

    /**
     * The "then" part of a type fork.
     */
    public abstract TypeForkPath getPrimaryPath();
    
    public final Set<Type> getPrimaryTypes(String parameter) {
        return getPrimaryPath().resolveTypesOfParameter(parameter);
    }

    /**
     * The "else" part of a type fork. Not required.
     */
    public abstract Optional<TypeForkPath> getSecondaryPath();
    
    public final Set<Type> getSecondaryTypes(String parameter) {
        return getSecondaryPath().map(p -> p.resolveTypesOfParameter(parameter)).orElse(ImmutableSet.of());
    }

}
