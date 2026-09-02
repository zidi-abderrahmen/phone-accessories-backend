package com.ia.backend.accessory.service;

import com.ia.backend.accessory.repository.specification.AccessorySpecification;
import com.ia.backend.accessory.dto.AccessoryRequest;
import com.ia.backend.accessory.dto.AccessoryResponse;
import com.ia.backend.accessory.dto.SearchRequest;
import com.ia.backend.accessory.entity.Accessory;
import com.ia.backend.category.entity.Category;
import com.ia.backend.common.exception.AlreadyExistException;
import com.ia.backend.common.exception.NotFoundException;
import com.ia.backend.accessory.mapper.AccessoryMapper;
import com.ia.backend.accessory.repository.AccessoryRepository;
import com.ia.backend.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessoryService {

    private final AccessoryRepository accessoryRepository;
    private final AccessoryMapper accessoryMapper;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<AccessoryResponse> getAllAccessories(Pageable pageable) {
        log.debug("Fetching a page of accessories");
        return accessoryRepository.findAll(pageable)
                .map(accessoryMapper::toDto);
    }

    @Transactional(readOnly = true)
    public AccessoryResponse getAccessoryById(Long id) {
        log.debug("Fetching accessory with id: {}", id);
        return accessoryRepository.findById(id)
                .map(accessoryMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Accessory not found."));
    }

    @Transactional
    public AccessoryResponse createAccessory(AccessoryRequest accessoryRequest) {
        if (accessoryRepository.existsByProductCode(accessoryRequest.productCode())) {
            throw new AlreadyExistException("Product Code already exists.");
        }

        Category existingCategory = validateCategory(accessoryRequest.categoryId());

        Accessory newAccessory = accessoryMapper.toEntity(accessoryRequest);
        newAccessory.setCategory(existingCategory);

        Accessory savedAccessory = accessoryRepository.save(newAccessory);

        log.info("Created new accessory with id: {}", savedAccessory.getId());
        return accessoryMapper.toDto(savedAccessory);
    }

    @Transactional
    public AccessoryResponse updateAccessory(Long id, AccessoryRequest accessoryRequest) {
        Accessory existingAccessory = accessoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Accessory not found."));

        if (!existingAccessory.getProductCode().equals(accessoryRequest.productCode())
                && accessoryRepository.existsByProductCode(accessoryRequest.productCode())) {
            throw new AlreadyExistException("Product Code already exists.");
        }

        Category existingCategory = validateCategory(accessoryRequest.categoryId());

        accessoryMapper.updateAccessory(accessoryRequest, existingAccessory);
        existingAccessory.setCategory(existingCategory);

        log.info("Updated accessory with id: {}", id);
        return accessoryMapper.toDto(existingAccessory);
    }

    private Category validateCategory(Long categoryId) {
        log.debug("Validating category with id: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found."));
    }

    @Transactional
    public void deleteAccessory(Long id) {
        if (!accessoryRepository.existsById(id)) {
            log.error("Attempted to delete non-existent accessory with id: {}", id);
            throw new NotFoundException("Accessory not found.");
        }

        accessoryRepository.deleteById(id);
        log.info("Deleted accessory with id: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<AccessoryResponse> filterAccessories(SearchRequest request, Pageable pageable) {
        Specification<Accessory> spec = Specification
                .where(AccessorySpecification.hasCategory(request.categoryId()))
                .and(AccessorySpecification.hasKeyword(request.keyword()))
                .and(AccessorySpecification.priceBetween(request.minPrice(), request.maxPrice()))
                .and(AccessorySpecification.inStock(request.inStock()));

        return accessoryRepository.findAll(spec, pageable)
                .map(accessoryMapper::toDto);
    }
}