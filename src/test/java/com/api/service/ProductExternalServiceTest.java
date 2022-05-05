package com.api.service;

import com.api.dto.InputItemDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.api.pojo.SessionInstance;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    private void getASessionInstanceGenericStub() {
        given(this.restTemplateMock.execute(
            eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn(Jsoup.parse(this.getDefaultHtmlContent()));

        given(this.restTemplateMock.postForEntity(eq("/wwv_flow.accept"), anyMap(), eq(String.class))).will(invocation -> null);

        given(this.restTemplateMock.execute(
            eq("/f?p=171:2:54321:NEXT:NO:2:P2_CURSOR:B"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn("peanut butter");
    }

    private String getDefaultHtmlContent() {
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
    void if_the_structures_of_the_html_responses_are_intact_then_should_return_a_valid_session_instance__getASessionInstance() {
        // given
        this.getASessionInstanceGenericStub();

        // when
        final SessionInstance actualSessionInstance = this.productExternalServiceUnderTest.getASessionInstance();

        // then
        assertThat(actualSessionInstance).isNotNull();
        assertThat(actualSessionInstance).extracting("sessionId").isEqualTo("54321");
        assertThat(actualSessionInstance).extracting("ajaxIdentifier").isEqualTo("PLUGIN=peanut butter");

        verify(this.restTemplateMock, times(2)).execute(anyString(), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).postForEntity(anyString(), anyMap(), eq(String.class));
    }

    @Test
    void if_the_login_page_parsing_fails_then_should_throw_an_exception_getASessionInstance() {
        // given
        given(this.restTemplateMock.execute(
            eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn(null);

        // when
        final Throwable actualThrowable = catchThrowable(() -> this.productExternalServiceUnderTest.getASessionInstance());

        // then
        assertThat(actualThrowable).isInstanceOf(IllegalStateException.class);
        assertThat(actualThrowable).hasMessage("Login page parsing failed");

        verify(this.restTemplateMock, times(1)).execute(anyString(), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, only()).execute(anyString(), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, never()).postForEntity(anyString(), anyMap(), eq(String.class));
    }

    @Test
    void when_some_of_the_required_fields_are_missing_then_should_throw_an_exception_getASessionInstance() {
        // given
        given(this.restTemplateMock.execute(
            eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn(Jsoup.parse("<input id=\"pInstance\" value=\"54321\"/><input id=\"pPageSubmissionId\" value=\"898900\"/>"));

        // when
        final Throwable actualThrowable = catchThrowable(() -> this.productExternalServiceUnderTest.getASessionInstance());

        // then
        assertThat(actualThrowable).isNotNull();
        assertThat(actualThrowable).isInstanceOf(IllegalStateException.class);

        verify(this.restTemplateMock, times(1)).execute(anyString(), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, only()).execute(anyString(), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, never()).postForEntity(anyString(), anyMap(), eq(String.class));
    }

    @Test
    void when_ajaxIdentifier_is_not_found_then_should_throw_an_exception_getASessionInstance() {
        // given
        given(this.restTemplateMock.execute(
                eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willReturn(Jsoup.parse(this.getDefaultHtmlContent()));

        given(this.restTemplateMock.postForEntity(eq("/wwv_flow.accept"), anyMap(), eq(String.class))).will(invocation -> null);

        given(this.restTemplateMock.execute(eq("/f?p=171:2:54321:NEXT:NO:2:P2_CURSOR:B"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class)
        )).willThrow(new IllegalStateException("Ajax identifier not found"));

        // when
        final Throwable actualThrowable = catchThrowable(() -> this.productExternalServiceUnderTest.getASessionInstance());

        // then
        assertThat(actualThrowable).isNotNull();
        assertThat(actualThrowable).isInstanceOf(IllegalStateException.class);
        assertThat(actualThrowable).hasMessage("Ajax identifier not found");

        verify(this.restTemplateMock, times(2)).execute(anyString(), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).postForEntity(anyString(), anyMap(), eq(String.class));
    }

    @Test
    void given_a_valid_bar_code_then_should_return_an_input_dto_fetchByEanCode() throws JsonProcessingException {
        // given
        final String existingBarCode = "123456789101";

        final Supplier<InputItemDTO> inputItemDTOSupplier = () -> InputItemDTO.builder()
            .description("description")
            .barcode(existingBarCode)
            .currentPrice(16.4)
            .sequence(123456)
            .build();

        given(this.restTemplateMock.httpEntityCallback(any(HttpEntity.class), eq(String.class))).willReturn(null);

        given(this.objectMapperMock.readValue(anyString(), eq(InputItemDTO.class)))
            .willReturn(inputItemDTOSupplier.get());

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
        final Optional<InputItemDTO> actualDTO =
            this.productExternalServiceUnderTest.fetchByEanCode(existingBarCode);

        // then
        assertThat(actualDTO).as("Optional cannot be null").isNotNull();
        assertThat(actualDTO.orElse(null)).as("actualDTO cannot be null").isNotNull();
        assertThat(actualDTO.get()).extracting("description").as("Description is not correct").isEqualTo("description");
        assertThat(actualDTO.get()).extracting("currentPrice").as("Price is not correct").isEqualTo(16.4);
        assertThat(actualDTO.get()).extracting("barcode").as("EanCode is not correct").isEqualTo("123456789101");

        verify(this.restTemplateMock, times(1)).execute(eq("/wwv_flow.show"), eq(HttpMethod.POST), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).httpEntityCallback(any(HttpEntity.class), eq(String.class));
        verify(this.objectMapperMock, times(1)).readValue(anyString(), eq(InputItemDTO.class));
    }

    @Test
    void given_a_non_existent_bar_code_then_should_return_optional_empty_fetchByEanCode() throws JsonProcessingException {
        // given
        final String nonExistingBarCode = "1983471983474";

        given(this.restTemplateMock.httpEntityCallback(any(HttpEntity.class), eq(String.class))).willReturn(null);

        given(this.objectMapperMock.readValue(anyString(), eq(InputItemDTO.class))).willThrow(new IllegalStateException("Item name is empty"));

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
        final Optional<InputItemDTO> actualDTO = this.productExternalServiceUnderTest.fetchByEanCode(nonExistingBarCode);

        // then
        assertThat(actualDTO).as("Optional cannot be null").isNotNull();
        assertThat(actualDTO.orElse(null)).as("'actualDTO' must be null").isNull();

        verify(this.restTemplateMock, times(1)).execute(eq("/wwv_flow.show"), eq(HttpMethod.POST), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).httpEntityCallback(any(HttpEntity.class), eq(String.class));
        verify(this.objectMapperMock, times(1)).readValue(anyString(), eq(InputItemDTO.class));
    }

    @Test
    void when_a_session_instance_is_not_valid_then_should_recreate_it() throws JsonProcessingException {
       //given
        final Supplier<InputItemDTO> inputItemDTOSupplier = () ->
            InputItemDTO.builder()
                .description("description")
                .barcode("123456789101")
                .currentPrice(16.4)
                .sequence(123456)
                .build();

        this.getASessionInstanceGenericStub();

        given(this.restTemplateMock.httpEntityCallback(any(HttpEntity.class), eq(String.class))).willReturn(null);

        given(this.objectMapperMock.readValue(anyString(), eq(InputItemDTO.class)))
            .willReturn(null)
            .willReturn(inputItemDTOSupplier.get());

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
        final Optional<InputItemDTO> actualDTO =
            this.productExternalServiceUnderTest.fetchByEanCode("134324134324");

        //then

        assertThat(actualDTO).as("Optional cannot be null").isNotNull();
        assertThat(actualDTO.orElse(null)).as("actualDTO cannot be null").isNotNull();
        assertThat(actualDTO.get()).extracting("description").as("Description is not correct").isEqualTo("description");
        assertThat(actualDTO.get()).extracting("currentPrice").as("Price is not correct").isEqualTo(16.4);
        assertThat(actualDTO.get()).extracting("barcode").as("barcode is not correct").isEqualTo("123456789101");

        verify(this.restTemplateMock, times(2)).execute(eq("/wwv_flow.show"), eq(HttpMethod.POST), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(2)).httpEntityCallback(any(HttpEntity.class), eq(String.class));
        verify(this.objectMapperMock, times(2)).readValue(anyString(), eq(InputItemDTO.class));

        // getASessionInstanceGenericStub();
        verify(this.restTemplateMock, times(1)).execute(eq("/f?p=171"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).execute(eq("/f?p=171:2:54321:NEXT:NO:2:P2_CURSOR:B"), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).postForEntity(eq("/wwv_flow.accept"), anyMap(), eq(String.class));

    }
}