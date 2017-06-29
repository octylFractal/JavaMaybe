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

import com.google.common.collect.ImmutableList;
import com.techshroom.javamaybe.compile.Any;

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
        System.err.println(copyOf((Any) Arrays.asList("1", "2", "3")));
        Iterable<String> iterable = Arrays.asList("a", "b", "c")::iterator;
        System.err.println(copyOf((Any) iterable));
        Iterator<String> iterator = Arrays.asList("that", "is", "how", "easy", "it", "is").iterator();
        System.err.println(copyOf((Any) iterator));
    }

    public static <E> ImmutableList<E> copyOf(Any source) {
        if (source.typeFork()) {
            return ImmutableList.copyOf(source.<Iterable<E>> as());
        } else if (source.typeFork()) {
            return ImmutableList.copyOf(source.<Iterator<E>> as());
        } else {
            return ImmutableList.copyOf(source.<Collection<E>> as());
        }
    }

    public int sporkInTheRoad(Any target) {
        if (target.typeFork()) {
            return target.<Number> as().intValue();
        } else if (target.typeFork()) {
            return target.<Integer> as();
        }
        return target.typeFork() ? target.<Fork> as().getProngs() : target.<Spoon> as().getCurvature();
    }

    public void numberToIL(Any nTIL) {
        // initially cast as Number, later specialized as Integer and Long
        // Here, the end parameters should be Integer and Long
        Number number = nTIL.as();
        long someLong = nTIL.typeFork() ? nTIL.<Integer> as() : nTIL.<Long> as();
        System.err.println(number + " == " + someLong);
    }

    public void numberToL(Any nTL) {
        // initially number, end as long
        Number number = nTL.<Number> as();
        long someLong = nTL.<Long> as();
        System.err.println(number + " == " + someLong);
    }

    public void numberToNL(Any nTNL) {
        // initially number, re-cast as number and long
        Number number = nTNL.<Number> as();
        long someLong = nTNL.typeFork() ? nTNL.<Number> as().longValue() : nTNL.<Long> as();
        System.err.println(number + " == " + someLong);
    }

    public BigInteger largeMod(Any number, Any divisor) {
        BigInteger numberInt = number.typeFork() ? BigInteger.valueOf(number.<Number> as().longValue())
                : new BigInteger(number.<String> as());
        BigInteger divInt = divisor.typeFork() ? BigInteger.valueOf(divisor.<Number> as().longValue())
                : new BigInteger(divisor.<String> as());
        return numberInt.mod(divInt);
    }

}
