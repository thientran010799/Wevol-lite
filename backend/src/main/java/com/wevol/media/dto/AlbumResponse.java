package com.wevol.media.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AlbumResponse(
        UUID id,
        UUID coupleId,
        String title,
        UUID coverMediaId,
        List<MediaResponse> photos,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
