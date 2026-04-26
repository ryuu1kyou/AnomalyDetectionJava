package com.anomalydetection.contracts.identity;

public record GetUsersInputDto(
    String filter,
    Integer skipCount,
    Integer maxResultCount) {}
