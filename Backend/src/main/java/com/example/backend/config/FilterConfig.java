package com.example.backend.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter jwtAuthFilter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setName("jwtAuthFilter");
        return registration;
    }
}
