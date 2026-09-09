package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Sends requests to the configured OpenAI Responses API.
 * This class keeps the HTTP request and response handling separate from the ticket workflow.
 */
@Service
public class OpenAiService {

    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String endpoint;
    private final Duration requestTimeout;
    private final HttpClient httpClient;

    public OpenAiService(
            ObjectMapper objectMapper,
            @Value("${app.ai.openai.api-key}") String apiKey,
            @Value("${app.ai.openai.model}") String model,
            @Value("${app.ai.openai.endpoint}") String endpoint,
            @Value("${app.ai.openai.connect-timeout-seconds}") long connectTimeoutSeconds,
            @Value("${app.ai.openai.request-timeout-seconds}") long requestTimeoutSeconds) {

        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.endpoint = endpoint;
        this.requestTimeout = Duration.ofSeconds(requestTimeoutSeconds);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();
    }

    // OpenAI calls are enabled only when the provider is selected and an API key has been supplied.
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /*
     * Sends the prepared prompt to the configured Responses API endpoint.
     * Only the generated text is returned so AiService does not need to handle HTTP response details.
     */
    public Optional<String> request(String instructions, String input) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("store", false);
            body.put("instructions", instructions);
            body.put("input", input);

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(requestTimeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }

            JsonNode json = objectMapper.readTree(response.body());

            if (json.hasNonNull("output_text")) {
                return Optional.of(json.get("output_text").asText());
            }

            JsonNode output = json.path("output");
            if (output.isArray()) {
                for (JsonNode item : output) {
                    JsonNode content = item.path("content");
                    if (!content.isArray()) {
                        continue;
                    }

                    for (JsonNode part : content) {
                        if (part.hasNonNull("text")) {
                            return Optional.of(part.get("text").asText());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // Return an empty result so AiService can safely fall back to the local analysis rules.
            return Optional.empty();
        }

        return Optional.empty();
    }
}
