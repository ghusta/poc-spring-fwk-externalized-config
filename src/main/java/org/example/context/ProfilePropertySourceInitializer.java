package org.example.context;

import org.example.config.loader.ApplicationConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

@Order(ProfilePropertySourceInitializer.ORDER)
public class ProfilePropertySourceInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(ProfilePropertySourceInitializer.class);

    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 2000;

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        log.debug("Searching and loading application config files from ApplicationConfigLoader");
        ConfigurableEnvironment environment = context.getEnvironment();
        ApplicationConfigLoader.load(environment);
    }

}
