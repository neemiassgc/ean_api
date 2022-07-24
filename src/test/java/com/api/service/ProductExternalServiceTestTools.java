package com.api.service;

import com.api.entity.SessionStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Collectors;

final class ProductExternalServiceTestTools {

    static <T> String getGenericHtmlContent(final T t) {
        final InputStreamReader isr = new InputStreamReader(
            Objects.requireNonNull(t.getClass().getClassLoader().getResourceAsStream("html_for_test.html"))
        );

        try (BufferedReader br = new BufferedReader(isr)) {
            return br.lines().collect(Collectors.joining("\n"));
        }
        catch (IOException ignored) {
            return "";
        }
    }

    static SessionStorage newSessionStorage(final LocalDate localDate) {
        return SessionStorage.builder()
            .ajaxId("810EFA05686BAB7BE4DCFF431D456E4DF1DD4365A762A8F2AB07E15CD9902459")
            .cookieKey("COOKIE_SAVEG_MOBILE")
            .cookieValue("ORA_WWV-dxVoldWhIfN2TviPcS9yhmcI")
            .creationDate(localDate)
            .instance(23515127806187L)
            .build();
    }
}
