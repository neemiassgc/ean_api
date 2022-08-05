package com.api.controller;

import com.api.annotation.Barcode;
import com.api.entity.Product;
import com.api.pojo.DomainUtils;
import com.api.projection.ProjectionFactory;
import com.api.repository.ProductRepository;
import com.api.service.DomainMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.api.projection.Projection.Paged;
import static com.api.projection.Projection.SimpleProduct;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
public class ProductController {

    private final ProductRepository productRepository;
    private final DomainMapper domainMapper;

    @GetMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public RepresentationModel<?> getAll(
        @RequestParam(name = "pag", required = false)
        @Pattern(regexp = "\\d{1,3}-\\d{1,3}", message = "pag must match digit-digit") String pag
    ) {
        if (Objects.isNull(pag)) {
            final List<EntityModel<SimpleProduct>> modelList =
                domainMapper.mapToSimpleProductList(productRepository.findAll())
                    .stream()
                    .map(simpleProduct -> {
                        final Link link = linkTo(methodOn(PriceController.class).searchByProductBarcode(simpleProduct.getBarcode()))
                            .withRel("prices");

                        return EntityModel.of(simpleProduct).add(link);
                    }).collect(Collectors.toList());

            return CollectionModel.of(modelList);
        }

        final Page<Product> productPage = productRepository.findAll(DomainUtils.parsePage(pag));

        final List<EntityModel<SimpleProduct>> modelList = productPage.getContent()
            .stream()
            .map(domainMapper::mapToSimpleProduct)
            .map(simpleProduct -> {
                final Link link = linkTo(methodOn(PriceController.class).searchByProductBarcode(simpleProduct.getBarcode()))
                    .withRel("prices");

                return EntityModel.of(simpleProduct).add(link);
            }).collect(Collectors.toList());

        final Link linkToNextPage =
            linkTo(methodOn(this.getClass()).getAll((productPage.getNumber() + 1)+"-"+productPage.getSize()))
                .withRel("next page");

        final EntityModel<Paged<List<EntityModel<SimpleProduct>>>> entityModelToResponse =
            EntityModel.of(ProjectionFactory.paged(productPage, modelList));

        if (productPage.hasNext()) entityModelToResponse.add(linkToNextPage);

        return entityModelToResponse;
    }

    @GetMapping(path = "/products/{barcode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel<SimpleProduct> getByBarcode(@PathVariable("barcode") @Barcode String barcode) {
        final Product productToProcess = productRepository.processByBarcode(barcode);
        final Link linkToPrices = linkTo(methodOn(PriceController.class).searchByProductBarcode(barcode))
            .withRel("prices");
        final SimpleProduct simpleProductToResponse = domainMapper.mapToSimpleProduct(productToProcess);

        return EntityModel.of(simpleProductToResponse, linkToPrices);
    }
}
