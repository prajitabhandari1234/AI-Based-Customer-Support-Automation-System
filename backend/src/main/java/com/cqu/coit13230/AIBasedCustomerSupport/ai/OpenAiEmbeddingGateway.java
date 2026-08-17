package com.cqu.coit13230.AIBasedCustomerSupport.ai;

import java.util.List;

public interface OpenAiEmbeddingGateway {
    List<List<Double>> embed(List<String> inputs);
}
