package org.example.config.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
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

    public static final String ACTIVE_PROFILES = AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME;

    public static final String CONFIG_LOCATION = "spring.config.location";

    public static final String ADDITIONAL_LOCATION = "spring.config.additional-location";

    public static final String CONFIG_NAME = "spring.config.name";

    private ApplicationConfigLoader() {
    }

    public static void load(ConfigurableEnvironment environment) {

        List<String> locations = new ArrayList<>();

        /*
         * Explicit locations have the highest priority.
         */
        addLocations(locations, environment.getProperty(CONFIG_LOCATION));

        /*
         * Additional locations have higher priority.
         */
        addLocations(locations, environment.getProperty(ADDITIONAL_LOCATION));

        /*
         * Default locations.
         */
        locations.add(FILE_URL_PREFIX + "./");
        locations.add(FILE_URL_PREFIX + "./config/");
        locations.add(CLASSPATH_URL_PREFIX + "/");
        locations.add(CLASSPATH_URL_PREFIX + "/config/");

        MutablePropertySources configDataFiles = new MutablePropertySources();

        /*
         * Load in increasing priority.
         */
        for (String location : locations) {
            loadLocation(environment, configDataFiles, location);
        }

        // config PropertySources added in order after existing PropertySources, like those in StandardEnvironment
        configDataFiles.stream()
                .forEach(propertySource -> environment.getPropertySources().addLast(propertySource));
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
            MutablePropertySources configDataFiles,
            String location) {

        boolean optional = false;

        if (location.startsWith("optional:")) {
            optional = true;
            location = location.substring("optional:".length());
        }

        String baseLocation = ensureTrailingSlash(location);

        String configName = environment.getProperty(CONFIG_NAME, "application");

        /*
         * application-{profile}.properties (higher priority)
         */
        for (String profile : environment.getActiveProfiles()) {
            loadResource(configDataFiles, baseLocation + configName + "-" + profile + ".properties", optional, true);
        }

        /*
         * application.properties
         */
        loadResource(configDataFiles, baseLocation + configName + ".properties", optional, false);
    }

    private static String ensureTrailingSlash(String location) {
        return location.endsWith("/")
                ? location
                : location + "/";
    }

    private static void loadResource(
            MutablePropertySources propertySources,
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

            PropertySource<?> resourcePropertySource = new ResourcePropertySource(resource);

            if (profileSpecific) {
                propertySources.addLast(resourcePropertySource);
            } else {
                propertySources.addLast(resourcePropertySource);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load configuration: " + location, e);
        }
    }
}