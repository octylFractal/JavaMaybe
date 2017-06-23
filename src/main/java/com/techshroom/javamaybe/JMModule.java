package com.techshroom.javamaybe;

import javax.inject.Singleton;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.validator.Java8Validator;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class JMModule extends AbstractModule {

    @Override
    public void configure() {
        bind(TaskProcessor.class).to(SyncTaskProcessor.class);
        bind(TaskGenerator.class).to(FSTaskGenerator.class);
    }

    @Provides
    @Singleton
    protected JavaParser provideParser() {
        return new JavaParser(new ParserConfiguration().setValidator(new Java8Validator()));
    }

}
