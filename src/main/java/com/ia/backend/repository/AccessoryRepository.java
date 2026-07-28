package com.ia.backend.repository;

import com.ia.backend.entity.Accessory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessoryRepository extends JpaRepository<Accessory, Long> {

    boolean existsByProductCode(String productCode);
}