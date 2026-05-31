package com.wevol.media.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "album_photos")
public class AlbumPhoto {

    @Embeddable
    public static class AlbumPhotoId implements Serializable {

        @Column(name = "album_id")
        private UUID albumId;

        @Column(name = "media_id")
        private UUID mediaId;

        public AlbumPhotoId() {}

        public AlbumPhotoId(UUID albumId, UUID mediaId) {
            this.albumId = albumId;
            this.mediaId = mediaId;
        }

        public UUID getAlbumId() { return albumId; }
        public void setAlbumId(UUID albumId) { this.albumId = albumId; }
        public UUID getMediaId() { return mediaId; }
        public void setMediaId(UUID mediaId) { this.mediaId = mediaId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AlbumPhotoId that)) return false;
            return Objects.equals(albumId, that.albumId) && Objects.equals(mediaId, that.mediaId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(albumId, mediaId);
        }
    }

    @EmbeddedId
    private AlbumPhotoId albumPhotoId;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    void prePersist() {
        addedAt = LocalDateTime.now();
    }

    public AlbumPhotoId getAlbumPhotoId() { return albumPhotoId; }
    public void setAlbumPhotoId(AlbumPhotoId albumPhotoId) { this.albumPhotoId = albumPhotoId; }
    public LocalDateTime getAddedAt() { return addedAt; }
}
