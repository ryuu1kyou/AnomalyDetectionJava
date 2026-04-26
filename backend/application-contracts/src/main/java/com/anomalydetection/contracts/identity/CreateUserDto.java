package com.anomalydetection.contracts.identity;

public record CreateUserDto(
    String userName,
    String email,
    String password,
    boolean isActive) {}
