package com.wevol.search.dto;

import java.util.List;

public record SearchResponse(int count, List<SearchResultDto> results) {}
