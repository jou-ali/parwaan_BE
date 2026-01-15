package com.parwaan.portfolio.service;

import com.parwaan.portfolio.dto.ChatRequest;
import com.parwaan.portfolio.dto.ChatResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

	public ChatResponse getChatResponse(ChatRequest request) {
		String question = request == null || request.getMessage() == null ? "" : request.getMessage().trim();
		String answer = "Stub reply (server): I received your question: \"" + question + "\" — This is a canned response. (" + Instant.now().toString() + ")";

		String sessionId = request != null && request.getSessionId() != null && !request.getSessionId().isBlank()
				? request.getSessionId()
				: UUID.randomUUID().toString();

		return new ChatResponse(answer, List.of("stub-source"), sessionId);
	}
}
