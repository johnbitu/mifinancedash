package dev.project.finance.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter(
            @Value("${security.cors.allowed-origins}") String allowedOrigins,
            @Value("${security.cors.allowed-methods}") String allowedMethods,
            @Value("${security.cors.allowed-headers}") String allowedHeaders
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(toList(allowedOrigins));
        configuration.setAllowedMethods(toList(allowedMethods));
        configuration.setAllowedHeaders(toList(allowedHeaders));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }

    private List<String> toList(String values) {
        return Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }
}
