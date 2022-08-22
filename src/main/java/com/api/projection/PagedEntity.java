package com.api.projection;

import lombok.Builder;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Getter
@Builder
public final class PagedEntity<T> extends RepresentationModel<PagedEntity<T>> {

    private final int currentPage;
    private final int totalOfPages;
    private final int numberOfItems;
    private final boolean hasNext;
    private final List<T> content;
}
