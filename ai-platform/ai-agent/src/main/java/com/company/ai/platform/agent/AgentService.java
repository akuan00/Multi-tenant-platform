package com.company.ai.platform.agent;

import com.company.ai.platform.agent.model.AgentRequest;
import com.company.ai.platform.agent.model.AgentResponse;

public interface AgentService {
    AgentResponse execute(AgentRequest request);
}
