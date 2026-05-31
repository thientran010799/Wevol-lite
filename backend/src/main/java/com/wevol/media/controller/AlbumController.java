package com.wevol.media.controller;

import com.wevol.media.dto.AddPhotosRequest;
import com.wevol.media.dto.AlbumResponse;
import com.wevol.media.dto.CreateAlbumRequest;
import com.wevol.media.dto.UpdateAlbumRequest;
import com.wevol.media.service.AlbumService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public ResponseEntity<List<AlbumResponse>> list() {
        return ResponseEntity.ok(albumService.findAll());
    }

    @PostMapping
    public ResponseEntity<AlbumResponse> create(@Valid @RequestBody CreateAlbumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(albumService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(albumService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlbumResponse> update(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdateAlbumRequest request) {
        return ResponseEntity.ok(albumService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        albumService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/photos")
    public ResponseEntity<AlbumResponse> addPhotos(@PathVariable UUID id,
                                                    @RequestBody AddPhotosRequest request) {
        return ResponseEntity.ok(albumService.addPhotos(id, request.mediaIds()));
    }

    @DeleteMapping("/{id}/photos/{mediaId}")
    public ResponseEntity<Void> removePhoto(@PathVariable UUID id, @PathVariable UUID mediaId) {
        albumService.removePhoto(id, mediaId);
        return ResponseEntity.noContent().build();
    }
}
