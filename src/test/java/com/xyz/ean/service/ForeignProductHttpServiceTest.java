package com.xyz.ean.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xyz.ean.dto.InputItemDTO;
import com.xyz.ean.pojo.SessionInstance;
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
        final SessionInstance actualSessionInstance = this.foreignProductHttpServiceUnderTest.getASessionInstance();

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
        final Throwable actualThrowable = catchThrowable(() -> this.foreignProductHttpServiceUnderTest.getASessionInstance());

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
        final Throwable actualThrowable = catchThrowable(() -> this.foreignProductHttpServiceUnderTest.getASessionInstance());

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
        final Throwable actualThrowable = catchThrowable(() -> this.foreignProductHttpServiceUnderTest.getASessionInstance());

        // then
        assertThat(actualThrowable).isNotNull();
        assertThat(actualThrowable).isInstanceOf(IllegalStateException.class);
        assertThat(actualThrowable).hasMessage("Ajax identifier not found");

        verify(this.restTemplateMock, times(2)).execute(anyString(), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).postForEntity(anyString(), anyMap(), eq(String.class));
    }

    @Test
    void given_a_valid_ean_code_then_should_return_an_input_dto_fetchByEanCode() throws JsonProcessingException {
        // given
        final Supplier<ObjectNode> objectNodeSupplier = () -> {
            ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
            ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode(5);
            arrayNode.insertObject(0);
            arrayNode.insertObject(1).set("value", JsonNodeFactory.instance.textNode("default description"));
            arrayNode.insertObject(2).set("value", JsonNodeFactory.instance.numberNode(12345));
            arrayNode.insertObject(3);
            arrayNode.insertObject(4).set("value", JsonNodeFactory.instance.numberNode(16.4));
            arrayNode.insertObject(5).set("value", JsonNodeFactory.instance.textNode("134283434809"));

            rootNode.putArray("item").addAll(arrayNode);

            return rootNode;
        };

        final String existingEanCode = "1234567890123";

        given(this.restTemplateMock.httpEntityCallback(any(HttpEntity.class), eq(String.class))).willReturn(null);

        given(this.objectMapperMock.readTree(anyString())).willReturn(objectNodeSupplier.get());

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
            this.foreignProductHttpServiceUnderTest.fetchByEanCode(existingEanCode);

        // then
        assertThat(actualDTO).as("Optional cannot be null").isNotNull();
        assertThat(actualDTO.orElse(null)).as("actualDTO cannot be null").isNotNull();
        assertThat(actualDTO.get()).extracting("description").as("Description is not correct").isEqualTo("default description");
        assertThat(actualDTO.get()).extracting("currentPrice").as("Price is not correct").isEqualTo(16.4);
        assertThat(actualDTO.get()).extracting("eanCode").as("EanCode is not correct").isEqualTo("134283434809");

        verify(this.restTemplateMock, times(1)).execute(eq("/wwv_flow.show"), eq(HttpMethod.POST), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).httpEntityCallback(any(HttpEntity.class), eq(String.class));
        verify(this.objectMapperMock, times(1)).readTree(anyString());
    }

    @Test
    void given_a_non_existent_ean_code_then_should_return_empty_fetchByEanCode() throws JsonProcessingException {
        // given
        final String nonExistingEanCode = "";

        final Supplier<ObjectNode> objectNodeSupplier = () -> {
            ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
            ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode(5);
            arrayNode.insertObject(0);
            arrayNode.insertObject(1).set("value", JsonNodeFactory.instance.textNode("default description"));
            arrayNode.insertObject(2).set("value", JsonNodeFactory.instance.numberNode(12345));
            arrayNode.insertObject(3);
            arrayNode.insertObject(4).set("value", JsonNodeFactory.instance.numberNode(16.4));
            arrayNode.insertObject(5).set("value", JsonNodeFactory.instance.textNode(""));

            rootNode.putArray("item").addAll(arrayNode);

            return rootNode;
        };

        given(this.restTemplateMock.httpEntityCallback(any(HttpEntity.class), eq(String.class))).willReturn(null);

        given(this.objectMapperMock.readTree(anyString())).willReturn(objectNodeSupplier.get());

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
        final Optional<InputItemDTO> actualDTO = this.foreignProductHttpServiceUnderTest.fetchByEanCode(nonExistingEanCode);

        // then
        assertThat(actualDTO).as("Optional cannot be null").isNotNull();
        assertThat(actualDTO.orElse(null)).as("'actualDTO' must be null").isNull();

        verify(this.restTemplateMock, times(1)).execute(eq("/wwv_flow.show"), eq(HttpMethod.POST), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).httpEntityCallback(any(HttpEntity.class), eq(String.class));
        verify(this.objectMapperMock, times(1)).readTree(anyString());
    }

    @Test
    void when_a_session_instance_is_not_valid_then_should_recreate_it() throws JsonProcessingException {
       //given
        final Supplier<ObjectNode> objectNodeSupplier = () -> {
            ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
            ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode(5);
            arrayNode.insertObject(0);
            arrayNode.insertObject(1).set("value", JsonNodeFactory.instance.textNode("default description"));
            arrayNode.insertObject(2).set("value", JsonNodeFactory.instance.numberNode(12345));
            arrayNode.insertObject(3);
            arrayNode.insertObject(4).set("value", JsonNodeFactory.instance.numberNode(16.4));
            arrayNode.insertObject(5).set("value", JsonNodeFactory.instance.textNode("123412341234"));

            rootNode.putArray("item").addAll(arrayNode);

            return rootNode;
        };

        this.getASessionInstanceGenericStub();

        given(this.restTemplateMock.httpEntityCallback(any(HttpEntity.class), eq(String.class))).willReturn(null);

        given(this.objectMapperMock.readTree(anyString()))
            .willReturn(JsonNodeFactory.instance.nullNode())
            .willReturn(objectNodeSupplier.get());

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
                this.foreignProductHttpServiceUnderTest.fetchByEanCode("134324134324");

        //then

        assertThat(actualDTO).as("Optional cannot be null").isNotNull();
        assertThat(actualDTO.orElse(null)).as("actualDTO cannot be null").isNotNull();
        assertThat(actualDTO.get()).extracting("description").as("Description is not correct").isEqualTo("default description");
        assertThat(actualDTO.get()).extracting("currentPrice").as("Price is not correct").isEqualTo(16.4);
        assertThat(actualDTO.get()).extracting("eanCode").as("EanCode is not correct").isEqualTo("123412341234");

        verify(this.restTemplateMock, times(2)).execute(eq("/wwv_flow.show"), eq(HttpMethod.POST), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(2)).httpEntityCallback(any(HttpEntity.class), eq(String.class));
        verify(this.objectMapperMock, times(2)).readTree(anyString());
        verify(this.restTemplateMock, times(2)).execute(anyString(), eq(HttpMethod.GET), isNull(), any(ResponseExtractor.class));
        verify(this.restTemplateMock, times(1)).postForEntity(anyString(), anyMap(), eq(String.class));
    }
}
