package com.api.projection;

import org.springframework.data.domain.Page;

import static com.api.projection.Projection.Paged;

public class ProjectionFactory {

    public static <T> Paged<T> paged(Page<?> page, final T content) {
        return new Paged<T>() {
            @Override
            public int getCurrentPage() {
                return page.getNumber();
            }

            @Override
            public int getTotalPages() {
                return page.getTotalPages();
            }

            @Override
            public int getNumberOfItems() {
                return page.getNumberOfElements();
            }

            @Override
            public boolean getHasNext() {
                return page.hasNext();
            }

            @Override
            public T getContent() {
                return content;
            }
        };
    }

}
