package com.xyz.ean.controller;

import com.xyz.ean.dto.EanCodeRequestDTO;
import com.xyz.ean.dto.ProductResponseDTO;
import com.xyz.ean.entity.Product;
import com.xyz.ean.service.DomainMapper;
import com.xyz.ean.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductController {

    private final ProductService productService;
    private final DomainMapper domainMapper;

    @PostMapping(path = "/products", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponseDTO createProduct(@RequestBody EanCodeRequestDTO eanCodeRequestDTO) {
        final Product fetchedProduct = productService.saveByEanCode(eanCodeRequestDTO.getEanCode());
        return domainMapper.mapToDto(fetchedProduct);
    }
}
