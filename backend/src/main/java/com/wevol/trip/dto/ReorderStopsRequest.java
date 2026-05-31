package com.wevol.trip.dto;

import java.util.List;
import java.util.UUID;

public record ReorderStopsRequest(List<UUID> stopIds) {}
