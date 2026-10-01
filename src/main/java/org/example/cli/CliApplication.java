package org.example.cli;

import org.example.config.ApplicationConfig;
import org.example.context.ProfilesLoggerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Objects;

public class CliApplication {

    private static final Logger log = LoggerFactory.getLogger(CliApplication.class);

    public static void main(String[] args) {
        log.debug("Application started");
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        for (ApplicationContextInitializer<ConfigurableApplicationContext> i : getRootApplicationContextInitializers()) {
            // TODO: respecter @Order
            i.initialize(applicationContext);
        }

        applicationContext.register(ApplicationConfig.class);
        applicationContext.refresh();

        // can use applicationContext (getBean, etc.)
        Object randomBean = applicationContext.getBean("randomBean");
        Objects.requireNonNull(randomBean, "randomBean is null");

        applicationContext.close();
    }

    private static ApplicationContextInitializer<ConfigurableApplicationContext>[] getRootApplicationContextInitializers() {
        return new ApplicationContextInitializer[]{
                new ProfilesLoggerInitializer()
        };
    }

}