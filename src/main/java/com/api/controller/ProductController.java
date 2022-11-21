package com.api.controller;

import com.api.annotation.Barcode;
import com.api.component.DomainUtils;
import com.api.entity.Product;
import com.api.projection.CustomPagination;
import com.api.projection.SimpleProduct;
import com.api.projection.SimpleProductWithStatus;
import com.api.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
@CrossOrigin
public class ProductController {

    private final ProductService productService;

    @GetMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll() {
        List<EntityModel<SimpleProduct>> responseBody =
            mapAndAddLinks(productService.findAll(Sort.by("description").ascending()));

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping(path = "/products", params = "pag", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllPaged(
        @RequestParam(name = "pag") @Pattern(regexp = "\\d-\\d", message = "must match digit-digit") String pag
    ) {
        final Page<Product> productPage =
            productService.findAll(DomainUtils.parsePage(pag, Sort.by("description").ascending()));

        return feedWithLinks(productPage);
    }

    @GetMapping(path = "/products", params = {"pag", "contains"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllPagedContainingDescription(
        @RequestParam(name = "pag") @Pattern(regexp = "\\d-\\d", message = "must match digit-digit") String pag,
        @RequestParam("contains") @NotNull String contains
    ) {
        final Pageable pageable = DomainUtils.parsePage(pag, Sort.by("description"));
        final Page<Product> productPage = productService.findAllByDescriptionIgnoreCaseContaining(contains, pageable);

        return feedWithLinks(productPage);
    }

    @GetMapping(path = "/products", params = {"pag", "startsWith"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllPagedStartingWithDescription(
            @RequestParam(name = "pag") @Pattern(regexp = "\\d-\\d", message = "must match digit-digit") String pag,
            @RequestParam("contains") @NotNull String startsWith
    ) {
        final Pageable pageable = DomainUtils.parsePage(pag, Sort.by("description"));
        final Page<Product> productPage = productService.findAllByDescriptionIgnoreCaseStartingWith(startsWith, pageable);

        return feedWithLinks(productPage);
    }

    private ResponseEntity<?> feedWithLinks(final Page<Product> productPage) {
        if (productPage.getContent().isEmpty()) return ResponseEntity.ok(Collections.emptyList());

        CustomPagination<EntityModel<SimpleProduct>> pagedModel =
            new CustomPagination<>(productPage, mapAndAddLinks(productPage.getContent()));

        pagedModel.addIf(productPage.hasNext(), () ->
            linkTo(methodOn(this.getClass()).getAllPaged((productPage.getNumber() + 1)+"-"+productPage.getSize()))
                .withRel("next page")
        );

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping(path = "/products/{barcode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByBarcode(@PathVariable("barcode") @Barcode String barcode) {
        final SimpleProductWithStatus simpleProductWithStatus = productService.getByBarcodeAndSaveIfNecessary(barcode);

        final Link linkToPrices = linkTo(methodOn(PriceController.class).searchByProductBarcode(barcode))
            .withRel("prices");
        final Link linkToSelf = linkTo(methodOn(this.getClass()).getByBarcode(barcode)).withSelfRel();

        final EntityModel<SimpleProduct> simpleProductModel =
            EntityModel.of(simpleProductWithStatus.getSimpleProduct()).add(linkToPrices, linkToSelf);

        return ResponseEntity.status(simpleProductWithStatus.getHttpStatus()).body(simpleProductModel);
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
}