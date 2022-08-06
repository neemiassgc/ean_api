package com.api.controller;

import com.api.entity.Product;
import com.api.projection.Projection;
import com.api.repository.ProductRepository;
import com.api.service.DomainMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private DomainMapper domainMapper;

    private MockMvc mockMvc;

    private static class Resources {

        private static List<Product> products;
        private static List<Projection.SimpleProduct> simpleProducts;

        static {
            products = List.of(
                Product.builder()
                    .description("ACHOC PO NESCAU 800G")
                    .sequenceCode(29250)
                    .barcode("7891000055120")
                    .build(),
                Product.builder()
                    .description("AMENDOIM SALG CROKISSIMO 400G PIMENTA")
                    .sequenceCode(120983)
                    .barcode("7896336010058")
                    .build(),
                Product.builder()
                    .description("CAFE UTAM 500G")
                    .sequenceCode(2909)
                    .barcode("7896656800018")
                    .build()
            );

            simpleProducts = products
                .stream()
                .map(product -> new Projection.SimpleProduct() {
                    @Override
                    public String getDescription() {
                        return product.getDescription();
                    }

                    @Override
                    public String getBarcode() {
                        return product.getBarcode();
                    }

                    @Override
                    public Integer getSequenceCode() {
                        return product.getSequenceCode();
                    }
                }).collect(Collectors.toList());
        }
    }

    @BeforeEach
    void setUp() {
        this.mockMvc = standaloneSetup(new ProductController(productRepository, domainMapper)).alwaysDo(print()).build();
    }
}