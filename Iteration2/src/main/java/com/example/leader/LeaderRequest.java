package com.example.leader;

import com.example.common.Message;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LeaderRequest(
    @NotNull Message message, @Min(1) int writeConcern) {}
