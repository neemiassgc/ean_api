package com.api.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SessionStorageSequence")
    @SequenceGenerator(
        name = "SessionStorageSequence",
        sequenceName = "session_storage_seq"
    )
    @Setter(value = AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private Long instance;

    @Column(length = 100, nullable = false)
    private String cookieKey;

    @Column(length = 100, nullable = false)
    private String cookieValue;

    @Column(nullable = false)
    private String ajaxId;

    @Column(nullable = false)
    private LocalDate creationDate;

    @Override
    public String toString() {
        return String.format(
            "SessionStorage{instance=%s, cookieKey=%s, cookieValue=%s, ajaxId=%s, creationDate=%s}",
            instance, cookieKey, cookieValue, ajaxId, creationDate
        );
    }
}
