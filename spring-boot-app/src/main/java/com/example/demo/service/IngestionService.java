package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class IngestionService {

    private final VectorStore vectorStore;

    /**
     * Nạp tài liệu từ một Resource (File PDF, Word, MD...)
     */
    public void ingest(Resource resource) {
        log.info("Bắt đầu nạp tài liệu: {}", resource.getFilename());
        
        // 1. Đọc nội dung từ file (Tự động nhận diện định dạng qua Tika)
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();
        
        // 2. Chia nhỏ tài liệu thành các đoạn (Chunks)
        // Mỗi đoạn khoảng 800 tokens để AI dễ xử lý
        TokenTextSplitter splitter = new TokenTextSplitter(800, 200, 5, 10000, true);
        List<Document> chunks = splitter.apply(documents);
        
        log.info("Đã chia nhỏ tài liệu thành {} đoạn.", chunks.size());
        
        // 3. Lưu vào Vector Store (OpenSearch)
        // Spring AI sẽ tự động gọi Ollama để tạo Embedding trước khi lưu
        vectorStore.add(chunks);
        
        log.info("Đã lưu các đoạn vector vào OpenSearch thành công.");
    }
}
