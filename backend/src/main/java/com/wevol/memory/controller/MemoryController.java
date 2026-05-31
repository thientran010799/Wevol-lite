package com.wevol.memory.controller;

import com.wevol.memory.dto.AddMediaRequest;
import com.wevol.memory.dto.CreateMemoryRequest;
import com.wevol.memory.dto.MemoryResponse;
import com.wevol.memory.dto.UpdateMemoryRequest;
import com.wevol.memory.service.MemoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/memories")
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @GetMapping
    public ResponseEntity<List<MemoryResponse>> list() {
        return ResponseEntity.ok(memoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemoryResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(memoryService.findById(id));
    }

    @PostMapping
    public ResponseEntity<MemoryResponse> create(@Valid @RequestBody CreateMemoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memoryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemoryResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateMemoryRequest request) {
        return ResponseEntity.ok(memoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        memoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/media")
    public ResponseEntity<Void> addMedia(@PathVariable UUID id, @RequestBody AddMediaRequest request) {
        memoryService.addMedia(id, request);
        return ResponseEntity.noContent().build();
    }
}
