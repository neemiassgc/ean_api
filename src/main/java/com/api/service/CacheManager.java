package com.api.service;

import com.api.entity.Product;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class CacheManager {

    private final Set<Product> source = new TreeSet<>(Comparator.comparing(Product::getDescription));
    private final ConcurrentMap<String, List<UUID>> cache = new ConcurrentHashMap<>();
}
