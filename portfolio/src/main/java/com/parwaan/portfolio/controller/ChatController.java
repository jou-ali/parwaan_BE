package com.parwaan.portfolio.controller;

import com.parwaan.portfolio.dto.ChatRequest;
import com.parwaan.portfolio.dto.ChatResponse;
import com.parwaan.portfolio.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/query")
    public ResponseEntity<ChatResponse> query(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.getChatResponse(request);
        return ResponseEntity.ok(response);
    }

}