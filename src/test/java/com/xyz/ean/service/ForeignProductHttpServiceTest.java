package com.xyz.ean.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyz.ean.pojo.SessionInstance;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class ForeignProductHttpServiceTest {

    private RestTemplate restTemplateMock;
    private ObjectMapper objectMapperMock;
    private ForeignProductHttpService foreignProductHttpServiceUnderTest;

    @BeforeEach
    void setUp() {
        this.restTemplateMock = mock(RestTemplate.class);

        final RestTemplateBuilder restTemplateBuilderMock = mock(RestTemplateBuilder.class);
        given(restTemplateBuilderMock.rootUri(anyString())).willReturn(restTemplateBuilderMock);
        given(restTemplateBuilderMock.requestFactory(any(Supplier.class))).willReturn(restTemplateBuilderMock);
        given(restTemplateBuilderMock.build()).willReturn(this.restTemplateMock);

        this.objectMapperMock = mock(ObjectMapper.class);
        this.foreignProductHttpServiceUnderTest = new ForeignProductHttpService(restTemplateBuilderMock, this.objectMapperMock);
    }

    @Test
    void ifTheStructuresOfTheHTMLResponsesAreIntactShouldReturnAValidSessionInstance() throws IOException {
        // given
        final InputStreamReader isr = new InputStreamReader(
            Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("html_for_test.html"))
        );

        try (BufferedReader br = new BufferedReader(isr)) {
            given(this.restTemplateMock.execute(
                eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
            )).willReturn(Jsoup.parse(br.lines().collect(Collectors.joining())));

            given(this.restTemplateMock.postForEntity(eq("/wwv_flow.accept"), anyMap(), eq(String.class))).will(invocation -> null);

            given(this.restTemplateMock.execute(
                eq("/f?p=171:2:54321:NEXT:NO:2:P2_CURSOR:B"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
            )).willReturn("peanut butter");
        }

        // when
        final SessionInstance actualSessionInstance = this.foreignProductHttpServiceUnderTest.getASessionInstance();

        // then
        assertThat(actualSessionInstance).isNotNull();
        assertThat(actualSessionInstance).extracting("sessionId").isEqualTo("54321");
        assertThat(actualSessionInstance).extracting("ajaxIdentifier").isEqualTo("PLUGIN=peanut butter");

        verify(this.restTemplateMock, times(2)).execute(anyString(), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).postForEntity(anyString(), anyMap(), eq(String.class));
    }
}
