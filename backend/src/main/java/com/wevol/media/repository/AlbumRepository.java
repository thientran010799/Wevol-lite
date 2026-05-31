package com.wevol.media.repository;

import com.wevol.media.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlbumRepository extends JpaRepository<Album, UUID> {
    List<Album> findByCoupleIdOrderByCreatedAtDesc(UUID coupleId);
    Optional<Album> findByIdAndCoupleId(UUID id, UUID coupleId);
}
