package com.wevol.media.controller;

import com.wevol.media.dto.ConfirmRequest;
import com.wevol.media.dto.MediaResponse;
import com.wevol.media.dto.PresignRequest;
import com.wevol.media.dto.PresignResponse;
import com.wevol.media.service.R2StorageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final R2StorageService r2StorageService;

    public MediaController(R2StorageService r2StorageService) {
        this.r2StorageService = r2StorageService;
    }

    @PostMapping("/presign")
    public ResponseEntity<PresignResponse> presign(@Valid @RequestBody PresignRequest request) {
        return ResponseEntity.ok(r2StorageService.presign(request.getFilename(), request.getMimeType()));
    }

    @PostMapping("/confirm")
    public ResponseEntity<MediaResponse> confirm(@RequestBody ConfirmRequest request) {
        return ResponseEntity.ok(r2StorageService.confirm(request.getMediaId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        r2StorageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
