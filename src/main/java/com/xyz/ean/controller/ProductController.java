package com.xyz.ean.controller;

import com.xyz.ean.service.DomainMapper;
import com.xyz.ean.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductController {

    private final ProductService productService;
    private final DomainMapper domainMapper;
