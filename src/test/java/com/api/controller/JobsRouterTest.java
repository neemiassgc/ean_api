package com.api.controller;

import com.api.job.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {JobsRouter.class, GlobalErrorHandlingController.class})
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public final class JobsRouterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Job job;
}
