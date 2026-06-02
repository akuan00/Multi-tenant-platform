package com.company.ai.platform.vector.impl;

import com.company.ai.platform.vector.VectorStoreService;
import com.company.ai.platform.vector.model.VectorDocument;
import com.company.ai.platform.vector.model.VectorSearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PgVectorStoreService implements VectorStoreService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final Set<String> initializedCollections = ConcurrentHashMap.newKeySet();

    @Override
    public void ensureCollection(String appId, int dimension) {
        String tableName = "vec_" + appId;
        if (initializedCollections.contains(tableName)) {
            return;
        }

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "chunk_id BIGINT NOT NULL, " +
                "content TEXT, " +
                "embedding vector(" + dimension + "), " +
                "metadata JSONB)");

        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_" + tableName + "_embedding " +
                "ON " + tableName + " USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100)");

        initializedCollections.add(tableName);
        log.info("Ensured vector collection exists: {}", tableName);
    }

    @Override
    public void add(String appId, List<VectorDocument> docs) {
        String tableName = "vec_" + appId;
        for (VectorDocument doc : docs) {
            String embeddingStr = arrayToVectorString(doc.getEmbedding());
            try {
                String metadataJson = doc.getMetadata() != null
                        ? objectMapper.writeValueAsString(doc.getMetadata())
                        : null;
                jdbcTemplate.update(
                        "INSERT INTO " + tableName + " (chunk_id, content, embedding, metadata) VALUES (?, ?, ?::vector, ?::jsonb)",
                        doc.getChunkId(), doc.getContent(), embeddingStr, metadataJson);
            } catch (Exception e) {
                throw new RuntimeException("Failed to insert vector document", e);
            }
        }
    }

    @Override
    public List<VectorSearchResult> search(String appId, float[] query, int topK) {
        String tableName = "vec_" + appId;
        String queryVector = arrayToVectorString(query);
        String sql = "SELECT chunk_id, content, 1 - (embedding <=> ?::vector) AS similarity, metadata " +
                "FROM " + tableName + " ORDER BY embedding <=> ?::vector LIMIT ?";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    VectorSearchResult result = new VectorSearchResult();
                    result.setChunkId(rs.getLong("chunk_id"));
                    result.setContent(rs.getString("content"));
                    result.setSimilarity(rs.getDouble("similarity"));
                    String metadataStr = rs.getString("metadata");
                    if (metadataStr != null) {
                        result.setMetadata(objectMapper.readValue(metadataStr, Map.class));
                    }
                    return result;
                },
                queryVector, queryVector, topK);
    }

    private String arrayToVectorString(float[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
