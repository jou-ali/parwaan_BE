package com.parwaan.portfolio.controller;

import com.parwaan.portfolio.dto.ChatRequest;
import com.parwaan.portfolio.dto.ChatResponse;
import com.parwaan.portfolio.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
// import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:3000") // allow frontend dev server
@RequiredArgsConstructor
public class ChatController {

    // For MVP stub we don't need a service layer. Add one later if you want.
    private final ChatService chatService;

    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody ChatRequest request) {
        // Simple canned reply that echoes question + timestamp
        String question = request.getMessage() == null ? "" : request.getMessage().trim();
        String reply = "Stub reply (server): I received your question: \"" + question +
                "\" — This is a canned response. (" + Instant.now().toString() + ")";

        // You can also return structured data: answer + sources + sessionId
        ChatResponse response = new ChatResponse(reply, List.of("stub-source"), null);
    public ResponseEntity<ChatResponse> query(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.getChatResponse(request);
        return ResponseEntity.ok(response);
    }

    // DTOs (could be moved to separate files)
    public static class ChatRequest {
        private String message;
        private String sessionId; // optional: to continue context

        public ChatRequest() {}
        public ChatRequest(String message, String sessionId) { this.message = message; this.sessionId = sessionId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }

    public static class ChatResponse {
        private String answer;
        private List<String> sources;
        private String sessionId;
        public ChatResponse() {}
        public ChatResponse(String answer, List<String> sources, String sessionId) {
            this.answer = answer; this.sources = sources; this.sessionId = sessionId;
        }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public List<String> getSources() { return sources; }
        public void setSources(List<String> sources) { this.sources = sources; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
}