package com.example.common;

import jakarta.validation.constraints.NotBlank;

public record Message(@NotBlank String value, @NotBlank String deduplicationId) {}
