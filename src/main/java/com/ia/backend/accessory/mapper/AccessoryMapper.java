package com.ia.backend.accessory.mapper;

import com.ia.backend.accessory.dto.AccessoryRequest;
import com.ia.backend.accessory.dto.AccessoryResponse;
import com.ia.backend.accessory.entity.Accessory;
import com.ia.backend.category.mapper.CategoryMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface AccessoryMapper {

    AccessoryResponse toDto(Accessory accessory);

    @Mapping(target = "category", ignore = true)
    Accessory toEntity(AccessoryRequest accessoryRequest);

    @Mapping(target = "category", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) // To ignore null field's values
    void updateAccessory(AccessoryRequest accessoryRequest, @MappingTarget Accessory accessory);
}