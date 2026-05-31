package com.wevol.media.repository;

import com.wevol.media.entity.AlbumPhoto;
import com.wevol.media.entity.AlbumPhoto.AlbumPhotoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlbumPhotoRepository extends JpaRepository<AlbumPhoto, AlbumPhotoId> {
    List<AlbumPhoto> findByAlbumPhotoIdAlbumId(UUID albumId);
    void deleteByAlbumPhotoIdAlbumIdAndAlbumPhotoIdMediaId(UUID albumId, UUID mediaId);
}
