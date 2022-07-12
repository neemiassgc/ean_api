package com.api.service;

import com.api.pojo.SessionInstance;
import com.api.projection.ProjectionFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.api.projection.Projection.PriceWithInstant;
import static com.api.projection.Projection.ProductWithLatestPrice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.*;

@SuppressWarnings("unchecked")
public class ProductExternalServiceTest {

    private RestTemplate restTemplateMock;
    private ObjectMapper objectMapperMock;
    private ProductExternalService productExternalServiceUnderTest;

    @BeforeEach
    void setUp() {
        this.restTemplateMock = mock(RestTemplate.class);

        final RestTemplateBuilder restTemplateBuilderMock = mock(RestTemplateBuilder.class);
        given(restTemplateBuilderMock.rootUri(anyString())).willReturn(restTemplateBuilderMock);
        given(restTemplateBuilderMock.requestFactory(any(Supplier.class))).willReturn(restTemplateBuilderMock);
        given(restTemplateBuilderMock.build()).willReturn(this.restTemplateMock);

        this.objectMapperMock = mock(ObjectMapper.class);
        this.productExternalServiceUnderTest = new ProductExternalService(restTemplateBuilderMock, this.objectMapperMock);
    }

    private void reusableSessionInstance() {
        given(this.restTemplateMock.execute(
            eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn(Jsoup.parse(this.getGenericHtmlContent()));

        given(this.restTemplateMock.postForEntity(eq("/wwv_flow.accept"), anyMap(), eq(String.class))).will(invocation -> null);

        given(this.restTemplateMock.execute(
            eq("/f?p=171:2:54321:NEXT:NO:2:P2_CURSOR:B"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn("peanut butter");
    }

    private String getGenericHtmlContent() {
        final InputStreamReader isr = new InputStreamReader(
            Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("html_for_test.html"))
        );

        try (BufferedReader br = new BufferedReader(isr)) {
            return br.lines().collect(Collectors.joining("\n"));
        }
        catch (IOException ignored) {
            return "";
        }
    }

    @Test
    void if_the_structures_of_the_html_responses_are_intact_then_should_return_a_valid_session_instance__newSessionInstance() {
        // given
        this.reusableSessionInstance();

        // when
        final SessionInstance actualSessionInstance = this.productExternalServiceUnderTest.newSessionInstance();

        // then
        assertThat(actualSessionInstance).isNotNull();
        assertThat(actualSessionInstance).extracting("sessionId").isEqualTo("54321");
        assertThat(actualSessionInstance).extracting("ajaxIdentifier").isEqualTo("PLUGIN=peanut butter");

        verify(this.restTemplateMock, times(2)).execute(anyString(), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).postForEntity(anyString(), anyMap(), eq(String.class));
    }

    @Test
    void if_the_login_page_parsing_fails_then_should_throw_an_exception_newSessionInstance() {
        // given
        given(this.restTemplateMock.execute(
            eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn(null);

        // when
        final Throwable actualThrowable = catchThrowable(() -> this.productExternalServiceUnderTest.newSessionInstance());

        // then
        assertThat(actualThrowable).isInstanceOf(IllegalStateException.class);
        assertThat(actualThrowable).hasMessage("Login page parsing failed");

        verify(this.restTemplateMock, times(1)).execute(eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, only()).execute(eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, never()).postForEntity(anyString(), anyMap(), eq(String.class));
    }

    @Test
    void when_some_of_the_required_fields_are_missing_then_should_throw_an_exception__newSessionInstance(){
        // given
        given(this.restTemplateMock.execute(
            eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn(Jsoup.parse("<input id=\"pInstance\" value=\"54321\"/><input id=\"pPageSubmissionId\" value=\"898900\"/>"));

        // when
        final Throwable actualThrowable = catchThrowable(() -> this.productExternalServiceUnderTest.newSessionInstance());

        // then
        assertThat(actualThrowable).isNotNull();
        assertThat(actualThrowable).isInstanceOf(IllegalStateException.class);

        verify(this.restTemplateMock, times(1)).execute(eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, only()).execute(eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, never()).postForEntity(anyString(), anyMap(), eq(String.class));
    }

    @Test
    void when_ajaxIdentifier_is_not_found_then_should_throw_an_exception_loginRequest() {
        // given
        given(this.restTemplateMock.execute(
                eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn(Jsoup.parse(this.getGenericHtmlContent()));

        given(this.restTemplateMock.postForEntity(eq("/wwv_flow.accept"), anyMap(), eq(String.class))).will(invocation -> null);

        given(
            this.restTemplateMock.execute(
                eq("/f?p=171:2:54321:NEXT:NO:2:P2_CURSOR:B"),
                eq(HttpMethod.GET),
                isNull(),
                any(ResponseExtractor.class)
            )
        ).willThrow(new IllegalStateException("Ajax identifier not found"));

        // when
        final Throwable actualThrowable = catchThrowable(() -> this.productExternalServiceUnderTest.newSessionInstance());

        // then
        assertThat(actualThrowable).isNotNull();
        assertThat(actualThrowable).isInstanceOf(IllegalStateException.class);
        assertThat(actualThrowable).hasMessage("Ajax identifier not found");

        verify(this.restTemplateMock, times(1)).execute(eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).execute(eq("/f?p=171:2:54321:NEXT:NO:2:P2_CURSOR:B"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).postForEntity(eq("/wwv_flow.accept"), anyMap(), eq(String.class));
    }

    @Test
    void given_a_valid_barcode_then_should_return_a_projection_fetchByBarcode() throws JsonProcessingException {
        // given
        final String existingBarcode = "123456789101";

        final ProductWithLatestPrice expectedProjection = ProjectionFactory.productWithLatestPriceBuilder()
            .description("description")
            .barcode(existingBarcode)
            .latestPrice(new PriceWithInstant(new BigDecimal("16.40"), Instant.now()))
            .sequenceCode(123456)
            .build();

        given(this.restTemplateMock.httpEntityCallback(any(HttpEntity.class), eq(String.class))).willReturn(null);
        given(this.objectMapperMock.readValue(anyString(), eq(ProductWithLatestPrice.class)))
            .willReturn(expectedProjection);
        given(this.restTemplateMock.execute(
            eq("/wwv_flow.show"),
            eq(HttpMethod.POST),
            isNull(),
            any(ResponseExtractor.class)
        )).will(invocation ->
            invocation.getArgument(3, ResponseExtractor.class)
                .extractData(new MockClientHttpResponse(new byte[0], HttpStatus.OK))
        );

        // when
        final Optional<ProductWithLatestPrice> actualProjection =
            this.productExternalServiceUnderTest.fetchByBarcode(existingBarcode).map(p -> (ProductWithLatestPrice)p);

        // then
        assertThat(actualProjection).isNotNull();
        assertThat(actualProjection.orElse(null)).isNotNull();
        assertThat(actualProjection.get()).extracting("description").isEqualTo("description");
        assertThat(actualProjection.get()).extracting("latestPrice.value").isEqualTo(new BigDecimal("16.40"));
        assertThat(actualProjection.get()).extracting("barcode").isEqualTo("123456789101");

        verify(this.restTemplateMock, times(1)).execute(eq("/wwv_flow.show"), eq(HttpMethod.POST), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).httpEntityCallback(any(HttpEntity.class), eq(String.class));
        verify(this.objectMapperMock, times(1)).readValue(anyString(), eq(ProductWithLatestPrice.class));
    }

    @Test
    void given_a_non_existing_barcode_then_should_return_optional_empty_fetchByBarcode() throws JsonProcessingException {
        // given
        final String nonExistingBarcode = "1983471983474";

        given(this.restTemplateMock.httpEntityCallback(any(HttpEntity.class), eq(String.class))).willReturn(null);

        given(
            this.objectMapperMock.readValue(
                anyString(),
                eq(ProductWithLatestPrice.class)
            )
        ).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        given(this.restTemplateMock.execute(
            eq("/wwv_flow.show"),
            eq(HttpMethod.POST),
            isNull(),
            any(ResponseExtractor.class)
        )).will(invocation ->
            invocation.getArgument(3, ResponseExtractor.class)
                .extractData(new MockClientHttpResponse(new byte[0], HttpStatus.OK))
        );

        // when
        final Throwable throwable = catchThrowable(() -> this.productExternalServiceUnderTest.fetchByBarcode("123089173479802134"));

        // then
        assertThat(throwable).isNotNull();
        assertThat(throwable).isExactlyInstanceOf(ResponseStatusException.class);
        assertThat((ResponseStatusException) throwable).extracting("status").isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((ResponseStatusException) throwable).extracting("reason").isEqualTo("Product not found");

        verify(this.restTemplateMock, times(1)).execute(eq("/wwv_flow.show"), eq(HttpMethod.POST), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).httpEntityCallback(any(HttpEntity.class), eq(String.class));
        verify(this.objectMapperMock, times(1)).readValue(anyString(), eq(ProductWithLatestPrice.class));
    }

    @Test
    void when_a_session_instance_is_not_valid_then_should_recreate_it() throws JsonProcessingException {
       //given
        final ProductWithLatestPrice expectedProjection = ProjectionFactory.productWithLatestPriceBuilder()
            .description("description")
            .barcode("123456789101")
            .latestPrice(new PriceWithInstant(new BigDecimal("16.40"), Instant.now()))
            .sequenceCode(123456)
            .build();

        this.reusableSessionInstance();

        given(this.restTemplateMock.httpEntityCallback(any(HttpEntity.class), eq(String.class))).willReturn(null);

        given(this.objectMapperMock.readValue(anyString(), eq(ProductWithLatestPrice.class)))
            .willReturn(null)
            .willReturn(expectedProjection);

        given(this.restTemplateMock.execute(
            eq("/wwv_flow.show"),
            eq(HttpMethod.POST),
            isNull(),
            any(ResponseExtractor.class)
        )).will(invocation ->
            invocation.getArgument(3, ResponseExtractor.class)
                .extractData(new MockClientHttpResponse(new byte[0], HttpStatus.OK))
        );

        //when
        final Optional<ProductWithLatestPrice> actualDTO =
            this.productExternalServiceUnderTest.fetchByBarcode("it is not a barcode")
            .map(p -> (ProductWithLatestPrice)p);

        //then
        assertThat(actualDTO).isNotNull();
        assertThat(actualDTO.orElse(null)).isNotNull();
        assertThat(actualDTO.get()).extracting("description").isEqualTo("description");
        assertThat(actualDTO.get()).extracting("latestPrice.value").isEqualTo(new BigDecimal("16.40"));
        assertThat(actualDTO.get()).extracting("barcode").isEqualTo("123456789101");

        verify(this.restTemplateMock, times(2)).execute(eq("/wwv_flow.show"), eq(HttpMethod.POST), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(2)).httpEntityCallback(any(HttpEntity.class), eq(String.class));
        verify(this.objectMapperMock, times(2)).readValue(anyString(), eq(ProductWithLatestPrice.class));

        // reusableNewInstance
        verify(this.restTemplateMock, times(1)).execute(eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).execute(eq("/f?p=171:2:54321:NEXT:NO:2:P2_CURSOR:B"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).postForEntity(eq("/wwv_flow.accept"), anyMap(), eq(String.class));

    }
}
