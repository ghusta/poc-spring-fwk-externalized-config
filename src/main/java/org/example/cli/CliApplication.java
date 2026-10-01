package org.example.cli;

import org.example.config.ApplicationConfig;
import org.example.context.ProfilePropertySourceInitializer;
import org.example.context.ProfilesLoggerInitializer;
import org.example.context.PropertySourcesLoggerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;
import java.util.Objects;

public class CliApplication {

    private static final Logger log = LoggerFactory.getLogger(CliApplication.class);

    public static void main(String[] args) {
        log.debug("Application started");
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        initializeContextInitializers(applicationContext);

        applicationContext.register(ApplicationConfig.class);
        applicationContext.refresh();

        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        // can use applicationContext (getBean, etc.)
        Object randomBean = applicationContext.getBean("randomBean");
        Objects.requireNonNull(randomBean, "randomBean is null");

        // get a property
        String prop1 = environment.getProperty("business.prop1");
        Objects.requireNonNull(prop1, "business prop1 is null");

        applicationContext.close();
    }

    private static void initializeContextInitializers(ConfigurableApplicationContext applicationContext) {
        Arrays.stream(getRootApplicationContextInitializers())
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .forEach(i -> i.initialize(applicationContext));
    }

    private static ApplicationContextInitializer<ConfigurableApplicationContext>[] getRootApplicationContextInitializers() {
        return new ApplicationContextInitializer[]{
                new ProfilePropertySourceInitializer(),
                new ProfilesLoggerInitializer(),
                new PropertySourcesLoggerInitializer()
        };
    }

}