package com.wevol.media.repository;

import com.wevol.media.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {
    List<Media> findByCoupleIdOrderByUploadedAtDesc(UUID coupleId);
    Optional<Media> findByIdAndCoupleId(UUID id, UUID coupleId);
}
