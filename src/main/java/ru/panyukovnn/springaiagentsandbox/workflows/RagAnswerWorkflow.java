package ru.panyukovnn.springaiagentsandbox.workflows;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Данный workflow позволяет задать вопрос пользователя и применить его к большому массиву данных, для поиска ответа.
 */
@Service
@RequiredArgsConstructor
public class RagAnswerWorkflow {

    private static final int TOP_K = 20;
    private static final String SUMMARIZING_PROMPT = """
                Вопрос пользователя: "%s"
                
                Вот релевантные фрагменты переписки:
                ======================
                %s
                ======================
                
                Ответь, используя ТОЛЬКО этот контекст.
                Если ответа нет — честно так и скажи.
                """;

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    /**
     * Разделяет документы на малые чанки, для сохранения в векторном хранилище
     */
    private final TokenTextSplitter ragSplitter = new TokenTextSplitter(500, 100, 0, 512, true);

    /**
     * Применяет алгоритм map-reduce к большому массиву текста, для поиска ответа на вопрос пользователя
     *
     * @param userQuestion вопрос пользователя
     * @param rawData      данные, в которых необходимо найти ответ на вопрос пользователя
     * @return объединенный результат пересказа контента
     */
    public Optional<String> answerWithRag(String userQuestion, String rawData) {
        Document doc = new Document(rawData);
        List<Document> chunks = ragSplitter.apply(List.of(doc));

        VectorStore vectorStore = createVectorStore();
        vectorStore.add(chunks);

        SearchRequest searchRequest = SearchRequest.builder()
            .query(userQuestion)
            .topK(TOP_K)
            .build();

        List<Document> chunksFromRag = vectorStore.similaritySearch(searchRequest);

        String context = chunksFromRag.stream()
            .map(Document::getFormattedContent)
            .collect(Collectors.joining("\n---\n"));

        String foundedAnswer = chatClient
            .prompt(SUMMARIZING_PROMPT.formatted(userQuestion, context))
            .call()
            .content();

        return Optional.ofNullable(foundedAnswer);
    }

    /**
     * @return in-memory векторное хранилище.
     */
    private VectorStore createVectorStore() {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
