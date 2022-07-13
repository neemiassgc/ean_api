package com.api.job;

import com.api.entity.Price;
import com.api.repository.PriceRepository;
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
    private final PriceRepository priceRepository;

    @Override
    public void execute(JobExecutionContext context) {
        final List<Price> allLatestPrices = priceRepository.findAllLatestPrice();

        for (final Price oldPrice : allLatestPrices) {
            productExternalService.fetchByBarcode(oldPrice.getProduct().getBarcode())
                .map(productBase -> ((ProductWithLatestPrice) productBase).getLatestPrice())
                .map(priceWithInstant -> new Price(priceWithInstant.getValue(), priceWithInstant.getInstant(), oldPrice.getProduct()))
                .ifPresent((newPrice) -> {
                    if (!oldPrice.getValue().equals(newPrice.getValue()))
                        priceRepository.save(newPrice);
                });
        }
    }
}
