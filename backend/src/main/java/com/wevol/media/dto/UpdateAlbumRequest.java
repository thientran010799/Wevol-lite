package com.wevol.media.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpdateAlbumRequest(@NotBlank String title, UUID coverMediaId) {}
