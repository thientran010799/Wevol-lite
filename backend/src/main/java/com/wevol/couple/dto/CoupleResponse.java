package com.wevol.couple.dto;

import java.time.LocalDate;

public record CoupleResponse(String name, LocalDate startDate, LocalDate anniversary) {}
