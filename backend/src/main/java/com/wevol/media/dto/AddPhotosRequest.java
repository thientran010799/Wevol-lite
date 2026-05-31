package com.wevol.media.dto;

import java.util.List;
import java.util.UUID;

public record AddPhotosRequest(List<UUID> mediaIds) {}
