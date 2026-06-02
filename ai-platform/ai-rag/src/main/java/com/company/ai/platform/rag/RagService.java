package com.company.ai.platform.rag;

import com.company.ai.platform.rag.model.RagQuery;
import com.company.ai.platform.rag.model.RagResult;

public interface RagService {
    RagResult query(String appId, RagQuery ragQuery);
}
