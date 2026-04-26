package com.anomalydetection.contracts.identity;

public record UpdateUserDto(
    String email,
    boolean isActive) {}
