package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class McpChatService {

    private static final int MAX_RETRIES = 2;

    private final ChatClient.Builder chatClientBuilder;
    private final ToolCallbackProvider mcpTools;

    private String getSystemPrompt() {
        return """
                Bạn là một trợ lý AI quản trị hệ thống. Bạn PHẢI sử dụng các công cụ (tools) được cung cấp để trả lời câu hỏi.
                
                QUY TẮC QUAN TRỌNG:
                1. BẠN PHẢI GỌI TOOL ĐỂ LẤY DỮ LIỆU. KHÔNG ĐƯỢC chỉ mô tả cách gọi tool hay in ra JSON.
                2. Nếu tool trả về lỗi, hãy thử lại với tham số khác.
                3. Trả lời NGẮN GỌN và TRỰC TIẾP vào câu hỏi. Ví dụ: "Có 5 log ERROR ngày hôm qua."
                
                Thông tin OpenSearch:
                - Hôm nay là %s
                - Pattern tên index của log: spring-logs-YYYY.MM.dd (ví dụ: spring-logs-2026.04.23)
                - Các trường dữ liệu: @timestamp, level (ERROR/WARN/INFO/DEBUG), message, logger_name, thread_name
                
                Hướng dẫn tìm kiếm:
                - Để đếm số lượng log (ví dụ: đếm log ERROR), gọi SearchIndexTool với query_dsl có chứa tham số tìm kiếm, size=0, và có thể truyền fields="count(*) as count".
                - KHÔNG IN RA JSON. HÃY THỰC SỰ GỌI TOOL.
                """.formatted(java.time.LocalDate.now());
    }

    public String chat(String userMessage) {
        log.info("MCP Chat - Câu hỏi: {}", userMessage);
        try {
            String result = callLlm(userMessage);

            // Detect if the model dumped a JSON tool call as text instead of actually calling it
            // This is a known issue with smaller local models (e.g. llama3.1:8B)
            for (int retry = 0; retry < MAX_RETRIES && looksLikeFailedToolCall(result); retry++) {
                log.warn("MCP Chat - Phát hiện tool call bị dump thành text, thử lại lần {} ...", retry + 1);
                result = callLlm(userMessage + "\n\nIMPORTANT: Do NOT write JSON in your answer. Actually USE the tools by calling them. Give me a direct answer.");
            }

            log.info("MCP Chat - Kết quả: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error in MCP Chat: ", e);
            return "Lỗi khi xử lý MCP chat: " + e.getMessage();
        }
    }

    private String callLlm(String userMessage) {
        return chatClientBuilder.build()
                .prompt()
                .system(getSystemPrompt())
                .user(userMessage)
                .tools(mcpTools)
                .call()
                .content();
    }

    /**
     * Heuristic to detect if the LLM output raw JSON tool call text
     * instead of actually invoking the tool via the function-calling API.
     */
    private boolean looksLikeFailedToolCall(String result) {
        if (result == null) return false;
        return (result.contains("\"name\"") && result.contains("\"parameters\""))
                || (result.contains("SearchIndexTool") && result.contains("query_dsl"))
                || (result.contains("ListIndexTool") && result.contains("{"));
    }
}
