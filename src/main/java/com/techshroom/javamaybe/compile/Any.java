package com.techshroom.javamaybe.compile;

public interface Any {

    static Any wrap(Object object) {
        return null;
    }

    // fake fork!
    boolean typeFork();

    // uses type inference, usable with variables
    <T> T as();

    // uses stricter type inference, for in-line cases
    <T> T as(Class<T> type);

    // for sticking the type in a constant somewhere
    // esp. if long like ImmutableMultimap<String, String>, can be shortened to
    // just "MAP"
    <T> T as(TypeCap<T> type);

}
