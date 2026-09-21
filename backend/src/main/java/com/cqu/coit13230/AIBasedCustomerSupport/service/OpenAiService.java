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
 * Provides communication with the configured OpenAI Responses API.
 *
 * <p>
 * This service is responsible for constructing and sending HTTP requests
 * to the configured OpenAI endpoint and extracting generated text from
 * successful API responses.
 * </p>
 *
 * <p>
 * Keeping external AI communication within this service separates HTTP
 * request and response handling from the main ticket and AI analysis
 * workflow.
 * </p>
 */
@Service

public class OpenAiService {

    /**
     * Object mapper used to serialize request bodies and deserialize
     * JSON responses returned by the OpenAI API.
     */
    private final ObjectMapper objectMapper;

    /**
     * API key used to authenticate requests sent to OpenAI.
     */
    private final String apiKey;

    /**
     * OpenAI model configured for response generation.
     */
    private final String model;

    /**
     * Endpoint of the configured OpenAI Responses API.
     */
    private final String endpoint;

    /**
     * Maximum duration allowed for an individual OpenAI API request.
     */
    private final Duration requestTimeout;

    /**
     * HTTP client used to send requests to the configured OpenAI endpoint.
     */
    private final HttpClient httpClient;

    /**
     * Creates an OpenAI service using the configured API settings and
     * HTTP timeout values.
     *
     * <p>
     * The HTTP client is created with the configured connection timeout,
     * while the request timeout is stored separately and applied to each
     * outgoing API request.
     * </p>
     *
     * @param objectMapper          JSON mapper used for request and response
     *                              processing
     * @param apiKey                OpenAI API key used for authentication
     * @param model                 OpenAI model used for response generation
     * @param endpoint              configured OpenAI Responses API endpoint
     * @param connectTimeoutSeconds maximum connection timeout in seconds
     * @param requestTimeoutSeconds maximum request timeout in seconds
     */
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

    /**
     * Determines whether the OpenAI integration has an API key available
     * for sending external requests.
     *
     * @return {@code true} when the API key is not {@code null} and is not
     *         blank; otherwise {@code false}
     */
    public boolean isConfigured() {

        return apiKey != null && !apiKey.isBlank();

    }

    /**
     * Sends the supplied instructions and input to the configured OpenAI
     * Responses API and extracts the generated response text.
     *
     * <p>
     * The request body contains the configured model, storage setting,
     * instructions, and input. Successful HTTP responses are parsed for
     * generated text using the supported response structures.
     * </p>
     *
     * <p>
     * If OpenAI is not configured, the HTTP response is unsuccessful,
     * generated text cannot be found, or an exception occurs, an empty
     * optional is returned so the calling service can use its fallback
     * processing.
     * </p>
     *
     * @param instructions instructions supplied to the OpenAI model
     * @param input        input content supplied to the OpenAI model
     * @return optional containing generated response text when available,
     *         or an empty optional when no usable result is produced
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

            return Optional.empty();

        }

        return Optional.empty();

    }

}