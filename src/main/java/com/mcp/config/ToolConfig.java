package com.mcp.config;

import com.mcp.tool.CommandTools;
import com.mcp.tool.DirectoryTools;
import com.mcp.tool.FileTools;
import com.mcp.tool.WebTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ToolConfig {
    @Bean
    public List<ToolCallback> allToolCallbacks(FileTools fileTools,
                                               DirectoryTools directoryTools,
                                               CommandTools commandTools,
                                               WebTools webTools) {
        return List.of(ToolCallbacks.from(fileTools, directoryTools, commandTools, webTools));
    }
}
