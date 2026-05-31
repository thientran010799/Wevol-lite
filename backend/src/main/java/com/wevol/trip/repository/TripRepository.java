package com.wevol.trip.repository;

import com.wevol.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    List<Trip> findByCoupleIdOrderByStartDateDescCreatedAtDesc(UUID coupleId);
    Optional<Trip> findByIdAndCoupleId(UUID id, UUID coupleId);
}
