package org.example.config.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Tries to mimic Spring Boot's external application properties loading.
 * <p>
 * See details <a href="https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files">here</a>.
 * </p>
 *
 * @see PropertySource
 */
public final class ApplicationConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ApplicationConfigLoader.class);

    private static final String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;

    private static final String FILE_URL_PREFIX = ResourceUtils.FILE_URL_PREFIX;

    public static final String ACTIVE_PROFILES = "spring.profiles.active";

    public static final String CONFIG_LOCATION = "spring.config.location";

    public static final String ADDITIONAL_LOCATION = "spring.config.additional-location";

    public static final String CONFIG_NAME = "spring.config.name";

    private ApplicationConfigLoader() {
    }

    public static void load(ConfigurableEnvironment environment) {

        List<String> locations = new ArrayList<>();

        /*
         * Default locations.
         */
        locations.add(CLASSPATH_URL_PREFIX + "/");

        locations.add(CLASSPATH_URL_PREFIX + "/config/");

        locations.add(FILE_URL_PREFIX + "./");

        locations.add(FILE_URL_PREFIX + "./config/");

        /*
         * Additional locations have higher priority.
         */
        addLocations(
                locations,
                environment.getProperty(ADDITIONAL_LOCATION)
        );

        /*
         * Explicit locations have the highest priority.
         */
        addLocations(
                locations,
                environment.getProperty(CONFIG_LOCATION)
        );

        /*
         * Load in increasing priority.
         */
        for (String location : locations) {
            loadLocation(environment, location);
        }
    }

    private static void addLocations(
            List<String> locations,
            String value) {

        if (value == null || value.isBlank()) {
            return;
        }

        Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(locations::add);
    }

    private static void loadLocation(
            ConfigurableEnvironment environment,
            String location) {

        boolean optional = false;

        if (location.startsWith("optional:")) {
            optional = true;
            location = location.substring("optional:".length());
        }

        String baseLocation = ensureTrailingSlash(location);

        String configName = environment.getProperty(CONFIG_NAME, "application");

        /*
         * application.properties
         */
        loadResource(environment, baseLocation + configName + ".properties", optional, false);

        /*
         * application-{profile}.properties
         */
        for (String profile : environment.getActiveProfiles()) {
            loadResource(environment, baseLocation + configName + "-" + profile + ".properties", optional, true);
        }
    }

    private static String ensureTrailingSlash(String location) {
        return location.endsWith("/")
                ? location
                : location + "/";
    }

    private static void loadResource(
            ConfigurableEnvironment environment,
            String location,
            boolean optional,
            boolean profileSpecific) {

        Resource resource = new DefaultResourceLoader().getResource(location);

        if (!resource.exists()) {

            if (!optional) {
                /*
                 * Default locations are allowed not to exist.
                 * Explicit locations could alternatively be made
                 * mandatory here.
                 */
            }

            return;
        }

        try {
            log.debug("Loading properties from resource {} ({})", resource, resource.getURI());
            Properties properties = PropertiesLoaderUtils.loadProperties(resource);
            log.debug("Found {} properties in {}", properties.size(), resource);

            // PropertySource<?> propertySource = new PropertiesPropertySource(location, properties);
            PropertySource<?> resourcePropertySource = new ResourcePropertySource(resource);

            if (profileSpecific) {
                environment.getPropertySources().addFirst(/*propertySource*/ resourcePropertySource);
            } else {
                environment.getPropertySources().addLast(/*propertySource*/ resourcePropertySource);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load configuration: " + location, e);
        }
    }
}