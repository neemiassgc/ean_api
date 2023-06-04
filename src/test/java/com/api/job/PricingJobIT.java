package com.api.job;

import com.api.Resources;
import com.api.repository.PriceRepository;
import com.api.service.interfaces.EmailService;
import com.api.service.interfaces.ProductExternalService;
import com.api.service.interfaces.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PricingJobIT {

    @Autowired
    private PriceRepository priceRepository;

    @MockBean
    private ProductExternalService productExternalService;

    @Autowired
    private ProductService productService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockBean
    private EmailService emailService;

    private PricingJob jobUnderTest;

    @BeforeEach
    void before() {
        jobUnderTest = new PricingJob(productExternalService, productService, emailService, transactionTemplate);
    }

    @Test
    void should_save_different_unequal_prices() {
        willDoNothing().given(emailService).sendAuditEmail(anyString());
        given(productExternalService.fetchByBarcode(anyString()))
            .willAnswer(invocationOnMock -> {
                System.out.println("Fetching product with barcode: "+invocationOnMock.getArgument(0, String.class)+"\n");
                Thread.sleep(100);
                return Optional.of(Resources.PRODUCTS_SAMPLE.get(0));
            });

        jobUnderTest.execute();

        final long actualAmountOfPrices = priceRepository.count();

        assertThat(actualAmountOfPrices).isEqualTo(101);

        verify(emailService, times(1)).sendAuditEmail(anyString());
        verify(productExternalService, times(16)).fetchByBarcode(anyString());
        verifyNoMoreInteractions(emailService, productExternalService);
    }
}