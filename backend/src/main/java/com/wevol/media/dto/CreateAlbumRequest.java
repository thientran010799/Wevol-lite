package com.wevol.media.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAlbumRequest(@NotBlank String title) {}
