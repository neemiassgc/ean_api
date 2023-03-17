package com.api.service;

import org.springframework.lang.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CacheManager<TARGET, KEY> {

    private final Set<TARGET> source;
    private final Function<TARGET, KEY> keyExtractorFunction;
    private final ConcurrentMap<String, List<KEY>> cache = new ConcurrentHashMap<>();

    public CacheManager(@NonNull final Comparator<TARGET> targetComparator, @NonNull final Function<TARGET, KEY> keyExtractorFunction) {
        this.source = new TreeSet<>(targetComparator);
        this.keyExtractorFunction = keyExtractorFunction;
    }

    public void put(@NonNull final String key, @NonNull List<TARGET> value) {
        source.addAll(value);
        final List<KEY> keyList = value.stream().map(keyExtractorFunction).collect(Collectors.toList());
        cache.put(key, keyList);
    }

    public Optional<List<TARGET>> get(@NonNull final String link) {
        try {
            final List<TARGET> list = Optional.ofNullable(cache.get(link))
                .orElseThrow()
                .stream()
                .map(key -> findByKey(key).orElseThrow())
                .collect(Collectors.toList());
            return Optional.of(list);
        }
        catch (NoSuchElementException ex) {
            return Optional.empty();
        }
    }

    private Optional<TARGET> findByKey(final KEY key) {
        for (final TARGET item : source)
            if (keyExtractorFunction.apply(item).equals(key))
                return Optional.of(item);
        return Optional.empty();
    }

    public void evictAll() {
        source.clear();
    }

    public boolean containsKey(final String key) {
        return cache.containsKey(key);
    }
}
