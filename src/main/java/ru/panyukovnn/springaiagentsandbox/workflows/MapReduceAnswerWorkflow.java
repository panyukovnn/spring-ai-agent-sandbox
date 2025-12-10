package ru.panyukovnn.springaiagentsandbox.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Данный workflow позволяет задать вопрос пользователя и применить его к большому массиву данных, для поиска ответа.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapReduceAnswerWorkflow {

    private static final int RESPONSE_MAX_TOKENS = 2000;
    private static final double ZERO_TEMPERATURE = 0.0;
    private static final Integer PARALLEL_THREADS_NUM = 5;
    private static final String NO_INFO_LLM_RESPONSE = "NO_INFO";
    private static final String MAP_PROMPT = """
        Пользователь просит выполнить следующее действие с информацией ниже: "%s"
        Если информация для ответа на вопрос не найдена, то напиши "%s".
        
        Вот фрагмент данных:
        ======================
        %s
        ======================
        """;
    private static final String SUMMARIZING_PROMPT = """
        Пользователь задал вопрос: "%s".
        
        Ниже собраны фрагменты полезной информации, которые могут быть релевантны:
        ======================
        %s
        ======================
        
        На основе этих данных:
        1. Дай максимально точный ответ.
        2. Если есть несколько версий, аккуратно разреши противоречия.
        3. Если ответа всё равно нет, честно скажи об этом.
        """;

    private final ChatClient chatClient;
    private final TokenCountEstimator tokenCountEstimator = new JTokkitTokenCountEstimator();
    /**
     * Разделяет документы на крупные части, которые могут поместиться за раз в контекст
     */
    private final TokenTextSplitter contextWindowSplitter = new TokenTextSplitter(190000, 100, 0, 50, true);

    /**
     * Применяет алгоритм map-reduce к большому массиву текста, для поиска ответа на вопрос пользователя
     *
     * @param userQuestion вопрос пользователя
     * @param rawData      данные, в которых необходимо найти ответ на вопрос пользователя
     * @return объединенный результат пересказа контента
     */
    public Optional<String> mapReduce(String userQuestion, String rawData) {
        Document doc = new Document(rawData);
        List<Document> chunks = contextWindowSplitter.apply(List.of(doc));

        List<String> relevantChunksSummarization = mapQuestionToChunks(userQuestion, chunks);

        if (relevantChunksSummarization.isEmpty()) {
            return Optional.empty();
        }

        if (relevantChunksSummarization.size() == 1) {
            return Optional.of(relevantChunksSummarization.getFirst());
        }

        if (relevantChunksSummarization.size() > 10) {
            throw new IllegalStateException("Количество данных слишком велико для выполнения mapReduce");
        }

        String joined = String.join("\n\n", relevantChunksSummarization);

        String summarizingPrompt = SUMMARIZING_PROMPT.formatted(userQuestion, joined);

        String finalSummary = callLlmWithTokenEstimation(summarizingPrompt, "finalSummary");

        return Optional.ofNullable(finalSummary);
    }

    private List<String> mapQuestionToChunks(String userQuestion, List<Document> chunks) {
        try (ExecutorService fixedExecutorService = Executors.newFixedThreadPool(PARALLEL_THREADS_NUM)) {
            List<CompletableFuture<String>> chunkSummarizationFutures = new ArrayList<>();

            chunks.forEach(chunk -> {
                String chunkText = chunk.getFormattedContent();

                CompletableFuture<String> chunkSummarizationFuture = CompletableFuture.supplyAsync(
                    () -> summarizeSingleChunk(userQuestion, chunkText), fixedExecutorService);

                chunkSummarizationFutures.add(chunkSummarizationFuture);
            });


            return chunkSummarizationFutures.stream()
                .map(CompletableFuture::join)
                .toList();
        }
    }

    private String summarizeSingleChunk(String userQuestion, String chunkText) {
        String mapPrompt = MAP_PROMPT.formatted(userQuestion, NO_INFO_LLM_RESPONSE, chunkText);

        String chunkSummary = callLlmWithTokenEstimation(mapPrompt, "mapChunk");

        return NO_INFO_LLM_RESPONSE.equals(chunkSummary)
            ? ""
            : chunkSummary;
    }

    private String callLlmWithTokenEstimation(String prompt, String scenario) {
        int estimatedPromptTokens = tokenCountEstimator.estimate(prompt);

        String chunkSummary = chatClient
            .prompt(prompt)
            .options(ChatOptions.builder()
                .temperature(ZERO_TEMPERATURE)
                .maxTokens(RESPONSE_MAX_TOKENS)
                .build())
            .call()
            .content();

        int estimatedResponseTokens = tokenCountEstimator.estimate(chunkSummary);

        log.info("Выполнен вызов LLM при {}, потрачено токенов: {}", scenario, estimatedPromptTokens + estimatedResponseTokens);

        return chunkSummary;
    }
}
