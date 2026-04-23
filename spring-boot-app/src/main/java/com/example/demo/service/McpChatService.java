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

    private final ChatClient.Builder chatClientBuilder;
    private final ToolCallbackProvider mcpTools;

    public String chat(String userMessage) {
        log.info("MCP Chat - Câu hỏi: {}", userMessage);
        try {
            String result = chatClientBuilder.build()
                    .prompt()
                    .system("""
                        Bạn là một trợ lý AI quản trị hệ thống.
                        Bạn có quyền truy vấn OpenSearch để tìm kiếm logs,
                        kiểm tra cluster health, và phân tích dữ liệu.
                        Hãy dùng các tools có sẵn khi cần thiết.
                        Trả lời bằng tiếng Việt.
                        """)
                    .user(userMessage)
                    .tools(mcpTools)
                    .call()
                    .content();
            log.info("MCP Chat - Kết quả: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error in MCP Chat: ", e);
            return "Lỗi khi xử lý MCP chat: " + e.getMessage();
        }
    }
}
