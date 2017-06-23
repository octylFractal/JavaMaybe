package com.techshroom.javamaybe.compile;

public interface ConsumerEx<I, E extends Exception> {

    void consume(I in) throws E;

}
