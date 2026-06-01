package com.wevol.couple.dto;

import java.time.LocalDate;

public record UpdateCoupleRequest(String name, LocalDate startDate, LocalDate anniversary) {}
