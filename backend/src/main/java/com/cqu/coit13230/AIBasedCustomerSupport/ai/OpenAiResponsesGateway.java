package com.cqu.coit13230.AIBasedCustomerSupport.ai;

public interface OpenAiResponsesGateway {
    OpenAiSupportDecision analyse(OpenAiSupportRequest request);
}
