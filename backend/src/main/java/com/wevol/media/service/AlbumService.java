package com.wevol.media.service;

import com.wevol.config.AppConstants;
import com.wevol.exception.AppException;
import com.wevol.media.dto.AlbumResponse;
import com.wevol.media.dto.CreateAlbumRequest;
import com.wevol.media.dto.MediaResponse;
import com.wevol.media.dto.UpdateAlbumRequest;
import com.wevol.media.entity.Album;
import com.wevol.media.entity.AlbumPhoto;
import com.wevol.media.entity.AlbumPhoto.AlbumPhotoId;
import com.wevol.media.entity.Media;
import com.wevol.media.repository.AlbumPhotoRepository;
import com.wevol.media.repository.AlbumRepository;
import com.wevol.media.repository.MediaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumPhotoRepository albumPhotoRepository;
    private final MediaRepository mediaRepository;

    public AlbumService(AlbumRepository albumRepository,
                        AlbumPhotoRepository albumPhotoRepository,
                        MediaRepository mediaRepository) {
        this.albumRepository = albumRepository;
        this.albumPhotoRepository = albumPhotoRepository;
        this.mediaRepository = mediaRepository;
    }

    @Transactional(readOnly = true)
    public List<AlbumResponse> findAll() {
        return albumRepository.findByCoupleIdOrderByCreatedAtDesc(AppConstants.COUPLE_ID)
                .stream().map(this::toAlbumResponse).toList();
    }

    @Transactional(readOnly = true)
    public AlbumResponse findById(UUID id) {
        Album album = albumRepository.findByIdAndCoupleId(id, AppConstants.COUPLE_ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Album not found"));
        return toAlbumResponse(album);
    }

    public AlbumResponse create(CreateAlbumRequest request) {
        Album album = new Album();
        album.setCoupleId(AppConstants.COUPLE_ID);
        album.setTitle(request.title().trim());
        albumRepository.save(album);
        return toAlbumResponse(album);
    }

    public AlbumResponse update(UUID id, UpdateAlbumRequest request) {
        Album album = albumRepository.findByIdAndCoupleId(id, AppConstants.COUPLE_ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Album not found"));
        album.setTitle(request.title().trim());
        album.setCoverMediaId(request.coverMediaId());
        albumRepository.save(album);
        return toAlbumResponse(album);
    }

    public void delete(UUID id) {
        Album album = albumRepository.findByIdAndCoupleId(id, AppConstants.COUPLE_ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Album not found"));
        albumRepository.delete(album);
    }

    public AlbumResponse addPhotos(UUID albumId, List<UUID> mediaIds) {
        Album album = albumRepository.findByIdAndCoupleId(albumId, AppConstants.COUPLE_ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Album not found"));
        for (UUID mediaId : mediaIds) {
            AlbumPhotoId pk = new AlbumPhotoId(albumId, mediaId);
            if (!albumPhotoRepository.existsById(pk)) {
                AlbumPhoto albumPhoto = new AlbumPhoto();
                albumPhoto.setAlbumPhotoId(pk);
                albumPhotoRepository.save(albumPhoto);
            }
        }
        return toAlbumResponse(album);
    }

    public void removePhoto(UUID albumId, UUID mediaId) {
        albumRepository.findByIdAndCoupleId(albumId, AppConstants.COUPLE_ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Album not found"));
        albumPhotoRepository.deleteByAlbumPhotoIdAlbumIdAndAlbumPhotoIdMediaId(albumId, mediaId);
    }

    private AlbumResponse toAlbumResponse(Album album) {
        List<AlbumPhoto> albumPhotos = albumPhotoRepository.findByAlbumPhotoIdAlbumId(album.getId());
        List<MediaResponse> photos = albumPhotos.stream()
                .map(ap -> mediaRepository.findById(ap.getAlbumPhotoId().getMediaId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(this::toMediaResponse)
                .toList();
        return new AlbumResponse(album.getId(), album.getCoupleId(), album.getTitle(),
                album.getCoverMediaId(), photos, album.getCreatedAt(), album.getUpdatedAt());
    }

    private MediaResponse toMediaResponse(Media media) {
        return new MediaResponse(media.getId(), media.getCoupleId(), media.getUploadedBy(),
                media.getUrl(), media.getThumbUrl(), media.getMimeType(),
                media.getSizeBytes(), media.getUploadedAt());
    }
}
