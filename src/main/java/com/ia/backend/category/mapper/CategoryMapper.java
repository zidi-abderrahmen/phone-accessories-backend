package com.ia.backend.category.mapper;

import com.ia.backend.category.dto.CategoryRequest;
import com.ia.backend.category.dto.CategoryResponse;
import com.ia.backend.category.entity.Category;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    Category toEntity(CategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCategory(CategoryRequest request, @MappingTarget Category category);

}