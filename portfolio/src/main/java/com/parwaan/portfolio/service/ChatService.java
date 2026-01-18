package com.parwaan.portfolio.service;

import com.parwaan.portfolio.dto.ChatRequest;
import com.parwaan.portfolio.dto.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChatService {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    
	

	@Value("${app.openai.key:}")
	private String openaiKey;

	@Value("${app.openai.model:gpt-4o-mini}")
	private String openaiModel;

	// No fallback — OpenAI only

	private final ObjectMapper mapper = new ObjectMapper();
	private final HttpClient http = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	public java.util.concurrent.CompletableFuture<ChatResponse> getChatResponse(ChatRequest request) {
		String message = request == null || request.getMessage() == null ? "" : request.getMessage().trim();
		String sessionId = (request != null && request.getSessionId() != null && !request.getSessionId().isBlank())
				? request.getSessionId()
				: UUID.randomUUID().toString();

		if (openaiKey == null || openaiKey.isBlank()) {
			log.warn("OpenAI key not configured (app.openai.key)");
			String msg = "OpenAI API key not configured. Set the environment variable APP_OPENAI_KEY or app.openai.key in application.properties.";
			return java.util.concurrent.CompletableFuture.completedFuture(new ChatResponse(msg, List.of(), sessionId));
		}

		try {
			List<Map<String, String>> messagesList = List.of(Map.of("role", "user", "content", message));

			Map<String, Object> payload = Map.of(
					"model", openaiModel,
					"messages", messagesList
			);
			String body = mapper.writeValueAsString(payload);

			HttpRequest req = HttpRequest.newBuilder()
					.uri(URI.create("https://api.openai.com/v1/chat/completions"))
					.timeout(Duration.ofSeconds(30))
					.header("Authorization", "Bearer " + openaiKey)
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(body))
					.build();

			return http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
					.thenApply(resp -> {
						try {
							if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
								JsonNode root = mapper.readTree(resp.body());
								JsonNode choice = root.path("choices").isArray() && root.path("choices").size() > 0
										? root.path("choices").get(0)
										: null;
								String answer = null;
								if (choice != null && choice.has("message")) {
									answer = choice.path("message").path("content").asText(null);
								} else if (root.has("output") && root.path("output").isArray() && root.path("output").size() > 0) {
									answer = root.path("output").get(0).path("content").asText(null);
								}
								if (answer != null) {
									return new ChatResponse(answer, List.of("openai"), sessionId);
								}
								log.warn("OpenAI response missing message content: {}", resp.body());
								return new ChatResponse("OpenAI returned no content", List.of(), sessionId);
							} else {
								log.warn("OpenAI request failed (status={}): {}", resp.statusCode(), resp.body());
								return new ChatResponse("OpenAI request failed (status=" + resp.statusCode() + ")", List.of(), sessionId);
							}
						} catch (Exception e) {
							log.warn("Error parsing OpenAI response: {}", e.getMessage());
							return new ChatResponse("Error parsing OpenAI response", List.of(), sessionId);
						}
					})
					.exceptionally(t -> {
						log.warn("OpenAI async request error: {}", t.getMessage());
						return new ChatResponse("OpenAI request error: " + t.getMessage(), List.of(), sessionId);
					});
		} catch (Exception e) {
			log.warn("Unexpected error preparing OpenAI request: {}", e.getMessage());
			return java.util.concurrent.CompletableFuture.completedFuture(new ChatResponse("Unexpected error: " + e.getMessage(), List.of(), sessionId));
		}
	}
}
