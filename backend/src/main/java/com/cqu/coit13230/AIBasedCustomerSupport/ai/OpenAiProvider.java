package com.cqu.coit13230.AIBasedCustomerSupport.ai;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketCategory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Component
public class OpenAiProvider {
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String endpoint;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public OpenAiProvider(ObjectMapper objectMapper,
                          @Value("${app.ai.openai.api-key}") String apiKey,
                          @Value("${app.ai.openai.model}") String model,
                          @Value("${app.ai.openai.endpoint}") String endpoint) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.endpoint = endpoint;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Optional<String> generate(String customerMessage, TicketCategory category,
                                     Sentiment sentiment, boolean escalated) {
        if (!isConfigured()) return Optional.empty();
        try {
            String instructions = """
                    You are a professional customer-support assistant.
                    Reply clearly and politely in no more than 120 words.
                    Do not invent order, payment, account, or policy details.
                    Ask for safe identifying information when needed, but never request passwords or full card numbers.
                    Category: %s. Sentiment: %s. Escalated to human: %s.
                    If escalated, tell the customer a human agent will review the ticket.
                    """.formatted(category, sentiment, escalated);
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "store", false,
                    "instructions", instructions,
                    "input", customerMessage
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(18))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) return Optional.empty();
            JsonNode json = objectMapper.readTree(response.body());
            if (json.hasNonNull("output_text")) return Optional.of(json.get("output_text").asText());
            JsonNode output = json.path("output");
            if (output.isArray()) {
                for (JsonNode item : output) {
                    for (JsonNode content : item.path("content")) {
                        if (content.hasNonNull("text")) return Optional.of(content.get("text").asText());
                    }
                }
            }
        } catch (Exception ignored) {
            // The caller will use the local fallback provider.
        }
        return Optional.empty();
    }
}
