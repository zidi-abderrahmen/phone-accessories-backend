package com.ia.backend.repository;

import com.ia.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByAccessory_Id(Long accessoryId, Pageable pageable);
    Optional<Review> findByUser_IdAndId(String userId, Long reviewId);
    boolean existsByUser_IdAndAccessory_Id(String userId, Long accessoryId);
}