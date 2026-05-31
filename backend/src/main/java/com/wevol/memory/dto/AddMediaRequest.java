package com.wevol.memory.dto;

import java.util.UUID;

public class AddMediaRequest {
    private UUID mediaId;
    private String url;

    public UUID getMediaId() { return mediaId; }
    public void setMediaId(UUID mediaId) { this.mediaId = mediaId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
