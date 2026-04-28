package com.anomalydetection.application.anomalydetection;

import com.anomalydetection.contracts.anomalydetection.CanAnomalyDetectionLogicDto;
import com.anomalydetection.domain.anomalydetection.CanAnomalyDetectionLogic;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CanAnomalyDetectionLogicMapper {

  CanAnomalyDetectionLogicDto toDto(CanAnomalyDetectionLogic entity);
}
