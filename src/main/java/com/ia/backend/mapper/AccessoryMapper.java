package com.ia.backend.mapper;

import com.ia.backend.dto.accessory.AccessoryRequest;
import com.ia.backend.dto.accessory.AccessoryResponse;
import com.ia.backend.entity.Accessory;
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