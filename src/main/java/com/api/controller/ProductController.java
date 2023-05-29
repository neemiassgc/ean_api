package com.api.controller;

import com.api.annotation.Barcode;
import com.api.annotation.ValidExpression;
import com.api.entity.Product;
import com.api.projection.CustomPagination;
import com.api.projection.SimpleProduct;
import com.api.projection.SimpleProductWithStatus;
import com.api.service.CacheManager;
import com.api.service.interfaces.ProductService;
import com.api.utility.DomainUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.api.utility.DomainUtils.calculateNextPage;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
@CrossOrigin
public class ProductController {

    private final ProductService productService;
    private final CacheManager<Product, UUID> productCacheManager;

    @GetMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll() {
        List<EntityModel<SimpleProduct>> responseBody =
            mapAndAddLinks(productService.findAll(Sort.by("description").ascending()));

        return ResponseEntity.ok().headers(getCachingHeaders()).body(responseBody);
    }

    @GetMapping(path = "/products", params = "pag", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(
        @RequestParam(name = "pag") @Pattern(regexp = "\\d{1,2}-\\d{1,2}", message = "must match digit-digit") String pag
    ) {
        final ProductData productData = new ProductData(pag, productService::findAll);
        return feedWithLinks(productData.getProductPage(), controller -> controller.getAll(productData.getNextPage()));
    }

    @GetMapping(path = "/products", params = {"pag", "contains"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllContainingDescription(
        @RequestParam(name = "pag") @Pattern(regexp = "\\d{1,2}-\\d{1,2}", message = "must match digit-digit") String pag,
        @RequestParam("contains") @ValidExpression String contains
    ) {
        final ProductData productData =
            new ProductData(pag, pageable -> productService.findAllByDescriptionIgnoreCaseContaining(contains, pageable));
        return feedWithLinks(
            productData.getProductPage(),
            controller -> controller.getAllContainingDescription(productData.getNextPage(), contains)
        );
    }

    @GetMapping(path = "/products", params = {"pag", "starts-with"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllStartingWithDescription(
        @RequestParam(name = "pag") @Pattern(regexp = "\\d{1,2}-\\d{1,2}", message = "must match digit-digit") String pag,
        @RequestParam("starts-with") @ValidExpression String startsWith
    ) {
        final ProductData productData =
            new ProductData(pag, pageable -> productService.findAllByDescriptionIgnoreCaseStartingWith(startsWith, pageable));
        return feedWithLinks(
            productData.getProductPage(),
            controller -> controller.getAllStartingWithDescription(productData.getNextPage(), startsWith)
        );
    }

    @GetMapping(path = "/products", params = {"pag", "ends-with"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllEndingWithDescription(
        @RequestParam(name = "pag") @Pattern(regexp = "\\d{1,2}-\\d{1,2}", message = "must match digit-digit") String pag,
        @RequestParam("ends-with") @ValidExpression String endsWith
    ) {
        final ProductData productData =
            new ProductData(pag, pageable -> productService.findAllByDescriptionIgnoreCaseEndingWith(endsWith, pageable));
        return feedWithLinks(
            productData.getProductPage(),
            controller -> controller.getAllEndingWithDescription(productData.getNextPage(), endsWith)
        );
    }

    private ResponseEntity<?> feedWithLinks(final Page<Product> productPage, final Function<ProductController, ResponseEntity<?>> function) {
        if (productPage.getContent().isEmpty())
            return ResponseEntity.ok().headers(getCachingHeaders()).body(Collections.emptyList());

        CustomPagination<EntityModel<SimpleProduct>> pagedModel =
            new CustomPagination<>(productPage, mapAndAddLinks(productPage.getContent()));

        pagedModel.addIf(productPage.hasNext(),
            () -> linkTo(function.apply(methodOn(ProductController.class))).withRel("Next page"));

        return ResponseEntity.ok().headers(getCachingHeaders()).body(pagedModel);
    }

    @GetMapping(path = "/products/{barcode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByBarcode(@PathVariable("barcode") @Barcode String barcode) {
        final SimpleProductWithStatus simpleProductWithStatus = productService.getByBarcodeAndSaveIfNecessary(barcode);

        final Link linkToPrices = linkTo(methodOn(PriceController.class).searchByProductBarcode(barcode))
            .withRel("prices");
        final Link linkToSelf = linkTo(methodOn(this.getClass()).getByBarcode(barcode)).withSelfRel();

        final EntityModel<SimpleProduct> simpleProductModel =
            EntityModel.of(simpleProductWithStatus.getSimpleProduct()).add(linkToPrices, linkToSelf);

        return ResponseEntity
            .status(simpleProductWithStatus.getHttpStatus())
            .headers(getCachingHeaders())
            .body(simpleProductModel);
    }

    private HttpHeaders getCachingHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setCacheControl("no-cache, max-age=0, must-revalidate");
        httpHeaders.setETag("\""+productCacheManager.getRef().toString().replace("-", "")+"\"");
        return httpHeaders;
    }

    private List<EntityModel<SimpleProduct>> mapAndAddLinks(List<Product> inputList) {
         return inputList
            .stream()
            .map(Product::toSimpleProduct)
            .map(simpleProduct -> {
                final Link linkToPrices = linkTo(methodOn(PriceController.class)
                    .searchByProductBarcode(simpleProduct.getBarcode()))
                    .withRel("prices");

                final Link linkToSelf = linkTo(methodOn(this.getClass())
                    .getByBarcode(simpleProduct.getBarcode()))
                    .withSelfRel();

                return EntityModel.of(simpleProduct).add(linkToPrices, linkToSelf);
            })
            .collect(Collectors.toList());
    }

    @Getter
    private static class ProductData {
        private final Page<Product> productPage;
        private final String nextPage;

        private ProductData(final String pag, final Function<Pageable, Page<Product>> function) {
            final Pageable pageable = DomainUtils.parsePage(pag, Sort.by("description"));
            this.productPage = function.apply(pageable);
            this.nextPage = calculateNextPage(productPage);
        }
    }
}