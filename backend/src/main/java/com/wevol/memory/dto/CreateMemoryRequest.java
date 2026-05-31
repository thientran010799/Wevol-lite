package com.wevol.memory.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateMemoryRequest(
        @NotBlank String title,
        String location,
        LocalDate memoryDate,
        String mood,
        String note
) {}
