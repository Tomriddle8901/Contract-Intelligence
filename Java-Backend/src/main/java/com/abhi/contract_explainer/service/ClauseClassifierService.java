package com.abhi.contract_explainer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ClauseClassifierService {

    // URL of your FastAPI classifier
    private static final String CLASSIFIER_URL = "http://127.0.0.1:8001/classify";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ClauseClassifierService(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    /**
     * Sends a clause to the Python API and returns the predicted label.
     */
    public String classifyClause(String clauseText) {
        try {
            // JSON body: { "text": "clause..." }
            ObjectNode body = objectMapper.createObjectNode();
            body.put("text", clauseText);

            String requestBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CLASSIFIER_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException("Classifier API error: HTTP "
                        + response.statusCode() + " - " + response.body());
            }

            // Response: { "label": "...", "scores": { ... } }
            JsonNode json = objectMapper.readTree(response.body());
            JsonNode labelNode = json.get("label");
            if (labelNode == null) {
                throw new RuntimeException("Classifier API response has no 'label': " + response.body());
            }

            return labelNode.asText();

        } catch (Exception e) {
            throw new RuntimeException("Error calling Clause Classifier API", e);
        }
    }
}