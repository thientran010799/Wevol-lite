package com.wevol.trip.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record UpdateTripStopRequest(
        String time,
        @NotBlank(message = "Place name is required") String placeName,
        String placeType,
        String address,
        String notes,
        BigDecimal cost,
        String currency
) {}
