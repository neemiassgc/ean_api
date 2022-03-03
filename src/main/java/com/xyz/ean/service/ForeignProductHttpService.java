package com.xyz.ean.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyz.ean.entity.Product;
import com.xyz.ean.pojo.DomainUtils;
import com.xyz.ean.pojo.SessionInstance;
import com.xyz.ean.pojo.dto.DomainResponse;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        this.createAnInstance();
    }
}
