package com.example.agenticrag;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfiguration {

    @Bean
    ToolCallbackProvider knowledgeToolCallbacks(KnowledgeTools knowledgeTools) {
        return MethodToolCallbackProvider.builder().toolObjects(knowledgeTools).build();
    }
}
