package com.wevol.memory.service;

import com.wevol.config.AppConstants;
import com.wevol.exception.AppException;
import com.wevol.memory.dto.AddMediaRequest;
import com.wevol.memory.dto.CreateMemoryRequest;
import com.wevol.memory.dto.MemoryResponse;
import com.wevol.memory.dto.UpdateMemoryRequest;
import com.wevol.memory.entity.Memory;
import com.wevol.memory.entity.MemoryMedia;
import com.wevol.memory.repository.MemoryMediaRepository;
import com.wevol.memory.repository.MemoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MemoryService {

    private final MemoryRepository memoryRepository;
    private final MemoryMediaRepository mediaRepository;

    public MemoryService(MemoryRepository memoryRepository, MemoryMediaRepository mediaRepository) {
        this.memoryRepository = memoryRepository;
        this.mediaRepository = mediaRepository;
    }

    @Transactional(readOnly = true)
    public List<MemoryResponse> findAll() {
        return memoryRepository.findByCoupleIdOrderByMemoryDateDescCreatedAtDesc(AppConstants.COUPLE_ID)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MemoryResponse findById(UUID id) {
        Memory memory = memoryRepository.findByIdAndCoupleId(id, AppConstants.COUPLE_ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Memory not found"));
        return toResponse(memory);
    }

    public MemoryResponse create(CreateMemoryRequest request) {
        Memory memory = new Memory();
        memory.setCoupleId(AppConstants.COUPLE_ID);
        memory.setCreatedBy(AppConstants.USER_ID);
        memory.setTitle(request.title().trim());
        memory.setLocation(request.location());
        memory.setMemoryDate(request.memoryDate());
        memory.setMood(request.mood() != null ? request.mood() : "happy");
        memory.setNote(request.note());
        memoryRepository.save(memory);
        return toResponse(memory);
    }

    public MemoryResponse update(UUID id, UpdateMemoryRequest request) {
        Memory memory = memoryRepository.findByIdAndCoupleId(id, AppConstants.COUPLE_ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Memory not found"));
        memory.setTitle(request.title().trim());
        memory.setLocation(request.location());
        memory.setMemoryDate(request.memoryDate());
        if (request.mood() != null) memory.setMood(request.mood());
        memory.setNote(request.note());
        memoryRepository.save(memory);
        return toResponse(memory);
    }

    public void delete(UUID id) {
        Memory memory = memoryRepository.findByIdAndCoupleId(id, AppConstants.COUPLE_ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Memory not found"));
        mediaRepository.deleteByMemoryId(memory.getId());
        memoryRepository.delete(memory);
    }

    public void addMedia(UUID memoryId, AddMediaRequest request) {
        Memory memory = memoryRepository.findByIdAndCoupleId(memoryId, AppConstants.COUPLE_ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Memory not found"));
        int nextOrder = (int) mediaRepository.countByMemoryId(memory.getId());
        MemoryMedia mm = new MemoryMedia();
        mm.setMemoryId(memory.getId());
        mm.setMediaId(request.getMediaId());
        mm.setUrl(request.getUrl());
        mm.setSortOrder(nextOrder);
        mediaRepository.save(mm);
    }

    private MemoryResponse toResponse(Memory memory) {
        List<MemoryMedia> mediaList = mediaRepository.findByMemoryIdOrderBySortOrder(memory.getId());
        List<UUID> mediaIds = mediaList.stream().map(MemoryMedia::getMediaId).toList();
        List<String> mediaUrls = mediaList.stream()
                .map(MemoryMedia::getUrl)
                .filter(url -> url != null && !url.isBlank())
                .toList();
        return new MemoryResponse(
                memory.getId(), memory.getCoupleId(), memory.getCreatedBy(),
                memory.getTitle(), memory.getNote(), memory.getMemoryDate(),
                memory.getLocation(), memory.getMood(), mediaList.size(),
                mediaIds, mediaUrls, memory.getCreatedAt(), memory.getUpdatedAt());
    }
}
