package com.wevol.media.dto;

import jakarta.validation.constraints.NotBlank;

public class PresignRequest {

    @NotBlank
    private String filename;

    @NotBlank
    private String mimeType;

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}
