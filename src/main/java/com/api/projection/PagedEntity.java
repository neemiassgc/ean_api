package com.api.projection;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;

import java.util.List;

@Getter
public final class PagedEntity<T> extends CollectionModel<T> {

    private final int currentPage;
    private final int totalOfPages;
    private final int numberOfItems;
    private final boolean hasNext;

    public PagedEntity(@NonNull final Page<?> page, @NonNull List<T> content) {
        super(content);
        this.currentPage = page.getNumber();
        this.totalOfPages = page.getTotalPages();
        this.numberOfItems = page.getNumberOfElements();
        this.hasNext = page.hasNext();
    }
}