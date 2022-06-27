package com.api.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public final class ErrorTemplate {

    private final Set<Violation> violations;

    @Override
    public String toString() {
        return String.format(
            "violation={%s}",
            violations.stream()
                .map(Violation::toString)
                .collect(Collectors.joining())
        );
    }
}
