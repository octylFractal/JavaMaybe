package com.techshroom.javamaybe;

import java.nio.file.Path;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.google.inject.AbstractModule;

public class InputModule extends AbstractModule {

    private final InputSource source;

    public InputModule(InputSource source) {
        this.source = source;
    }

    @Override
    protected void configure() {
        bind(Path.class)
                .annotatedWith(Input.class)
                .toInstance(source.provideInputFile());
        bind(Path.class)
                .annotatedWith(Output.class)
                .toInstance(source.provideOutputDirectory());
        bind(TypeSolver.class).toInstance(source.provideTypeSolver());
    }

}
