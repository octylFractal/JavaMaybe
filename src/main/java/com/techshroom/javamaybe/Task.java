package com.techshroom.javamaybe;

import java.nio.file.Path;

import com.github.javaparser.ast.CompilationUnit;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Task {

    public static Task create(CompilationUnit unit, Path source, Path output) {
        return new AutoValue_Task(unit, source, output);
    }

    Task() {
    }

    public abstract CompilationUnit getUnit();

    public abstract Path getSource();

    public abstract Path getOutput();

}
