package com.example.demo.controller;

import com.example.demo.service.McpChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpChatService mcpChatService;

    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam("q") String query) {
        String answer = mcpChatService.chat(query);
        return ResponseEntity.ok(answer);
    }
}
