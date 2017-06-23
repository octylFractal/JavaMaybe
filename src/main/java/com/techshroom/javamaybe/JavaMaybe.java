package com.techshroom.javamaybe;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class JavaMaybe {

    public static void main(String[] args) throws Exception {
        main(new OptionInputSource(args));
    }

    public static void main(InputSource inputSource) {
        Injector injector = Guice.createInjector(new JMModule(), new InputModule(inputSource));

        TaskProcessor processor = injector.getInstance(TaskProcessor.class);
        PrettyPrinter printer = new PrettyPrinter(new PrettyPrinterConfiguration()
                .setPrintComments(false));

        for (Task task : injector.getInstance(TaskGenerator.class).generateTasks()) {
            processor.process(task).thenAccept(cu -> {
                Path taskOut = task.getOutput();
                try {
                    Files.createDirectories(taskOut.getParent());
                    try (Writer writer = Files.newBufferedWriter(taskOut)) {
                        writer.write(printer.print(cu));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
