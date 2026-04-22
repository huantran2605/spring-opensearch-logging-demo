package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;

    /**
     * Quy trình RAG:
     * 1. Tìm tài liệu liên quan trong OpenSearch.
     * 2. Xây dựng Prompt với ngữ cảnh tìm được.
     * 3. Gửi cho LLM (Ollama) trả lời.
     */
    public String ask(String query) {
        log.info("Người dùng hỏi: {}", query);

        // 1. Tìm kiếm các đoạn tài liệu liên quan nhất
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(3) // Lấy top 3 đoạn liên quan nhất
                        .build()
        );

        String context = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        log.info("Đã tìm thấy ngữ cảnh từ {} đoạn tài liệu.", similarDocuments.size());

        // 2. Xây dựng thông điệp hệ thống (System Prompt)
        String systemInstructions = """
                Bạn là một trợ lý AI thông minh. 
                Hãy trả lời câu hỏi của người dùng dựa trên PHẦN NGỮ CẢNH được cung cấp dưới đây.
                Nếu thông tin không có trong ngữ cảnh, hãy nói rằng bạn không biết, đừng tự bịa ra câu trả lời.
                
                PHẦN NGỮ CẢNH:
                %s
                """.formatted(context);

        // 3. Gọi LLM qua ChatClient
        return chatClientBuilder.build()
                .prompt()
                .system(systemInstructions)
                .user(query)
                .call()
                .content();
    }
}
