package com.ia.backend.review.mapper;

import com.ia.backend.review.dto.ReviewRequest;
import com.ia.backend.review.dto.ReviewResponse;
import com.ia.backend.review.entity.Review;
import com.ia.backend.user.mapper.UserMapper;
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