package com.api.repository;

import com.api.entity.Price;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PriceRepository extends JpaRepository<Price, UUID> {

    List<Price> findByProductBarcode(String barcode, Sort sort);

    List<Price> findByProductBarcode(String barcode, Pageable pageable);
}
