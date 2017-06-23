package com.techshroom.javamaybe;

import java.util.concurrent.CompletableFuture;

import com.github.javaparser.ast.CompilationUnit;

public interface TaskProcessor {

    CompletableFuture<CompilationUnit> process(Task task);

}
