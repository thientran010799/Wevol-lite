package com.wevol.trip.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateTripStopRequest(
        @Min(value = 1, message = "Day number must be at least 1") int dayNumber,
        String time,
        @NotBlank(message = "Place name is required") String placeName,
        String placeType,
        String address,
        String notes,
        BigDecimal cost,
        String currency
) {}
