package com.ia.backend.mapper;

import com.ia.backend.dto.review.ReviewRequest;
import com.ia.backend.dto.review.ReviewResponse;
import com.ia.backend.entity.Review;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ReviewMapper {

    ReviewResponse toResponse(Review review, boolean mine);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "accessory", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReview(ReviewRequest request, @MappingTarget Review review);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "accessory", ignore = true)
    Review toEntity(ReviewRequest request);
}