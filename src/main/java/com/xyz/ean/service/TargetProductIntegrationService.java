package com.xyz.ean.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyz.ean.pojo.SessionInstance;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Service
public class TargetProductIntegrationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final EntityMapper entityMapper;

    private SessionInstance sessionInstance;

    @Autowired
    public TargetProductIntegrationService(final RestTemplateBuilder restTemplateBuilder, final ObjectMapper objectMapper, final EntityMapper entityMapper) {
        final Supplier<RestTemplate> restTemplateSupplier = () -> {
            final CloseableHttpClient httpClient =
                HttpClientBuilder.create()
                    .setRedirectStrategy(DefaultRedirectStrategy.INSTANCE)
                    .setDefaultCookieStore(new BasicCookieStore())
                    .build();

            return restTemplateBuilder
                .rootUri("https://apex.savegnago.com.br/apexmobile")
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
        };

        this.restTemplate = restTemplateSupplier.get();
        this.objectMapper = objectMapper;
        this.entityMapper = entityMapper;
    }
}
