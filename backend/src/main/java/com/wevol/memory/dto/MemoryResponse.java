package com.wevol.memory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MemoryResponse(
        UUID id,
        UUID coupleId,
        UUID createdBy,
        String title,
        String note,
        LocalDate memoryDate,
        String location,
        String mood,
        int mediaCount,
        List<UUID> mediaIds,
        List<String> mediaUrls,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
