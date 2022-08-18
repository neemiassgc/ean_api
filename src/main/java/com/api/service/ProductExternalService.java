package com.api.service;

import com.api.components.ProductSessionInstance;
import com.api.entity.Product;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Log4j2
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductExternalService {

    private final RestTemplate restTemplate;
    private final ProductSessionInstance productSessionInstance;

    private HttpHeaders buildHttpHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        return httpHeaders;
    }

    private MultiValueMap<String, String> buildMultipartBodyWithBarcode(final String barcode) {
        final MultiValueMap<String, String> multipartBody = new LinkedMultiValueMap<>(6);
        multipartBody.add("p_request", productSessionInstance.getSessionInstance().getAjaxIdentifier());
        multipartBody.add("p_flow_id", "171");
        multipartBody.add("p_flow_step_id", "2");
        multipartBody.add("p_instance", productSessionInstance.getSessionInstance().getSessionId());
        multipartBody.add("p_debug", "");
        multipartBody.addAll("p_arg_names", List.of("P2_CURSOR", "P2_LOJA_ID", "P2_COD1"));
        multipartBody.addAll("p_arg_values", List.of("B", "221", barcode));
        return multipartBody;
    }

    public Optional<Product> fetchByBarcode(@NonNull final String barcode) {
        final HttpEntity<MultiValueMap<String, String>> httpEntity =
            new HttpEntity<>(buildMultipartBodyWithBarcode(barcode), buildHttpHeaders());

        log.info("Fetching for information about a product by barcode "+barcode);

        return treatError(() ->
            restTemplate.postForObject("/wwv_flow.show", httpEntity, Product.class),
            barcode
        );
    }

    private Optional<Product> treatError(final Supplier<Product> productSupplier, final String barcode) {
        try {
            return Optional.of(productSupplier.get());
        }
        catch (Exception exception) {
            if (verifyIfProductIsNotFoundException(exception)) return Optional.empty();

            productSessionInstance.reloadSessionInstance();
            return fetchByBarcode(barcode);
        }
    }

    private boolean verifyIfProductIsNotFoundException(final Exception exception) {
        return exception instanceof IllegalStateException && exception.getMessage().equals("Item name is empty");
    }
}
