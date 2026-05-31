package com.wevol.search.service;

import com.wevol.config.AppConstants;
import com.wevol.exception.AppException;
import com.wevol.search.dto.SearchResponse;
import com.wevol.search.dto.SearchResultDto;
import com.wevol.search.repository.SearchRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class SearchService {

    private final SearchRepository searchRepository;

    public SearchService(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public SearchResponse search(String query, String type) {
        if (query == null || query.trim().length() < 2) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Query must be at least 2 characters");
        }
        String q = query.trim();
        List<SearchResultDto> results = switch (type.toLowerCase()) {
            case "memories" -> searchRepository.searchMemories(AppConstants.COUPLE_ID, q);
            case "trips"    -> searchRepository.searchTrips(AppConstants.COUPLE_ID, q);
            default         -> Stream.concat(
                    searchRepository.searchMemories(AppConstants.COUPLE_ID, q).stream(),
                    searchRepository.searchTrips(AppConstants.COUPLE_ID, q).stream()
            ).sorted((a, b) -> b.getDate() != null && a.getDate() != null
                    ? b.getDate().compareTo(a.getDate()) : 0)
             .limit(20).toList();
        };
        return new SearchResponse(results.size(), results);
    }
}
