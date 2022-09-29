package com.api.projection;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Getter
public final class CustomPagination<T> extends RepresentationModel<EntityModel<T>> {

    private final int currentPage;
    private final int totalOfPages;
    private final int currentCountOfItems;
    private final long totalOfItems;
    private final boolean hasNext;
    private final List<T> content;

    public CustomPagination(@NonNull final Page<?> page, @NonNull List<T> content) {
        this.content = content;
        this.currentPage = page.getNumber();
        this.totalOfPages = page.getTotalPages();
        this.currentCountOfItems = page.getNumberOfElements();
        this.hasNext = page.hasNext();
        this.totalOfItems = page.getTotalElements();
    }
}