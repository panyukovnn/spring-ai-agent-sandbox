package ru.panyukovnn.springaiagentsandbox.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TavilyWebSearchTool {

    private final RestClient restClient = RestClient.create(); // TODO

    @Value("${spring-ai-agent-sandbox.api-keys.tavily}")
    private String tavilyApiKey;

    @Tool(description = "Search the web for current information")
    public String apply(@ToolParam(description = "Web search request") String query) {

        return restClient.post()
            .uri("https://api.tavily.com/search")
            .header("Content-Type", "application/json")
            .body(Map.of(
                "api_key", tavilyApiKey,
                "query", query,
                "max_results", 5
            ))
            .retrieve()
            .body(String.class);
    }
}
