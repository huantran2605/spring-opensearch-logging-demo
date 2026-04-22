package com.example.demo.controller;

import com.example.demo.service.ChatService;
import com.example.demo.service.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final IngestionService ingestionService;
    private final ChatService chatService;

    /**
     * Upload tài liệu để AI học
     */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        // Lưu tạm file để Tika có thể đọc
        Path tempFile = Files.createTempFile("knowledge-", file.getOriginalFilename());
        file.transferTo(tempFile.toFile());

        try {
            ingestionService.ingest(new FileSystemResource(tempFile.toFile()));
            return ResponseEntity.ok("Đã nạp kiến thức từ file: " + file.getOriginalFilename());
        } finally {
            // Xóa file tạm sau khi nạp xong
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Hỏi AI dựa trên kiến thức đã nạp
     */
    @GetMapping("/ask")
    public ResponseEntity<String> ask(@RequestParam("q") String query) {
        String answer = chatService.ask(query);
        return ResponseEntity.ok(answer);
    }
}
