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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.techshroom.javamaybe.compile.Any;
import com.techshroom.javamaybe.compile.TypeCap;

public class TestClass {

    private static final class Fork {

        public int getProngs() {
            return 1;
        }

    }

    private static final class Spoon {

        public int getCurvature() {
            return 1;
        }

    }

    public static void main(String[] args) {
        System.err.println(copyOf(Arrays.asList("1", "2", "3")));
        Iterable<String> iterable = Arrays.asList("a", "b", "c")::iterator;
        System.err.println(copyOf(iterable));
        Iterator<String> iterator = Arrays.asList("that", "is", "how", "easy", "it", "is").iterator();
        System.err.println(copyOf(iterator));
        System.err.println(new TestClass().sporkInTheRoad(new Fork()));
    }

    public static <E> ImmutableList<E> copyOf(Object source) {
        if (Any.typeFork(source)) {
            return ImmutableList.copyOf(Any.<Iterable<E>> from(source));
        } else if (Any.typeFork(source)) {
            return ImmutableList.copyOf(Any.<Iterator<E>> from(source));
        } else {
            return ImmutableList.copyOf(Any.<Collection<E>> from(source));
        }
    }

    public int sporkInTheRoad(Object target) {
        if (Any.typeFork(target)) {
            return Any.<Number> from(target).intValue();
        } else if (Any.typeFork(target)) {
            return Any.<Integer> from(target);
        }
        return Any.typeFork(target) ? Any.<Fork> from(target).getProngs() : Any.<Spoon> from(target).getCurvature();
    }

    public void numberToIL(Object nTIL) {
        // initially cast as Number, later specialized as Integer and Long
        // Here, the end parameters should be Integer and Long
        Number number = Any.convert(nTIL);
        long someLong = Any.typeFork(nTIL) ? Any.<Integer> from(nTIL) : Any.<Long> from(nTIL);
        System.err.println(number + " == " + someLong);
    }

    public void numberToL(Object nTL) {
        // initially number, end as long
        Number number = Any.convert(nTL);
        long someLong = Any.convert(nTL);
        System.err.println(number + " == " + someLong);
    }

    public void numberToNL(Object nTNL) {
        // initially number, re-cast as number and long
        Number number = Any.convert(nTNL);
        long someLong = Any.typeFork(nTNL) ? Any.<Number> from(nTNL).longValue() : Any.<Long> from(nTNL);
        System.err.println(number + " == " + someLong);
    }

    public BigInteger largeMod(Object number, Object divisor) {
        BigInteger numberInt = getBigInteger(number);
        BigInteger divInt = getBigInteger(divisor);
        return numberInt.mod(divInt);
    }

    private static final BigInteger getBigInteger(Object number) {
        return Any.typeFork(number) ? BigInteger.valueOf(Any.<Number> from(number).longValue())
                : new BigInteger(Any.<String> from(number));
    }

    private static final TypeCap<Map<BigInteger, BigInteger>> MOD_MAP_TYPE = new TypeCap<>();

    public BigInteger largeModCached(Object cache) {
        if (Any.typeFork(cache)) {
            return Any.<Map<String, BigInteger>> from(cache).get("1");
        }
        return Any.to(MOD_MAP_TYPE, cache).get(BigInteger.ONE);
    }

}
