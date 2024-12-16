package com.certidevs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient manufacturerClient() {
        return WebClient.create("https://api.manufacturers.com");
    }

    @Bean
    public WebClient ratingClient() {
        return WebClient.create("https://api.ratings.com");
    }
}
