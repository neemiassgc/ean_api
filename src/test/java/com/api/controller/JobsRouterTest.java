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
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        mockMvc.perform(get(URI)
            .accept(MediaType.ALL)
            .characterEncoding(StandardCharsets.UTF_8)
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType("text/plain;charset=UTF-8"))
        .andExpect(content().string("BAD_REQUEST"));

        verify(job, never()).execute();
    }


    @TestConfiguration
    static class TestConfig {

        @Bean
        public CacheManager<Product, UUID> productCacheManager() {
            return new CacheManager<>(Product::getId);
        }
    }
}
