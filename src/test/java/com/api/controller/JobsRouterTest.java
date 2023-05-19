package com.api.controller;

import com.api.entity.Product;
import com.api.job.Job;
import com.api.service.CacheManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {JobsRouter.class, GlobalErrorHandlingController.class})
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public final class JobsRouterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Job job;

    private final String URI = "/api/jobs/updatePrices";

    @Test
    @DisplayName("If X-Appegine-Cron is missing then api/jobs/updatePrices -> 400 BAD_REQUEST")
    void should_not_run_the_job_if_X_Appengine_Cron_header_is_missing() throws Exception {
        testWithHeader(HttpStatus.BAD_REQUEST, "BAD_REQUEST", null);

        verify(job, never()).execute();
    }

    @Test
    @DisplayName("If X-Appegine-Cron is not set to true then api/jobs/updatePrices -> 400 BAD_REQUEST")
    void should_not_run_the_job_if_X_Appengine_Cron_header_is_not_true() throws Exception {
        testWithHeader(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "false");

        verify(job, never()).execute();
    }

    @Test
    @DisplayName("If X-Appegine-Cron is set to anything else than true then api/jobs/updatePrices -> 400 BAD_REQUEST\"")
    void should_not_run_the_job_if_X_Appengine_Cron_header_is_set_to_anything_else_than_true() throws Exception {
        testWithHeader(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "apple");

        verify(job, never()).execute();
    }

    @Test
    @DisplayName("If X-Appegine-Cron is set to true then api/jobs/updatePrices -> 200 Ok")
    void should_run_the_job_succesfully_when_X_Appengine_Cron_header_is_set_to_true() throws Exception {
        doNothing().when(job).execute();

        testWithHeader(HttpStatus.OK, "OK", "true");

        verify(job, times(1)).execute();
        verifyNoMoreInteractions(job);
    }

    private void testWithHeader(
        @NonNull final HttpStatus expectedHttpStatus,
        @NonNull final String expectedContent,
        @Nullable final String headerValue
    ) throws Exception {
        final MockHttpServletRequestBuilder requestBuilder =
            get(URI).accept(MediaType.ALL).characterEncoding(StandardCharsets.UTF_8);

        if (Objects.nonNull(headerValue))
            requestBuilder.header("X-Appengine-Cron", headerValue);

        mockMvc.perform(requestBuilder)
            .andExpect(status().is(expectedHttpStatus.value()))
            .andExpect(content().contentType("text/plain;charset=UTF-8"))
            .andExpect(content().string(expectedContent));
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public CacheManager<Product, UUID> productCacheManager() {
            return new CacheManager<>(Product::getId);
        }
    }
}
