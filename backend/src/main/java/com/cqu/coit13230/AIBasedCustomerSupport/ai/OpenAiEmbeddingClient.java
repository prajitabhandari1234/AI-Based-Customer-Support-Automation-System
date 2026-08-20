package com.cqu.coit13230.AIBasedCustomerSupport.ai;

import com.cqu.coit13230.AIBasedCustomerSupport.common.OpenAiConfigurationException;
import com.cqu.coit13230.AIBasedCustomerSupport.common.OpenAiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiEmbeddingClient implements OpenAiEmbeddingGateway {
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String endpoint;
    private final Duration requestTimeout;
    private final HttpClient httpClient;

    public OpenAiEmbeddingClient(
            ObjectMapper objectMapper,
            @Value("${app.ai.openai.api-key:}") String apiKey,
            @Value("${app.ai.openai.embedding-model:text-embedding-3-small}") String model,
            @Value("${app.ai.openai.embeddings-endpoint:https://api.openai.com/v1/embeddings}") String endpoint,
            @Value("${app.ai.openai.connect-timeout-seconds:8}") long connectTimeoutSeconds,
            @Value("${app.ai.openai.request-timeout-seconds:30}") long requestTimeoutSeconds
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.endpoint = endpoint;
        this.requestTimeout = Duration.ofSeconds(requestTimeoutSeconds);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();
    }

    @Override
    public List<List<Double>> embed(List<String> inputs) {
        requireApiKey();
        if (inputs == null || inputs.isEmpty()) {
            return List.of();
        }

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", model);
            payload.put("input", inputs);
            payload.put("encoding_format", "float");

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(requestTimeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new OpenAiServiceException(
                        "OpenAI embeddings request failed: " + safeError(response.body()),
                        response.statusCode()
                );
            }

            JsonNode data = objectMapper.readTree(response.body()).path("data");
            if (!data.isArray()) {
                throw new OpenAiServiceException("OpenAI embeddings response did not contain a data array");
            }

            List<IndexedVector> indexed = new ArrayList<>();
            for (JsonNode item : data) {
                int index = item.path("index").asInt(-1);
                JsonNode vectorNode = item.path("embedding");
                if (index < 0 || !vectorNode.isArray()) {
                    throw new OpenAiServiceException("OpenAI embeddings response contained an invalid vector");
                }
                List<Double> vector = new ArrayList<>(vectorNode.size());
                for (JsonNode value : vectorNode) {
                    vector.add(value.asDouble());
                }
                indexed.add(new IndexedVector(index, List.copyOf(vector)));
            }

            indexed.sort(Comparator.comparingInt(IndexedVector::index));
            List<List<Double>> result = indexed.stream().map(IndexedVector::vector).toList();
            if (result.size() != inputs.size()) {
                throw new OpenAiServiceException("OpenAI returned a different number of embeddings than requested");
            }
            return result;
        } catch (OpenAiServiceException | OpenAiConfigurationException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new OpenAiServiceException("OpenAI embeddings request was interrupted", ex);
        } catch (Exception ex) {
            throw new OpenAiServiceException("Unable to call the OpenAI embeddings API", ex);
        }
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new OpenAiConfigurationException(
                    "OPENAI_API_KEY is not configured. Add it as an environment variable before using AI features."
            );
        }
    }

    private String safeError(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String message = root.path("error").path("message").asText();
            return message.isBlank() ? "HTTP error from OpenAI" : message;
        } catch (Exception ignored) {
            return "HTTP error from OpenAI";
        }
    }

    private record IndexedVector(int index, List<Double> vector) {
    }
}
