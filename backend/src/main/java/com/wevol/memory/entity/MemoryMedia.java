package com.wevol.memory.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "memory_media")
public class MemoryMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "memory_id", nullable = false)
    private UUID memoryId;

    @Column(name = "media_id", nullable = false)
    private UUID mediaId;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "url")
    private String url;

    public UUID getId() { return id; }
    public UUID getMemoryId() { return memoryId; }
    public void setMemoryId(UUID memoryId) { this.memoryId = memoryId; }
    public UUID getMediaId() { return mediaId; }
    public void setMediaId(UUID mediaId) { this.mediaId = mediaId; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
