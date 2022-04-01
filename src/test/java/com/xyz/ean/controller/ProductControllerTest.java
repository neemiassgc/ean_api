package com.xyz.ean.controller;

import com.xyz.ean.service.DomainMapper;
import com.xyz.ean.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @MockBean
    private ProductService productServiceMock;

    @MockBean
    private DomainMapper domainMapperMock;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = standaloneSetup(new ProductController(this.productServiceMock, this.domainMapperMock)).build();
    }
}