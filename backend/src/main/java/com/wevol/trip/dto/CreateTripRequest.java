package com.wevol.trip.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTripRequest(
        @NotBlank(message = "Trip name is required") String name,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        BigDecimal budget,
        String currency,
        String notes
) {}
