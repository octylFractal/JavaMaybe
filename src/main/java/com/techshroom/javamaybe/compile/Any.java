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
package com.techshroom.javamaybe.compile;

public interface Any {

    // uses type inference, usable with variables
    static <T> T convert(Object in) {
        return null;
    }

    // uses type inference, usable with variables
    // this form usually is used as Any.<Number>from(var)
    // this reads as "number from var"
    static <T> T from(Object in) {
        return null;
    }

    // uses stricter type inference, for in-line cases
    static <T> T to(Class<T> type, Object in) {
        return null;
    }

    // for sticking the type in a constant somewhere
    // esp. if long like ImmutableMultimap<String, String>, can be shortened to
    // just "MAP"
    static <T> T to(TypeCap<T> type, Object in) {
        return null;
    }

    static boolean typeFork(Object varTarget) {
        return false;
    }

}
