package com.api.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ProductRepositoryIT {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void should_return_all_ids_findAllId() {
        final Page<UUID> uuidPage = productRepository.findAllId(PageRequest.ofSize(10));

        assertThat(uuidPage).isNotNull();
        assertThat(uuidPage.getContent()).isInstanceOf(List.class);
        assertThat(uuidPage.getContent()).hasSize(10);
    }
}
