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
