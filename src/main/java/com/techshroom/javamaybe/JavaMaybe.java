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
