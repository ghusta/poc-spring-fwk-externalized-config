package org.example.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

@Order(Ordered.LOWEST_PRECEDENCE)
public class PropertySourcesLoggerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(PropertySourcesLoggerInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();

        log.debug("Current ConfigurableEnvironment is of type: {}", environment.getClass().getSimpleName());
        log.debug("Listing the ConfigurableEnvironment's propertySources (@PropertySources not displayed here as they are processed later)");
        int i = 0;
        for (PropertySource<?> propertySource : propertySources) {
            String strIdx = String.format("%2s", i + 1);
            log.debug("[{}] {} ({})", strIdx, propertySource.getName(), propertySource.getClass());
            i++;
        }
    }

}
