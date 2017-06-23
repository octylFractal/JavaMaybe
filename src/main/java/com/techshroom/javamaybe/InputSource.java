package com.techshroom.javamaybe;

import java.nio.file.Path;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

public interface InputSource {

    Path provideInputFile();

    Path provideOutputDirectory();

    TypeSolver provideTypeSolver();

}
