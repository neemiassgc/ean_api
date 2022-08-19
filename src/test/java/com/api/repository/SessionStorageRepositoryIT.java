package com.api.repository;

import com.api.entity.SessionStorage;
import com.api.service.interfaces.SessionStorageService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

@SpringBootTest
public class SessionStorageRepositoryIT {

    @Autowired
    private SessionStorageService sessionStorageService;

    @Test
    @DisplayName("Testing findTopBy method")
    void should_return_a_session() {
        final Sort.Order orderByCreationDateDesc = Sort.Order.desc("creationDate");
        final Sort.Order orderByIdAsc = Sort.Order.asc("id");
        final Optional<SessionStorage> actualOptional =
            sessionStorageService.findTopBy(Sort.by(orderByCreationDateDesc, orderByIdAsc));

        Assertions.assertThat(actualOptional).isNotNull();
        Assertions.assertThat(actualOptional).isPresent();
        Assertions.assertThat(actualOptional.get().getCreationDate()).isEqualTo(LocalDate.of(2022, Month.JULY, 21));
        Assertions.assertThat(actualOptional.get().getCookieKey()).isEqualTo("COOKIE_SAVEG_MOBILE");
        Assertions.assertThat(actualOptional.get().getCookieValue()).isEqualTo("ORA_WWV-1Ad8ftQ9pjqwmineownr");
        Assertions.assertThat(actualOptional.get().getInstance()).isEqualTo(4078553012341L);
        Assertions.assertThat(actualOptional.get().getAjaxId()).isEqualTo("87C8742DE5B50364E11627DF664BB0QWEJPIOQJW983QWEJASDFJL5331F");
    }
}
