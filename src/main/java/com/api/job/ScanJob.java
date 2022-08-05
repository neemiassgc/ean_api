package com.api.job;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.repository.ProductRepository;
import com.api.service.ProductExternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ScanJob implements Job {

    private final ProductExternalService productExternalService;
    private final ProductRepository productRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void execute(JobExecutionContext context) {
        final List<Product> products = productRepository.findAllWithLastPrice();

        for (Product product : products) {
            final Price newPrice = productExternalService.fetchByBarcode(product.getBarcode())
                .map(it -> {
                    final Price priceToReturn = it.getPrices().get(0);
                    it.removePrice(priceToReturn);
                    return priceToReturn;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

            if (product.getPrices().get(0).getValue().equals(newPrice.getValue()))
                continue;

            productRepository.save(product.addPrice(newPrice));
        }
    }
}