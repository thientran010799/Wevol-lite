package com.wevol.trip.repository;

import com.wevol.trip.entity.TripStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface TripStopRepository extends JpaRepository<TripStop, UUID> {

    Optional<TripStop> findByIdAndTripId(UUID id, UUID tripId);

    @Modifying
    @Query("DELETE FROM TripStop s WHERE s.trip.id = :tripId AND s.dayNumber = :dayNumber")
    void deleteByTripIdAndDayNumber(UUID tripId, int dayNumber);

    @Query("SELECT COALESCE(MAX(s.position), -1) FROM TripStop s WHERE s.trip.id = :tripId AND s.dayNumber = :dayNumber")
    int findMaxPositionByTripIdAndDayNumber(UUID tripId, int dayNumber);
}
