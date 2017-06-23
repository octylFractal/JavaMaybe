package com.techshroom.javamaybe;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;

/**
 * Input source from {@code String[] args}.
 */
public class OptionInputSource implements InputSource {

    private static final OptionParser PARSER = new OptionParser();
    private static final OptionSpec<Path> INPUT_PATH = PARSER
            .acceptsAll(Arrays.asList("i", "input"), "The input file/directory.").withRequiredArg()
            .withValuesConvertedBy(new PathConverter(PathProperties.READABLE)).defaultsTo(Paths.get("."));
    private static final OptionSpec<Path> OUTPUT_DIRECTORY = PARSER
            .acceptsAll(Arrays.asList("o", "output-dir"), "The output directory.").withRequiredArg()
            .withValuesConvertedBy(new PathConverter(PathProperties.DIRECTORY_EXISTING)).defaultsTo(Paths.get("./out"));
    private static final OptionSpec<Path> SOURCEPATH = PARSER
            .acceptsAll(Arrays.asList("sourcepath"),
                    "The source path that contains Java files with referenced classes.")
            .withOptionalArg()
            .withValuesConvertedBy(new PathConverter(PathProperties.DIRECTORY_EXISTING))
            .withValuesSeparatedBy(File.pathSeparator);
    private static final OptionSpec<Path> CLASSPATH = PARSER
            .acceptsAll(Arrays.asList("classpath"), "The class path that contains JARs with referenced classes.")
            .withOptionalArg()
            .withValuesConvertedBy(new PathConverter(PathProperties.FILE_EXISTING))
            .withValuesSeparatedBy(File.pathSeparator);

    private final Path inputFile;
    private final Path outputDirectory;
    private final TypeSolver typeSolver;

    public OptionInputSource(String[] args) {
        OptionSet opts = PARSER.parse(args);

        inputFile = opts.valueOf(INPUT_PATH);
        outputDirectory = opts.valueOf(OUTPUT_DIRECTORY);

        List<Path> sources = opts.valuesOf(SOURCEPATH);
        List<Path> classes = opts.valuesOf(CLASSPATH);
        CombinedTypeSolver cts = new CombinedTypeSolver();
        cts.add(new ReflectionTypeSolver());
        sources.forEach(p -> cts.add(new JavaParserTypeSolver(p.toFile())));
        classes.forEach(p -> {
            try {
                cts.add(new JarTypeSolver(p.toString()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        typeSolver = cts;
    }

    @Override
    public Path provideInputFile() {
        return inputFile;
    }

    @Override
    public Path provideOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public TypeSolver provideTypeSolver() {
        return typeSolver;
    }

}
