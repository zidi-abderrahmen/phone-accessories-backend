package com.ia.backend.service;

import com.ia.backend.dto.review.ReviewRequest;
import com.ia.backend.dto.review.ReviewResponse;
import com.ia.backend.entity.Accessory;
import com.ia.backend.entity.Review;
import com.ia.backend.entity.User;
import com.ia.backend.exception.AlreadyExistException;
import com.ia.backend.exception.NotFoundException;
import com.ia.backend.mapper.ReviewMapper;
import com.ia.backend.repository.AccessoryRepository;
import com.ia.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AccessoryRepository accessoryRepository;
    private final ReviewMapper reviewMapper;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviewsByAccessoryId(Pageable pageable, Long accessoryId) {
        User currentUser = userService.getCurrentUserEntity();

        log.debug("Fetching reviews for accessory with id: {}", accessoryId);
        return reviewRepository.findAllByAccessory_Id(accessoryId, pageable)
                .map(review -> reviewMapper.toResponse(
                        review,
                        Objects.equals(review.getUser().getId(), currentUser.getId())
                ));
    }

    @Transactional
    public ReviewResponse createReview(Long accessoryId, ReviewRequest request) {
        User currentUser = userService.getCurrentUserEntity();

        Accessory existingAccessory = accessoryRepository.findById(accessoryId)
                .orElseThrow(() -> notFoundException("Accessory not found"));

        if (reviewRepository.existsByUser_IdAndAccessory_Id(currentUser.getId(), accessoryId)) {
            log.error("User {} already reviewed accessory {}", currentUser.getId(), accessoryId);
            throw new AlreadyExistException("You have already reviewed this accessory.");
        }

        log.debug("Creating new review for accessory with id: {}", accessoryId);
        Review newReview = reviewMapper.toEntity(request);
        newReview.setUser(currentUser);
        newReview.setAccessory(existingAccessory);

        return reviewMapper.toResponse(reviewRepository.save(newReview), true);
    }

    @Transactional
    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        User currentUser = userService.getCurrentUserEntity();

        Review existingReview = reviewRepository.findByUser_IdAndId(currentUser.getId(), id)
                .orElseThrow(() -> notFoundException("Review not found"));

        log.debug("Updating review with id: {}", id);
        reviewMapper.updateReview(request, existingReview);

        reviewRepository.saveAndFlush(existingReview);

        return reviewMapper.toResponse(existingReview, true);
    }

    @Transactional
    public void deleteReview(Long id) {
        User currentUser = userService.getCurrentUserEntity();

        Review existingReview = reviewRepository.findByUser_IdAndId(currentUser.getId(), id)
                .orElseThrow(() -> notFoundException("Review not found"));

        log.debug("Deleting review with id: {}", id);
        reviewRepository.delete(existingReview);
    }

    private NotFoundException notFoundException(String message) {
        log.error(message);
        return new NotFoundException(message);
    }
}