package com.api.job;

import com.api.entity.Product;
import com.api.service.ProductService;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScanJobIT {

    @Autowired private Scheduler scheduler;
    @Autowired private ProductService productService;

    @Test
    void should_add_prices() throws SchedulerException {
        scheduler.triggerJob(new JobKey("scanJob"));

        final List<Product> actualProductList = productService.findAll();

        assertThat(actualProductList).hasSize(4);
        assertThat(actualProductList.get(0).getPrices()).hasSize(5);
        assertThat(actualProductList.get(1).getPrices()).hasSize(4);
        assertThat(actualProductList.get(2).getPrices()).hasSize(3);
        assertThat(actualProductList.get(3).getPrices()).hasSize(2);
    }
}