package com.wevol.media.service;

import com.wevol.config.AppConstants;
import com.wevol.exception.AppException;
import com.wevol.media.dto.ConfirmRequest;
import com.wevol.media.dto.MediaResponse;
import com.wevol.media.dto.PresignResponse;
import com.wevol.media.entity.Media;
import com.wevol.media.repository.MediaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@Transactional
public class R2StorageService {

    private static final Logger log = LoggerFactory.getLogger(R2StorageService.class);

    private final MediaRepository mediaRepository;
    private final S3Client r2Client;
    private final S3Presigner r2Presigner;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.public-url}")
    private String r2PublicUrl;

    public R2StorageService(MediaRepository mediaRepository, S3Client r2Client, S3Presigner r2Presigner) {
        this.mediaRepository = mediaRepository;
        this.r2Client = r2Client;
        this.r2Presigner = r2Presigner;
    }

    public PresignResponse presign(String filename, String mimeType) {
        String r2Key = AppConstants.COUPLE_ID + "/" + UUID.randomUUID() + "/" + filename;

        Media media = new Media();
        media.setCoupleId(AppConstants.COUPLE_ID);
        media.setUploadedBy(AppConstants.USER_ID);
        media.setR2Key(r2Key);
        media.setUrl("");
        media.setMimeType(mimeType);
        mediaRepository.save(media);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket).key(r2Key).contentType(mimeType).build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presigned = r2Presigner.presignPutObject(presignRequest);
        log.info("[PRESIGN] mediaId={} key={}", media.getId(), r2Key);

        return new PresignResponse(media.getId(), presigned.url().toString(), r2Key);
    }

    public MediaResponse confirm(UUID mediaId, ConfirmRequest request) {
        Media media = mediaRepository.findByIdAndCoupleId(mediaId, AppConstants.COUPLE_ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Media not found"));

        media.setUrl(r2PublicUrl + "/" + media.getR2Key());
        if (request.getMimeType() != null) media.setMimeType(request.getMimeType());
        if (request.getSizeBytes() != null) media.setSizeBytes(request.getSizeBytes());
        mediaRepository.save(media);

        return toResponse(media);
    }

    public void delete(UUID mediaId) {
        Media media = mediaRepository.findByIdAndCoupleId(mediaId, AppConstants.COUPLE_ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Media not found"));
        try {
            r2Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(media.getR2Key()).build());
        } catch (Exception e) {
            log.warn("Failed to delete R2 object {}: {}", media.getR2Key(), e.getMessage());
        }
        mediaRepository.delete(media);
    }

    private MediaResponse toResponse(Media media) {
        return new MediaResponse(media.getId(), media.getCoupleId(), media.getUploadedBy(),
                media.getUrl(), media.getThumbUrl(), media.getMimeType(),
                media.getSizeBytes(), media.getUploadedAt());
    }
}
