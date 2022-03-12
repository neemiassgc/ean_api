package com.xyz.ean.job;

import com.xyz.ean.entity.Price;
import com.xyz.ean.service.ForeignProductHttpService;
import com.xyz.ean.service.ProductService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ScanJob extends QuartzJobBean {

    private final ForeignProductHttpService foreignProductHttpService;
    private final ProductService productService;

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) {

        productService.findAll().forEach(dbProduct -> {
            final String dbProductEanCode = dbProduct.getEanCode();
            foreignProductHttpService.fetchByEanCode(dbProductEanCode).ifPresent(externalProduct -> {
                final double externalProductPrice = externalProduct.getCurrentPrice();
                if (!Objects.equals(dbProduct.getPrices().get(0).getPrice(), externalProductPrice)) {
                    dbProduct.addPrice(new Price(externalProductPrice));
                    this.productService.save(dbProduct);
                }
            });
        });
    }
}
