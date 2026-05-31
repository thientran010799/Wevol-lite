package com.wevol.memory.repository;

import com.wevol.memory.entity.MemoryMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemoryMediaRepository extends JpaRepository<MemoryMedia, UUID> {
    long countByMemoryId(UUID memoryId);
    List<MemoryMedia> findByMemoryIdOrderBySortOrder(UUID memoryId);
    void deleteByMemoryId(UUID memoryId);
}
