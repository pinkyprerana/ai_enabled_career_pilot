package com.example.learningSpringBootApp.dto;

public record StudentResponse(
    Long id,
    String name,
    String email
) {}
