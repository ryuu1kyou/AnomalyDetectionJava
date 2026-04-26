package com.anomalydetection.contracts.multitenancy;

public record GetTenantsInputDto(
    String filter,
    Integer skipCount,
    Integer maxResultCount) {}
