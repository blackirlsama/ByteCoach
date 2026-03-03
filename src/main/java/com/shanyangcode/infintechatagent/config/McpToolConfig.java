package com.shanyangcode.infintechatagent.config;

import java.util.Arrays;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Value("${bigmodel.api-key}")
    private String apiKey;

    @Bean
    public McpToolProvider mcpToolProvider() {



        McpTransport searchTransport = new HttpMcpTransport.Builder()
                .sseUrl("https://open.bigmodel.cn/api/mcp/web_search/sse?Authorization=" + apiKey.trim()) // 去掉空格并 trim
                .build();

        McpClient searchClient = new DefaultMcpClient.Builder()
                .key("BigModelSearchMcpClient")
                .transport(searchTransport)
                .build();


        McpTransport timeTransport = new StdioMcpTransport.Builder()
                .command(Arrays.asList("uvx", "mcp-server-time", "--local-timezone=Asia/Shanghai"))
                .build();

        McpClient timeClient = new DefaultMcpClient.Builder()
                .key("timeClient")
                .transport(timeTransport)
                .build();


        return McpToolProvider.builder()
                .mcpClients(Arrays.asList(timeClient, searchClient))
                .build();
    }
}