package com.company.ai.platform.vector;

import com.company.ai.platform.vector.model.VectorDocument;
import com.company.ai.platform.vector.model.VectorSearchResult;
import java.util.List;

public interface VectorStoreService {
    void add(String appId, List<VectorDocument> docs);
    List<VectorSearchResult> search(String appId, float[] query, int topK);
    void ensureCollection(String appId, int dimension);
}
