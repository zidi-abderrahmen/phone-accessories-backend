package com.ia.backend.accessory.repository;

import com.ia.backend.accessory.entity.Accessory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccessoryRepository extends JpaRepository<Accessory, Long>, JpaSpecificationExecutor<Accessory> {

    boolean existsByProductCode(String productCode);

    @EntityGraph(attributePaths = "category")
    Page<Accessory> findAllByCategory_Id(Long categoryId, Pageable pageable);
}