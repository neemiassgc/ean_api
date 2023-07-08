package com.api.job;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.service.interfaces.EmailService;
import com.api.service.interfaces.ProductExternalService;
import com.api.service.interfaces.ProductService;
import com.api.service.minimal.Info;
import com.api.service.minimal.ProductDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class PricingJob implements Job {

    private final ProductExternalService productExternalServiceImpl;
    private final ProductService productService;
    private final EmailService emailService;

    @Override
    public void execute() {
        final Info info;

        try {
            info = updatePrices();
        }
        catch (Exception e) {
            emailService.sendFailureMessage(e.getMessage());
            throw e;
        }

        emailService.sendSuccessMessage(info);
    }

    private Info updatePrices() {
        final List<Product> products = productService.findAllWithLatestPrice();
        final List<ProductDetails> productDetailsList = new ArrayList<>(products.size());
        final long startMeasureTime = System.currentTimeMillis();

        for (Product product : products) {
            final Price newPrice = productExternalServiceImpl.fetchByBarcode(product.getBarcode())
                .map(this::clonePriceFromProduct)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

            if (checkIfPricesAreEqual(product.getPrices().get(0), newPrice)) {
                log.info("No price difference found for product: "+product.getBarcode()+"...");
                continue;
            }

            log.info("Saving price for product with bacode: "+product.getBarcode()+"...");
            product.addPrice(newPrice);
            productService.save(product);

            final BigDecimal previousPrice = product.getPrices().get(0).getValue();
            final BigDecimal lastestPrice = newPrice.getValue();
            productDetailsList.add(new ProductDetails(product.getDescription(), previousPrice, lastestPrice));

            log.info("Delay of 1 second...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.info("Delay Interrupted");
            }
        }

        final long elapsedTimeInSeconds = Duration.ofMillis(System.currentTimeMillis() - startMeasureTime).toSeconds();
        return Info.builder()
            .productDetailsList(productDetailsList)
            .totalOfProducts(products.size())
            .elapsedTimeInSeconds(elapsedTimeInSeconds)
            .build();
    }

    private Price clonePriceFromProduct(final Product product) {
        final Price priceToBeCloned = product.getPrices().get(0);
        return new Price(priceToBeCloned.getValue(), priceToBeCloned.getInstant(), null);
    }

    private boolean checkIfPricesAreEqual(final Price priceA, final Price priceB) {
        return priceA.getValue().equals(priceB.getValue());
    }
}