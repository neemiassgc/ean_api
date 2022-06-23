package com.api.job;

import com.api.service.PersistenceService;
import com.api.service.ProductExternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.api.projection.Projection.ProductWithLatestPrice;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ScanJob implements Job {

    private final ProductExternalService productExternalService;
    private final PersistenceService persistenceService;

    @Override
    public void execute(JobExecutionContext context) {
        final List<ProductWithLatestPrice> productsWithLatestPrice = persistenceService.findAllProductsWithLatestPrice();

        for (ProductWithLatestPrice productFromDB : productsWithLatestPrice) {
            productExternalService.fetchByBarcode(productFromDB.getBarcode())
                .map(product -> (ProductWithLatestPrice) product)
                .ifPresent(externalProduct -> {
                    if (!(productFromDB.getLatestPrice().equals(externalProduct.getLatestPrice()))) {
                        persistenceService.saveProductWithPrice(externalProduct);
                    }
                });
        }
    }
}
