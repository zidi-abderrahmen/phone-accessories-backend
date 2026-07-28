package com.ia.backend.mapper;

import com.ia.backend.dto.accessory.AccessoryRequest;
import com.ia.backend.dto.accessory.AccessoryResponse;
import com.ia.backend.entity.Accessory;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AccessoryMapper {

    AccessoryResponse toDto(Accessory accessory);

    Accessory toEntity(AccessoryRequest accessoryRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) // To ignore null field's values
    void updateAccessory(AccessoryRequest accessoryRequest, @MappingTarget Accessory accessory);
}