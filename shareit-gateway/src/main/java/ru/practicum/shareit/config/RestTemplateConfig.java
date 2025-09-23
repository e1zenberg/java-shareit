package ru.practicum.shareit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestTemplateConfig {

    @Value("${shareit-server.url}")
    private String serverUrl;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(serverUrl);
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        return builder.uriTemplateHandler(uriFactory).build();
    }
}
