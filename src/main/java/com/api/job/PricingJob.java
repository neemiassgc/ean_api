package com.api.job;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.service.interfaces.EmailService;
import com.api.service.interfaces.ProductExternalService;
import com.api.service.interfaces.ProductService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            sendFailureMessage(e.getMessage());
            throw e;
        }

        sendSuccessMessage(info);
    }

    private String calculateVariablePercentage(final BigDecimal originalPrice, final BigDecimal newPrice) {
        final BigDecimal fraction = originalPrice.subtract(newPrice).divide(originalPrice, 2, RoundingMode.HALF_EVEN);
        final BigDecimal oneHundred = new BigDecimal("100");
        final BigDecimal percentage = fraction.abs().multiply(oneHundred);
        return (isNegative(fraction) ? ">" : "<")+" ~"+percentage.toBigInteger()+"%";
    }

    private boolean isNegative(final BigDecimal value) {
        return value.signum() == -1;
    }

    private void sendFailureMessage(final String message) {
        final String emailMessage =
        "Running job failed\n\n"+
        "With exception: "+message;

        emailService.sendAuditEmail(emailMessage);
    }

    public void sendSuccessMessage(final Info info) {
        final Function<ProductDetails, String> mapperFunction = productDetails ->
           String.format(
                "%s | R$%s -> R$%s %s", productDetails.getDescription(),
                productDetails.getOldPrice(), productDetails.getNewPrice(),
                calculateVariablePercentage(productDetails.getOldPrice(), productDetails.getNewPrice())
            );

        final String emailMessage =
            "Running job successfully\n\n"+
            String.format("%d/%d products changed\n\n", info.getProductDetailsList().size(), info.getTotalOfProducts())+
            String.format("Elapsed time: %d seconds\n\n", info.getElapsedTimeInSeconds()) +
            info.getProductDetailsList().stream().map(mapperFunction).collect(Collectors.joining("\n"));

        emailService.sendAuditEmail(emailMessage);
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

    @Getter
    @RequiredArgsConstructor
    @Builder
    private static class Info {
        private final List<ProductDetails> productDetailsList;
        private final int totalOfProducts;
        private final long elapsedTimeInSeconds;
    }

    @Getter
    @RequiredArgsConstructor
    private static class ProductDetails {
        private final String description;
        private final BigDecimal oldPrice;
        private final BigDecimal newPrice;
    }
}