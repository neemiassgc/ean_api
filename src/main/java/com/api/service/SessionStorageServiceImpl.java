package com.api.service;

import com.api.entity.SessionStorage;
import com.api.repository.SessionStorageRepository;
import com.api.service.interfaces.SessionStorageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class SessionStorageServiceImpl implements SessionStorageService {

    private final SessionStorageRepository sessionStorageRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<SessionStorage> findTopBy(@NonNull Sort sort) {
        return sessionStorageRepository.findTopBy(sort);
    }

    @Override
    public void save(@NonNull SessionStorage sessionStorage) {
        sessionStorageRepository.save(sessionStorage);
    }
}
