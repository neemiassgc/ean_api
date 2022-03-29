package com.xyz.ean.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

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
}
