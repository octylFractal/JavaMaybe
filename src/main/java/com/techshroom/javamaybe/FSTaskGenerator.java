package com.techshroom.javamaybe;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParseStart;
import com.github.javaparser.Providers;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;

public class FSTaskGenerator implements TaskGenerator {

    private final Path input;
    private final Path output;
    private final JavaParser parser;

    @Inject
    FSTaskGenerator(@Input Path input, @Output Path output, JavaParser parser) {
        this.input = input;
        this.output = output;
        this.parser = parser;
    }

    @Override
    public Iterable<Task> generateTasks() {
        List<Path> paths;
        try (Stream<Path> files = Files.walk(input)) {
            paths = files.filter(p -> p.toString().endsWith(".java")).collect(ImmutableList.toImmutableList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return () -> new AbstractIterator<Task>() {

            private final Iterator<Path> iter = paths.iterator();

            @Override
            protected Task computeNext() {
                if (!iter.hasNext()) {
                    return endOfData();
                }
                Path next = iter.next();
                ParseResult<CompilationUnit> unit;
                try {
                    unit = parser.parse(ParseStart.COMPILATION_UNIT, Providers.provider(next));
                } catch (IOException e) {
                    throw new ParseProblemException(e);
                }
                if (!unit.isSuccessful()) {
                    throw new ParseProblemException(unit.getProblems());
                }
                String fileName = next.getFileName().toString();
                CompilationUnit cu = unit.getResult().get();
                String unitPath = cu.getPackageDeclaration().map(PackageDeclaration::getNameAsString)
                        .map(pkg -> pkg.replace('.', '/') + "/" + fileName).orElse(fileName);
                Path nextOut = output.resolve(unitPath);
                return Task.create(cu, next, nextOut);
            }
        };
    }

}
