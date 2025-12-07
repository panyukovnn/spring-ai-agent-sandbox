package ru.panyukovnn.springaiagentsandbox.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SandboxConfig {

    @Bean
    public ToolExecutionExceptionProcessor toolExecutionExceptionProcessor() {
        return new DefaultToolExecutionExceptionProcessor(true);
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // in-memory хранилище
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
