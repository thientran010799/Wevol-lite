package com.wevol.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TripResponse(
        UUID id,
        UUID coupleId,
        UUID createdBy,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String notes,
        BigDecimal budget,
        String currency,
        List<TripStopResponse> stops,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
