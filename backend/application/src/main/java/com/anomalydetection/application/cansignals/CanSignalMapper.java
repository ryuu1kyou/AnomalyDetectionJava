package com.anomalydetection.application.cansignals;

import com.anomalydetection.contracts.cansignals.CanSignalDto;
import com.anomalydetection.domain.cansignals.CanSignal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CanSignalMapper {

  @Mapping(source = "signed", target = "isSigned")
  CanSignalDto toDto(CanSignal entity);
}
