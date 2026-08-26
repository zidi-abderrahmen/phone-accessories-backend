package com.ia.backend.controller;

import com.ia.backend.dto.review.ReviewRequest;
import com.ia.backend.dto.review.ReviewResponse;
import com.ia.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/accessory/{id}")
    public ResponseEntity<Page<ReviewResponse>> getAllReviewsByAccessoryId(Pageable pageable, @PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getAllReviewsByAccessoryId(pageable, id));
    }

    @PostMapping("/accessory/{id}")
    public ResponseEntity<ReviewResponse> createReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(id, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}