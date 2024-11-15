package com.zero.bwtableback.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig {
    // RESTful 웹 서비스를 호출하고 그 결과를 처리하는 데 사용
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
