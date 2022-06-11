package com.api.job;

import com.api.entity.Product;
import com.api.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScanJobIT {

    @Autowired private ProductService productService;
    @Autowired private ScanJob scanJob;

    @Test
    void should_update_the_prices() {
        scanJob.execute(Mockito.mock(JobExecutionContext.class));

        final Map<UUID, Product> actualProductMap = productService
            .findAllByOrderByDescriptionAsc()
            .stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        assertThat(actualProductMap).hasSize(4);
        assertThat(actualProductMap.get(UUID.fromString("7e49cbbf-0d4b-4a67-b108-346bef1c961f")).getPrices()).hasSize(5);
        assertThat(actualProductMap.get(UUID.fromString("3f30dc5c-5ce1-4556-a648-de8e55b0f6be")).getPrices()).hasSize(4);
        assertThat(actualProductMap.get(UUID.fromString("f5a3feed-b3f0-4253-99f9-129049856c4f")).getPrices()).hasSize(3);
        assertThat(actualProductMap.get(UUID.fromString("f3a9f940-9c07-4986-a655-8b91119dae8a")).getPrices()).hasSize(2);

    }
}