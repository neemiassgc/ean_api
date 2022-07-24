package com.api.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionStorage {

    @Id
    private Long instance;

    @Column(length = 100)
    private String cookieKey;

    @Column(length = 100)
    private String cookieValue;

    private String ajaxId;

    private LocalDate creationDate;

    @Override
    public String toString() {
        return String.format(
            "SessionStorage{instance=%s, cookieKey=%s, cookieValue=%s, ajaxId=%s, creationDate=%s}",
            instance, cookieKey, cookieValue, ajaxId, creationDate
        );
    }
}
