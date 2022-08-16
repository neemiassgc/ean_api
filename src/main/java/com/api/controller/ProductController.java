package com.api.controller;

import com.api.annotation.Barcode;
import com.api.entity.Product;
import com.api.pojo.DomainUtils;
import com.api.projection.ProjectionFactory;
import com.api.repository.ProductRepository;
import com.api.service.DomainMapper;
import com.api.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.api.projection.Projection.Paged;
import static com.api.projection.Projection.SimpleProduct;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
@CrossOrigin
public class ProductController {

    private final ProductService productService;
    private final DomainMapper domainMapper;

    @GetMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll() {
        final List<EntityModel<SimpleProduct>> responseBody =
            makeMapping(productService.findAll(Sort.by("description").ascending()));

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping(path = "/products", params = "pag", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(@RequestParam(name = "pag") String pag) {
        final Page<Product> productPage =
            productService.findAll(DomainUtils.parsePage(pag, Sort.by("description").ascending()));

        if (productPage.getContent().isEmpty()) return ResponseEntity.ok(Collections.emptyList());

        final List<EntityModel<SimpleProduct>> modelList = makeMapping(productPage.getContent());

        final EntityModel<Paged<EntityModel<SimpleProduct>>> responseBody =
            EntityModel.of(ProjectionFactory.paged(productPage, modelList));

        responseBody.addIf(productPage.hasNext(), () ->
            linkTo(methodOn(this.getClass()).getAll((productPage.getNumber() + 1)+"-"+productPage.getSize()))
                .withRel("next page")
        );

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping(path = "/products/{barcode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByBarcode(@PathVariable("barcode") @Barcode String barcode) {
        final Product productToProcess = productService.processByBarcode(barcode);

        final Link linkToPrices = linkTo(methodOn(PriceController.class).searchByProductBarcode(barcode))
            .withRel("prices");

        final Link linkToSelf = linkTo(methodOn(this.getClass()).getByBarcode(barcode)).withSelfRel();

        final SimpleProduct simpleProductToResponse = domainMapper.mapToSimpleProduct(productToProcess);

        final EntityModel<SimpleProduct> responseBody =
            EntityModel.of(simpleProductToResponse, linkToPrices, linkToSelf);

        return ResponseEntity.ok(responseBody);
    }

    private List<EntityModel<SimpleProduct>> makeMapping(List<Product> inputList) {
        return domainMapper.mapToSimpleProductList(inputList)
            .stream()
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