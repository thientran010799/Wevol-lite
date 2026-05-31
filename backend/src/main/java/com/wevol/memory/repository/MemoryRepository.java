package com.wevol.memory.repository;

import com.wevol.memory.entity.Memory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemoryRepository extends JpaRepository<Memory, UUID> {
    List<Memory> findByCoupleIdOrderByMemoryDateDescCreatedAtDesc(UUID coupleId);
    Optional<Memory> findByIdAndCoupleId(UUID id, UUID coupleId);
}
