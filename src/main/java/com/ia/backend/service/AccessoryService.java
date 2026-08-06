package com.ia.backend.service;

import com.ia.backend.dto.accessory.AccessoryRequest;
import com.ia.backend.dto.accessory.AccessoryResponse;
import com.ia.backend.entity.Accessory;
import com.ia.backend.entity.Category;
import com.ia.backend.exception.AlreadyExistException;
import com.ia.backend.exception.NotFoundException;
import com.ia.backend.mapper.AccessoryMapper;
import com.ia.backend.repository.AccessoryRepository;
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
}