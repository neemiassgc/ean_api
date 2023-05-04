package com.api.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CacheManager<TARGET, KEY> {

    private final Map<KEY, TARGET> sourceBucket = new HashMap<>();
    private final Function<TARGET, KEY> keyExtractorFunction;
    private final ConcurrentMap<String, List<KEY>> cache = new ConcurrentHashMap<>();

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private UUID ref = UUID.randomUUID();

    public CacheManager(@NonNull final Function<TARGET, KEY> keyExtractorFunction) {
        this.keyExtractorFunction = keyExtractorFunction;
    }

    public Optional<List<TARGET>> sync(@NonNull final String link, @NonNull final Supplier<List<TARGET>> synchronizerSupplier) {
        if (cache.containsKey(link)) return get(link);
        final List<TARGET> dataToSync = synchronizerSupplier.get();
        if (Objects.isNull(dataToSync) || dataToSync.isEmpty()) return Optional.empty();
        put(link, dataToSync);
        return get(link);
    }

    private void put(@NonNull final String key, @NonNull List<TARGET> listOfData) {
        for (final TARGET target : listOfData)
            sourceBucket.put(keyExtractorFunction.apply(target), target);
        final List<KEY> listOfKeys = listOfData.stream().map(keyExtractorFunction).collect(Collectors.toList());
        cache.put(key, listOfKeys);
    }

    private Optional<List<TARGET>> get(@NonNull final String link) {
        try {
            final List<TARGET> list = cache.get(link)
                .stream()
                .map(key -> Optional.of(sourceBucket.get(key)).orElseThrow())
                .collect(Collectors.toList());
            return Optional.of(list);
        }
        catch (NoSuchElementException ex) {
            return Optional.empty();
        }
    }

    public void evictAll() {
        sourceBucket.clear();
        cache.clear();
        setRef(UUID.randomUUID());
    }

    public boolean containsKey(final String key) {
        return cache.containsKey(key);
    }
}