package com.wevol.search.repository;

import com.wevol.search.dto.SearchResultDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SearchRepository {

    private final JdbcTemplate jdbc;

    public SearchRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SearchResultDto> searchMemories(UUID coupleId, String query) {
        String sql = """
            SELECT
                'memory'::text                                               AS type,
                m.id,
                m.title,
                ts_headline('english', COALESCE(m.note, m.title),
                    plainto_tsquery('english', ?),
                    'MaxWords=15, MinWords=5, MaxFragments=1')               AS snippet,
                m.memory_date                                                AS date,
                mm.url                                                       AS thumbnail_url,
                ts_rank(m.search_vector, plainto_tsquery('english', ?))      AS rank
            FROM memories m
            LEFT JOIN LATERAL (
                SELECT url FROM memory_media
                WHERE memory_id = m.id
                ORDER BY sort_order ASC
                LIMIT 1
            ) mm ON true
            WHERE m.couple_id = ?::uuid
              AND m.search_vector @@ plainto_tsquery('english', ?)
            ORDER BY rank DESC
            LIMIT 20
            """;
        return jdbc.query(sql, (rs, row) -> {
            SearchResultDto dto = new SearchResultDto();
            dto.setType(rs.getString("type"));
            dto.setId(UUID.fromString(rs.getString("id")));
            dto.setTitle(rs.getString("title"));
            dto.setSnippet(rs.getString("snippet"));
            java.sql.Date d = rs.getDate("date");
            dto.setDate(d != null ? d.toLocalDate() : null);
            dto.setThumbnailUrl(rs.getString("thumbnail_url"));
            return dto;
        }, query, query, coupleId.toString(), query);
    }

    public List<SearchResultDto> searchTrips(UUID coupleId, String query) {
        String sql = """
            SELECT
                'trip'::text                                                 AS type,
                t.id,
                t.name                                                       AS title,
                ts_headline('english', COALESCE(t.notes, t.name),
                    plainto_tsquery('english', ?),
                    'MaxWords=15, MinWords=5, MaxFragments=1')               AS snippet,
                t.start_date                                                 AS date,
                NULL::text                                                   AS thumbnail_url,
                ts_rank(t.search_vector, plainto_tsquery('english', ?))      AS rank
            FROM trips t
            WHERE t.couple_id = ?::uuid
              AND t.search_vector @@ plainto_tsquery('english', ?)
            ORDER BY rank DESC
            LIMIT 20
            """;
        return jdbc.query(sql, (rs, row) -> {
            SearchResultDto dto = new SearchResultDto();
            dto.setType(rs.getString("type"));
            dto.setId(UUID.fromString(rs.getString("id")));
            dto.setTitle(rs.getString("title"));
            dto.setSnippet(rs.getString("snippet"));
            java.sql.Date d = rs.getDate("date");
            dto.setDate(d != null ? d.toLocalDate() : null);
            dto.setThumbnailUrl(rs.getString("thumbnail_url"));
            return dto;
        }, query, query, coupleId.toString(), query);
    }
}
