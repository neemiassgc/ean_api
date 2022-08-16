package com.api.job;

import com.api.entity.Price;
import com.api.entity.Product;
import com.api.repository.ProductRepository;
import com.api.service.interfaces.EmailService;
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

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ScanJob implements Job {

    private final ProductExternalService productExternalService;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    @Transactional(propagation = Propagation.REQUIRED)
    private Map<String, Integer> delegateTask() {
        final List<Product> products = productRepository.findAllWithLastPrice();
        final int totalOfProducts = products.size();
        int countOfChangedProducts = 0;

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
            countOfChangedProducts++;
        }

        return Map.of("totalOfProducts", totalOfProducts, "countOfChangedProducts", countOfChangedProducts);
    }

    @Override
    public void execute(JobExecutionContext context) {
        final long startMeasureTime = System.currentTimeMillis();
        Map<String, Integer> summary = null;

        try {
            summary = delegateTask();
        }
        catch (Exception e) {
            final String emailMessage =
            "Running job failed\n\n"+
            "With exception: "+e.getMessage();

            emailService.sendAuditEmail(emailMessage);

            throw e;
        }
        final long elapsedTimeInSeconds = Duration.ofMillis(System.currentTimeMillis() - startMeasureTime).toSeconds();

        final String emailMessage =
        "Running job successfully\n\n"+
        String.format("%d/%d products changed\n\n", summary.get("countOfChangedProducts"), summary.get("totalOfProducts"))+
        String.format("Elapsed time: %d seconds", elapsedTimeInSeconds);

        emailService.sendAuditEmail(emailMessage);
    }
}