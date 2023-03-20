package com.api.configuration;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.service.CacheManager;
import com.api.service.interfaces.PriceService;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.time.Duration;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

@Configuration
public class BeansConfiguration {

    @Bean
    public JavaMailSender javaMailSender() {
        final Properties props = new Properties();
        final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.transport.protocol", "smtp");

        final Session session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(System.getenv("MAIL_USERNAME"), System.getenv("MAIL_PASSWORD"));
            }
        });

        javaMailSender.setHost("smtp.gmail.com");
        javaMailSender.setUsername(System.getenv("MAIL_USERNAME"));
        javaMailSender.setPassword(System.getenv("MAIL_PASSWORD"));
        javaMailSender.setSession(session);

        return javaMailSender;
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.US);
    }

    @Bean
    public CookieStore cookieStore() {
        return new BasicCookieStore();
    }

    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        final int twoSeconds = (int) Duration.ofSeconds(2).toMillis();

        final RequestConfig requestConfig = RequestConfig
            .custom()
            .setConnectionRequestTimeout(twoSeconds)
            .setConnectTimeout(twoSeconds)
            .build();

        final CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(requestConfig)
            .setRedirectStrategy(DefaultRedirectStrategy.INSTANCE)
            .setDefaultCookieStore(cookieStore())
            .build();

        return restTemplateBuilder
            .rootUri("https://apex.savegnago.com.br/apexmobile")
            .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
            .build();
    }

    @Bean
    public CacheManager<Product, UUID> productCacheManager() {
        return new CacheManager<>(Product::getId);
    }

    @Bean
    public CacheManager<Price, UUID> priceCacheManager() {
        return new CacheManager<>(Price::getId);
    }
}