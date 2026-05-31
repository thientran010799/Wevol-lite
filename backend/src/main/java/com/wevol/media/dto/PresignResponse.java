package com.wevol.media.dto;

import java.util.UUID;

public record PresignResponse(UUID mediaId, String uploadUrl, String r2Key) {}
