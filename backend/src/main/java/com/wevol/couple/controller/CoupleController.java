package com.wevol.couple.controller;

import com.wevol.couple.dto.CoupleResponse;
import com.wevol.couple.dto.UpdateCoupleRequest;
import com.wevol.couple.service.CoupleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/couple")
public class CoupleController {

    private final CoupleService service;

    public CoupleController(CoupleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<CoupleResponse> get() {
        return ResponseEntity.ok(service.get());
    }

    @PutMapping
    public ResponseEntity<CoupleResponse> update(@RequestBody UpdateCoupleRequest req) {
        return ResponseEntity.ok(service.update(req));
    }
}
