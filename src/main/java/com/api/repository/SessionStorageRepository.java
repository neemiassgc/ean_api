package com.api.repository;

import com.api.entity.SessionStorage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionStorageRepository extends CrudRepository<SessionStorage, Long> {

    Optional<SessionStorage> findTopByOrderByCreationDateDesc();
}