package com.wevol.trip.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TripStopResponse(
        UUID id,
        UUID tripId,
        int dayNumber,
        String time,
        String placeName,
        String placeType,
        String address,
        String notes,
        BigDecimal cost,
        String currency,
        int position,
        LocalDateTime createdAt
) {}
