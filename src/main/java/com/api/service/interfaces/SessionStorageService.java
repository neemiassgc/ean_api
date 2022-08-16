package com.api.service.interfaces;

import com.api.entity.SessionStorage;
import org.springframework.data.domain.Sort;

import java.util.Optional;

public interface SessionStorageService {

    Optional<SessionStorage> findTopBy(Sort sort);

    void save(SessionStorage sessionStorage);
}
