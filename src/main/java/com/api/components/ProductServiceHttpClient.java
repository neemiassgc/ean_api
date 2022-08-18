package com.api.components;

import com.api.service.interfaces.SessionStorageService;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class ProductServiceHttpClient {

    private final RestTemplate restTemplate;
    private final CookieStore cookieStore;

    @Autowired
    ProductServiceHttpClient(
        final SessionStorageService sessionStorageService,
        final RestTemplate restTemplate,
        final RestTemplateBuilder restTemplateBuilder
    ) {
        cookieStore = new BasicCookieStore();

        final int twoSeconds = (int) Duration.ofSeconds(2).toMillis();
        final RequestConfig requestConfig = RequestConfig
            .custom()
            .setConnectionRequestTimeout(twoSeconds)
            .setConnectTimeout(twoSeconds)
            .build();

        final CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(requestConfig)
            .setRedirectStrategy(DefaultRedirectStrategy.INSTANCE)
            .setDefaultCookieStore(cookieStore)
            .build();

        this.restTemplate = restTemplateBuilder
            .rootUri("https://apex.savegnago.com.br/apexmobile")
            .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
            .build();
    }
}
