package com.ia.backend.service;

import com.ia.backend.dto.category.CategoryRequest;
import com.ia.backend.dto.category.CategoryResponse;
import com.ia.backend.entity.Category;
import com.ia.backend.exception.AlreadyExistException;
import com.ia.backend.exception.NotFoundException;
import com.ia.backend.mapper.CategoryMapper;
import com.ia.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        log.debug("Fetching all categories");
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        log.debug("Fetching category with id: {}", id);
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            log.error("Category already exists");
            throw new AlreadyExistException("Category already exists");
        }

        Category category = categoryMapper.toEntity(request);
        categoryRepository.save(category);

        log.info("Created new category with id: {}", category.getId());
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.debug("Updating category with id: {}", id);
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (!existingCategory.getName().equalsIgnoreCase(request.name())
                && categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new AlreadyExistException("Category name already exists");
        }

        categoryMapper.updateCategory(request, existingCategory);

        log.info("Updated category with id: {}", id);
        return categoryMapper.toResponse(existingCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            log.error("Attempted to delete non-existent category with id: {}", id);
            throw new NotFoundException("Category not found");
        }

        categoryRepository.deleteById(id);
        log.info("Deleted category with id: {}", id);
    }
}