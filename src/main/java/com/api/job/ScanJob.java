package com.api.job;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.service.ProductExternalService;
import com.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ScanJob implements Job {

    private final ProductExternalService productExternalService;
    private final ProductService productService;

    @Override
    public void execute(JobExecutionContext context) {
        final List<Product> productsToScan = productService.findAll();
        log.info("ProductService.findAll() invoked; fetched {} products", productsToScan.size());

        productsToScan.forEach(product -> {
            final String productBarcode = product.getBarcode();

            productExternalService.fetchByEanCode(productBarcode).ifPresent(inputItem -> {
                log.info("ProductExternalService.fetchByEanCode({}) invoked; ({})", productBarcode, inputItem.getDescription());

                final Double currentPrice = inputItem.getCurrentPrice();
                final Double lastPrice = product.getPrices().get(0).getPrice();

                if (!Objects.equals(currentPrice, lastPrice)) {
                    product.addPrice(new Price(currentPrice));
                    log.info("Product.addPrice({}) invoked; ({})", currentPrice, product.getDescription());
                }
            });
        });

        productService.saveAll(productsToScan);
        log.info("productService.saveAll(productsToScan) invoked");
    }
}
