package com.abhi.contract_explainer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class LlmService {

    // Ollama local chat API
    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";

    // Name of the model you pulled with `ollama pull llama3`
    private static final String MODEL = "llama3";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LlmService(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    // Called by /upload to summarize the contract
    public String summarizeContract(String contractText) {
        String prompt = """
                You are a helpful assistant that explains contracts in simple language.
                Summarize the following contract for a non-lawyer in 5–8 bullet points.
                Focus on: obligations, payments, duration, termination, and any penalties.
                Always add: "This is not legal advice." at the end.

                Contract text:
                """ + contractText;

        return callLlm(prompt);
    }

    // Called by /{id}/ask to answer a question about the contract
    public String answerQuestion(String contextText, String question) {
        String prompt = """
            You are a careful assistant that explains contract clauses in simple language.

            TASK:
            1) Read ONLY the text below under "Contract excerpt".
            2) Find the sentence(s) that answer the user's question.
            3) First, copy those sentence(s) EXACTLY as they appear under the heading:
               "Relevant contract text:".
            4) Then, under the heading "Explanation:", explain the meaning in simple language.
               - If there are different conditions (for example:
                 * during probation vs after probation,
                 * employer vs employee,
                 * different notice periods in different situations),
                 describe EACH condition separately and clearly.
            5) If the answer is NOT clearly stated in the text, say:
               "I'm not sure. The contract text does not clearly specify this."
            6) Do NOT invent rules or numbers that are not clearly written in the text.
            7) Always end your answer with: "This is not legal advice."

            Contract excerpt:
            """ + contextText + """

            User question: """ + question;

        return callLlm(prompt);
    }

    // Core method: sends prompt to Ollama and returns the model's reply text
    private String callLlm(String prompt) {
        try {
            // 1) Build JSON body for Ollama
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", MODEL);
            root.put("stream", false);

            ArrayNode messages = objectMapper.createArrayNode();

            // System message: define the assistant's role
            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are a contract and policy explainer for non-lawyers.");
            messages.add(systemMsg);

            // User message: our actual prompt (instructions + contract text + question)
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);

            root.set("messages", messages);

            String requestBody = objectMapper.writeValueAsString(root);

            // 2) HTTP POST to local Ollama server
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 3) Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Ollama API error: HTTP " + response.statusCode() + " - " + response.body());
            }

            // 4) Parse JSON and extract the model's reply text
            JsonNode json = objectMapper.readTree(response.body());
            JsonNode messageNode = json.get("message");
            if (messageNode == null || messageNode.get("content") == null) {
                throw new RuntimeException("Ollama API response has no message content: " + response.body());
            }

            return messageNode.get("content").asText();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error calling Ollama LLM API", e);
        }
    }
}