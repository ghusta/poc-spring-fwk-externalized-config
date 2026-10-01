package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:some-inner.properties")
public class ApplicationConfig {

    @Bean("randomBean")
    Object randomBean() {
        return "random bean";
    }

}
