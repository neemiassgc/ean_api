package com.api.service;

import com.api.entity.Product;
import com.api.entity.SessionStorage;
import com.api.pojo.SessionInstance;
import com.api.repository.SessionStorageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.api.service.ProductExternalServiceTestTools.getGenericHtmlContent;
import static com.api.service.ProductExternalServiceTestTools.newSessionStorage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.*;

@SuppressWarnings("unchecked")
public class ProductExternalServiceTest {

    private RestTemplate restTemplateMock;
    private ObjectMapper objectMapperMock;
    private SessionStorageRepository sessionStorageRepository;
    private ProductExternalService productExternalServiceUnderTest;

    void setup(@Nullable final Consumer<SessionStorageRepository> consumer) {
        this.restTemplateMock = mock(RestTemplate.class);

        final RestTemplateBuilder restTemplateBuilderMock = mock(RestTemplateBuilder.class);
        given(restTemplateBuilderMock.rootUri(anyString())).willReturn(restTemplateBuilderMock);
        given(restTemplateBuilderMock.requestFactory(any(Supplier.class))).willReturn(restTemplateBuilderMock);
        given(restTemplateBuilderMock.build()).willReturn(this.restTemplateMock);

        this.objectMapperMock = mock(ObjectMapper.class);
        this.sessionStorageRepository = mock(SessionStorageRepository.class);

        if (Objects.nonNull(consumer)) consumer.accept(this.sessionStorageRepository);

        this.productExternalServiceUnderTest =
            new ProductExternalService(restTemplateBuilderMock, this.objectMapperMock, sessionStorageRepository);
    }

    private void reusableSessionInstance() {
        // Parse the login page
        given(this.restTemplateMock.execute(
            eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn(Jsoup.parse(getGenericHtmlContent(this)));

        // Login into the system to get a cookie
        given(this.restTemplateMock.postForEntity(eq("/wwv_flow.accept"), anyMap(), eq(String.class)))
            .willReturn(
                ResponseEntity
                    .ok()
                    .header("Set-Cookie", "COOKIE_SAVEG_MOBILE=ORA_WWV-dxVoldWhIfN2TviPcS9yhmcI")
                    .build()
            );

        // Parse page to get a Ajax Identifier
        given(this.restTemplateMock.execute(
            eq("/f?p=171:2:54321:NEXT:NO:2:P2_CURSOR:B"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn("peanut butter");
    }

    @Test
    @DisplayName("If everything is ok then should return a valid session instance")
    void should_return_a_valid_session_instance() {
        // given
        setup(it -> {
            given(it.findTopBy(eq(Sort.by("creationDate")))).willReturn(
                Optional.of(newSessionStorage(LocalDate.now().minusDays(3)))
            );
            given(it.save(any(SessionStorage.class))).willAnswer(invocation -> invocation.getArgument(0));
        });

        this.reusableSessionInstance();

        // when
        final SessionInstance actualSessionInstance = this.productExternalServiceUnderTest.newSessionInstance();

        // then
        assertThat(actualSessionInstance).isNotNull();
        assertThat(actualSessionInstance).extracting("sessionId").isEqualTo("54321");
        assertThat(actualSessionInstance).extracting("ajaxIdentifier").isEqualTo("PLUGIN=peanut butter");

        verify(this.restTemplateMock, times(2)).execute(anyString(), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).postForEntity(anyString(), anyMap(), eq(String.class));
        verify(this.sessionStorageRepository, times(1)).findTopBy(eq(Sort.by("creationDate")));
        verify(this.sessionStorageRepository, times(1)).save(any(SessionStorage.class));
    }

    @Test
    @DisplayName("Should throw an exception - newSessionInstance")
    void if_unable_possible_to_parse_the_login_page_then_should_throw_an_exception() {
        // given
        setup(it -> {
            given(it.findTopBy(eq(Sort.by("creationDate")))).willReturn(
                Optional.of(newSessionStorage(LocalDate.now().minusDays(3)))
            );
        });

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
        verify(this.sessionStorageRepository, times(1)).findTopBy(eq(Sort.by("creationDate")));
        verify(this.sessionStorageRepository, never()).save(any(SessionStorage.class));
    }

    @Test
    @DisplayName("Should throw an exception when some of the required fields are missing - newSessionInstance")
    void when_some_of_the_required_fields_are_missing_then_should_throw_an_exception(){
        // given
        setup(it -> {
            given(it.findTopBy(eq(Sort.by("creationDate")))).willReturn(
                Optional.of(newSessionStorage(LocalDate.now().minusDays(3)))
            );
        });

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
        verify(this.sessionStorageRepository, times(1)).findTopBy(eq(Sort.by("creationDate")));
        verify(this.sessionStorageRepository, never()).save(any(SessionStorage.class));
    }

    @Test
    @DisplayName("Should throw an exception when ajaxIdentifier is not found - loginRequest")
    void when_ajaxIdentifier_is_not_found_then_should_throw_an_exception_loginRequest() {
        // given
        setup(it -> {
            given(it.findTopBy(eq(Sort.by("creationDate")))).willReturn(
                Optional.of(newSessionStorage(LocalDate.now().minusDays(3)))
            );
        });

        // Parse the login page
        given(this.restTemplateMock.execute(
            eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn(Jsoup.parse(getGenericHtmlContent(this)));

        // Login into the system to get a cookie
        given(this.restTemplateMock.postForEntity(eq("/wwv_flow.accept"), anyMap(), eq(String.class)))
            .willReturn(ResponseEntity.ok()
                .header("Set-Cookie", "COOKIE_SAVEG_MOBILE=ORA_WWV-dxVoldWhIfN2TviPcS9yhmcI")
                .build()
            );

        // Parse page to get a Ajax Identifier
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
        verify(this.sessionStorageRepository, times(1)).findTopBy(eq(Sort.by("creationDate")));
        verify(this.sessionStorageRepository, never()).save(any(SessionStorage.class));
    }

    @Test
    @DisplayName("Should return a product - fetchByBarcode")
    void given_a_valid_barcode_then_should_return_a_product_fetchByBarcode() throws JsonProcessingException {
        // given
        setup(it -> {
            given(it.findTopBy(eq(Sort.by("creationDate"))))
                .willReturn(Optional.of(newSessionStorage(LocalDate.now())));
        });

        final String existingBarcode = "7896036093085";
        final Product expectedProduct = Product.builder()
            .description("OLEO MARIA")
            .barcode("7896036093085")
            .sequenceCode(1184)
            .build();

        given(this.restTemplateMock.httpEntityCallback(any(HttpEntity.class), eq(String.class))).willReturn(null);
        given(this.objectMapperMock.readValue(anyString(), eq(Product.class)))
            .willReturn(expectedProduct);
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
        final Optional<Product> actualProjection =
            this.productExternalServiceUnderTest.fetchByBarcode(existingBarcode);

        // then
        assertThat(actualProjection).isNotNull();
        assertThat(actualProjection.orElse(null)).isNotNull();
        assertThat(actualProjection.get()).extracting("description").isEqualTo("OLEO MARIA");
        assertThat(actualProjection.get()).extracting("sequenceCode").isEqualTo(1184);
        assertThat(actualProjection.get().getPrices()).isEmpty();
        assertThat(actualProjection.get()).extracting("barcode").isEqualTo(existingBarcode);

        verify(this.restTemplateMock, times(1)).execute(eq("/wwv_flow.show"), eq(HttpMethod.POST), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).httpEntityCallback(any(HttpEntity.class), eq(String.class));
        verify(this.objectMapperMock, times(1)).readValue(anyString(), eq(Product.class));
        verify(this.sessionStorageRepository, times(1)).findTopBy(eq(Sort.by("creationDate")));
        verify(this.sessionStorageRepository, never()).save(any(SessionStorage.class));
    }

    @Test
    @DisplayName("Should return an optional empty - fetchByBarcode ")
    void given_a_non_existing_barcode_then_should_return_an_optional_empty_fetchByBarcode() throws JsonProcessingException {
        // given
        setup(it -> {
            given(it.findTopBy(eq(Sort.by("creationDate"))))
                .willReturn(Optional.of(newSessionStorage(LocalDate.now())));
        });

        final String nonExistingBarcode = "1983471983474";

        given(this.restTemplateMock.httpEntityCallback(any(HttpEntity.class), eq(String.class))).willReturn(null);
        given(this.objectMapperMock.readValue(anyString(), eq(Product.class)))
            .willThrow(new IllegalStateException("Item name is empty"));

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
        final Optional<Product> emptyOptional = productExternalServiceUnderTest.fetchByBarcode(nonExistingBarcode);

        // then
        assertThat(emptyOptional).isNotPresent();

        verify(this.restTemplateMock, times(1)).execute(eq("/wwv_flow.show"), eq(HttpMethod.POST), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).httpEntityCallback(any(HttpEntity.class), eq(String.class));
        verify(this.objectMapperMock, times(1)).readValue(anyString(), eq(Product.class));
        verify(this.sessionStorageRepository, times(1)).findTopBy(eq(Sort.by("creationDate")));
        verify(this.sessionStorageRepository, never()).save(any(SessionStorage.class));


    }

    @Test
    @DisplayName("Should create a new session dynamically")
    void when_a_session_instance_is_not_valid_then_should_recreate_it() throws JsonProcessingException {
       //given
        setup(it -> {
            given(it.findTopBy(eq(Sort.by("creationDate"))))
                .willReturn(Optional.of(newSessionStorage(LocalDate.now().minusMonths(1))));
        });

        final String existingBarcode = "7896036093085";
        final Product expectedProduct = Product.builder()
            .description("OLEO MARIA")
            .barcode("7896036093085")
            .sequenceCode(1184)
            .build();

        this.reusableSessionInstance();

        given(this.restTemplateMock.httpEntityCallback(any(HttpEntity.class), eq(String.class))).willReturn(null);

        given(this.objectMapperMock.readValue(anyString(), eq(Product.class)))
            .willThrow(InvalidDefinitionException.class)
            .willReturn(expectedProduct);

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
        final Optional<Product> actualProduct =
            this.productExternalServiceUnderTest.fetchByBarcode("it is not a barcode");

        //then
        assertThat(actualProduct).isNotNull();
        assertThat(actualProduct.orElse(null)).isNotNull();
        assertThat(actualProduct.get()).extracting("description").isEqualTo("OLEO MARIA");
        assertThat(actualProduct.get().getPrices()).isEmpty();
        assertThat(actualProduct.get()).extracting("sequenceCode").isEqualTo(1184);
        assertThat(actualProduct.get()).extracting("barcode").isEqualTo(existingBarcode);

        verify(this.restTemplateMock, times(2)).execute(eq("/wwv_flow.show"), eq(HttpMethod.POST), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(2)).httpEntityCallback(any(HttpEntity.class), eq(String.class));
        verify(this.objectMapperMock, times(2)).readValue(anyString(), eq(Product.class));

        // reusableNewInstance
        verify(this.restTemplateMock, times(1)).execute(eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).execute(eq("/f?p=171:2:54321:NEXT:NO:2:P2_CURSOR:B"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).postForEntity(eq("/wwv_flow.accept"), anyMap(), eq(String.class));
        verify(this.sessionStorageRepository, times(1)).findTopBy(eq(Sort.by("creationDate")));
        verify(this.sessionStorageRepository, times(1)).save(any(SessionStorage.class));
    }
}
