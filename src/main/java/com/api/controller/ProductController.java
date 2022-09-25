package com.api.controller;

import com.api.annotation.Barcode;
import com.api.component.DomainUtils;
import com.api.entity.Product;
import com.api.projection.PagedEntity;
import com.api.projection.SimpleProduct;
import com.api.projection.SimpleProductWithStatus;
import com.api.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
            makeMappingAndLinks(productService.findAll(Sort.by("description").ascending()));

        return ResponseEntity.ok(CollectionModel.of(responseBody));
    }

    @GetMapping(path = "/products", params = "pag", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPagedAll(@RequestParam(name = "pag") String pag) {
        final Page<Product> productPage =
            productService.findAll(DomainUtils.parsePage(pag, Sort.by("description").ascending()));

        if (productPage.getContent().isEmpty()) return ResponseEntity.ok(Collections.emptyList());

        PagedEntity<EntityModel<SimpleProduct>> pagedModel =
            new PagedEntity<>(productPage, makeMappingAndLinks(productPage.getContent()));

        pagedModel.addIf(productPage.hasNext(), () ->
            linkTo(methodOn(this.getClass()).getPagedAll((productPage.getNumber() + 1)+"-"+productPage.getSize()))
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

        final EntityModel<SimpleProduct> responseBody =
            EntityModel.of(simpleProductWithStatus.getSimpleProduct(), linkToPrices, linkToSelf);

        return ResponseEntity.status(simpleProductWithStatus.getHttpStatus()).body(responseBody);
    }

    private List<EntityModel<SimpleProduct>> makeMappingAndLinks(List<Product> inputList) {
         return inputList
            .stream()
            .map(Product::toSimpleProduct)
            .map(it -> {
                final Link linkToPrices = linkTo(methodOn(PriceController.class)
                    .searchByProductBarcode(it.getBarcode()))
                    .withRel("prices");

                final Link linkToSelf = linkTo(methodOn(this.getClass())
                    .getByBarcode(it.getBarcode()))
                    .withSelfRel();

                return EntityModel.of(it).add(linkToPrices, linkToSelf);
            })
            .collect(Collectors.toList());
    }
}