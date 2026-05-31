package com.wevol.media.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MediaResponse(
        UUID id,
        UUID coupleId,
        UUID uploadedBy,
        String url,
        String thumbUrl,
        String mimeType,
        Long sizeBytes,
        LocalDateTime uploadedAt
) {}
