package com.wevol.trip.controller;

import com.wevol.trip.dto.*;
import com.wevol.trip.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> list() {
        return ResponseEntity.ok(tripService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(tripService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TripResponse> create(@Valid @RequestBody CreateTripRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> update(@PathVariable UUID id,
                                                @Valid @RequestBody UpdateTripRequest request) {
        return ResponseEntity.ok(tripService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tripService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tripId}/stops")
    public ResponseEntity<TripStopResponse> addStop(@PathVariable UUID tripId,
                                                     @Valid @RequestBody CreateTripStopRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.addStop(tripId, request));
    }

    @PutMapping("/{tripId}/stops/{stopId}")
    public ResponseEntity<TripStopResponse> updateStop(@PathVariable UUID tripId,
                                                        @PathVariable UUID stopId,
                                                        @Valid @RequestBody UpdateTripStopRequest request) {
        return ResponseEntity.ok(tripService.updateStop(tripId, stopId, request));
    }

    @DeleteMapping("/{tripId}/stops/{stopId}")
    public ResponseEntity<Void> deleteStop(@PathVariable UUID tripId, @PathVariable UUID stopId) {
        tripService.deleteStop(tripId, stopId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tripId}/days/{dayNumber}")
    public ResponseEntity<Void> deleteDay(@PathVariable UUID tripId, @PathVariable int dayNumber) {
        tripService.deleteDay(tripId, dayNumber);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{tripId}/stops/reorder")
    public ResponseEntity<Void> reorderStops(@PathVariable UUID tripId,
                                              @RequestBody ReorderStopsRequest request) {
        tripService.reorderStops(tripId, request);
        return ResponseEntity.noContent().build();
    }
}
