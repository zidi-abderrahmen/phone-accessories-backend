package com.ia.backend.service;

import com.ia.backend.accessory.dto.AccessoryRequest;
import com.ia.backend.accessory.dto.AccessoryResponse;
import com.ia.backend.accessory.entity.Accessory;
import com.ia.backend.accessory.service.AccessoryService;
import com.ia.backend.category.entity.Category;
import com.ia.backend.common.exception.AlreadyExistException;
import com.ia.backend.common.exception.NotFoundException;
import com.ia.backend.accessory.mapper.AccessoryMapper;
import com.ia.backend.category.mapper.CategoryMapper;
import com.ia.backend.accessory.repository.AccessoryRepository;
import com.ia.backend.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessoryServiceTest {

    @Mock
    private AccessoryRepository accessoryRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AccessoryMapper accessoryMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private AccessoryService accessoryService;

    private Accessory accessory;
    private AccessoryRequest accessoryRequest;
    private AccessoryResponse accessoryResponse;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Category 1");
        category.setDescription("Description 1");

        accessory = new Accessory();
        accessory.setId(1L);
        accessory.setTitle("Accessory 1");
        accessory.setDescription("Description 1");
        accessory.setPrice(new BigDecimal("12.34"));
        accessory.setStock(5);
        accessory.setCategory(category);
        accessory.setProductCode("123456");
        accessory.setCreatedAt(LocalDateTime.now());
        accessory.setUpdatedAt(LocalDateTime.now());

        accessoryRequest = new AccessoryRequest(
                "",
                "Accessory 1",
                "Description 2",
                new BigDecimal("78.99"),
                4,
                1L,
                "1542648"
        );
        accessoryResponse = new AccessoryResponse(
                1L,
                "",
                "Accessory 1",
                "Description 2",
                new BigDecimal("78.99"),
                4,
                categoryMapper.toResponse(category),
                "1542648",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void getAccessoryById_whenExists_shouldReturnResponse() {
        when(accessoryRepository.findById(1L)).thenReturn(Optional.of(accessory));
        when(accessoryMapper.toDto(accessory)).thenReturn(accessoryResponse);

        AccessoryResponse result = accessoryService.getAccessoryById(1L);

        assertThat(result).isEqualTo(accessoryResponse);
        verify(accessoryRepository).findById(1L);
    }

    @Test
    void getAccessoryById_whenNotExists_shouldThrowException() {
        when(accessoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessoryService.getAccessoryById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Accessory not found.");
    }

    @Test
    void createAccessory_whenProductCodeIsNew_shouldSaveAndReturn() {
        when(accessoryRepository.existsByProductCode(any())).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(accessoryMapper.toEntity(accessoryRequest)).thenReturn(accessory);
        when(accessoryRepository.save(accessory)).thenReturn(accessory);
        when(accessoryMapper.toDto(accessory)).thenReturn(accessoryResponse);

        AccessoryResponse result = accessoryService.createAccessory(accessoryRequest);

        assertThat(result).isEqualTo(accessoryResponse);
        verify(accessoryRepository).save(accessory);
    }

    @Test
    void createAccessory_whenProductCodeExists_shouldThrowException() {
        when(accessoryRepository.existsByProductCode(any())).thenReturn(true);

        assertThatThrownBy(() -> accessoryService.createAccessory(accessoryRequest))
                .isInstanceOf(AlreadyExistException.class)
                .hasMessage("Product Code already exists.");

        verify(accessoryRepository, never()).save(any());
    }

    @Test
    void updateAccessory_whenExists_shouldUpdateAndReturn() {
        when(accessoryRepository.findById(1L)).thenReturn(Optional.of(accessory));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        doNothing().when(accessoryMapper).updateAccessory(accessoryRequest, accessory);
        when(accessoryMapper.toDto(accessory)).thenReturn(accessoryResponse);

        AccessoryResponse result = accessoryService.updateAccessory(1L, accessoryRequest);

        assertThat(result).isEqualTo(accessoryResponse);
        verify(accessoryRepository).findById(1L);
        verify(accessoryMapper).updateAccessory(accessoryRequest, accessory);
        verify(accessoryMapper).toDto(accessory);
    }

    @Test
    void updateAccessory_whenNotExists_shouldThrowException() {
        when(accessoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accessoryService.updateAccessory(99L, accessoryRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Accessory not found.");

        verify(accessoryMapper, never()).updateAccessory(any(), any());
        verify(accessoryMapper, never()).toDto(any());
    }

    @Test
    void deleteAccessory_whenExists_shouldDelete() {
        when(accessoryRepository.existsById(1L)).thenReturn(true);

        accessoryService.deleteAccessory(1L);

        verify(accessoryRepository).deleteById(1L);
    }

    @Test
    void deleteAccessory_whenNotExists_shouldThrowException() {
        when(accessoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> accessoryService.deleteAccessory(99L))
                .isInstanceOf(NotFoundException.class);

        verify(accessoryRepository, never()).deleteById(any());
    }

    @Test
    void getAllAccessories_shouldReturnPageOfResponses() {
        Pageable pageable = Pageable.unpaged();
        Page<Accessory> accessoryPage = new PageImpl<>(List.of(accessory));

        when(accessoryRepository.findAll(pageable)).thenReturn(accessoryPage);
        when(accessoryMapper.toDto(accessory)).thenReturn(accessoryResponse);

        Page<AccessoryResponse> result = accessoryService.getAllAccessories(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(accessoryResponse);
    }
}