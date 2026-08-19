package com.sorokaandriy.order_service.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient productRestClient(@Value("${app.product-service-url}") String productServiceUrl) {
        return RestClient.builder()
                .baseUrl(productServiceUrl)
                .build();
    }
}
