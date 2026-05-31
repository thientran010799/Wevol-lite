package com.wevol.media.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "couple_id", nullable = false)
    private UUID coupleId;

    @Column(nullable = false)
    private String title;

    @Column(name = "cover_media_id")
    private UUID coverMediaId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getCoupleId() { return coupleId; }
    public void setCoupleId(UUID coupleId) { this.coupleId = coupleId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public UUID getCoverMediaId() { return coverMediaId; }
    public void setCoverMediaId(UUID coverMediaId) { this.coverMediaId = coverMediaId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
