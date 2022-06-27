package com.api.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class Violation {

    private final String field;
    private final String violationMessage;

    @Override
    public String toString() {
        return String.format("'%s' %s", field, violationMessage);
    }
}