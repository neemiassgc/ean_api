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
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class PricingJob implements Job {

    private final ProductExternalService productExternalServiceImpl;
    private final ProductService productService;
    private final EmailService emailService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void execute() {
        final long startMeasureTime = System.currentTimeMillis();
        final Info info;

        try {
            info = updatePrices();
        }
        catch (Exception e) {
            sendFailureMessage(e.getMessage());
            throw e;
        }
        final long elapsedTimeInSeconds = Duration.ofMillis(System.currentTimeMillis() - startMeasureTime).toSeconds();

        sendSuccessMessage(
            Info.builder()
                .changedProductDescriptions(info.getChangedProductDescriptions())
                .countOfChangedProducts(info.getCountOfChangedProducts())
                .totalOfProducts(info.getTotalOfProducts())
                .elapsedTimeInSeconds(elapsedTimeInSeconds)
                .build()
        );
    }

    private void sendFailureMessage(final String message) {
        final String emailMessage =
        "Running job failed\n\n"+
        "With exception: "+message;

        emailService.sendAuditEmail(emailMessage);
    }

    private void sendSuccessMessage(final Info info) {
        final String emailMessage =
            "Running job successfully\n\n"+
            String.format("%d/%d products changed\n\n", info.getCountOfChangedProducts(), info.getTotalOfProducts())+
            String.format("Elapsed time: %d seconds\n\n", info.getElapsedTimeInSeconds()) +
            String.join("\n", info.getChangedProductDescriptions());

        emailService.sendAuditEmail(emailMessage);
    }

    private Info updatePrices() {
        final List<Product> products = productService.findAllWithLatestPrice();
        final List<Product> changedProducts = new ArrayList<>();
        final int totalOfProducts = products.size();
        int countOfChangedProducts = 0;

        for (Product product : products) {
            final Price newPrice = productExternalServiceImpl.fetchByBarcode(product.getBarcode())
                .map(fetchedProduct -> {
                    final Price price = fetchedProduct.getPrices().get(0);
                    fetchedProduct.removePrice(price);
                    return price;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

            if (checkIfPricesAreEqual(product.getPrices().get(0), newPrice))
                continue;

            productService.save(product.addPrice(newPrice));
            countOfChangedProducts++;
            changedProducts.add(product);
        }

        return Info.builder()
            .countOfChangedProducts(countOfChangedProducts)
            .totalOfProducts(totalOfProducts)
            .changedProductDescriptions(changedProducts.stream().map(Product::getDescription).collect(Collectors.toList()))
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
        private final int countOfChangedProducts;
        private final int totalOfProducts;
        private final long elapsedTimeInSeconds;
        private final List<String> changedProductDescriptions;
    }
}