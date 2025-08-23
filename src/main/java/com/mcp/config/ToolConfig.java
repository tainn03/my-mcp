package com.mcp.config;

import com.mcp.tool.FileTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ToolConfig {
    @Bean
    public List<ToolCallback> allToolCallbacks(FileTools fileTools) {
        return List.of(ToolCallbacks.from(fileTools));
    }
}
