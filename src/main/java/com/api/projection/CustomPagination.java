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
    private final int numberOfItems;
    private final long totalOfItems;
    private final boolean hasNext;
    private final List<EntityModel<T>> content;

    public CustomPagination(@NonNull final Page<?> page, @NonNull List<EntityModel<T>> content) {
        this.content = content;
        this.currentPage = page.getNumber();
        this.totalOfPages = page.getTotalPages();
        this.numberOfItems = page.getNumberOfElements();
        this.hasNext = page.hasNext();
        this.totalOfItems = page.getTotalElements();
    }
}