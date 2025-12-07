package ru.panyukovnn.springaiagentsandbox.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import ru.panyukovnn.springaiagentsandbox.client.feign.TgChatsCollectorClient;
import ru.panyukovnn.springaiagentsandbox.dto.common.CommonRequest;
import ru.panyukovnn.springaiagentsandbox.dto.common.CommonResponse;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchChatsResponse;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchPrivateChatRequest;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchPublicChannelByIdRequest;
import ru.panyukovnn.springaiagentsandbox.dto.searchchathistory.SearchChatHistoryRequest;
import ru.panyukovnn.springaiagentsandbox.dto.searchchathistory.SearchChatHistoryResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgChatsCollectorTool {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final TgChatsCollectorClient tgChatsCollectorClient;
    /**
     * Разделяет документы на малые чанки, для сохранения в векторном хранилище
     */
    private final TokenTextSplitter ragSplitter = new TokenTextSplitter(500, 100, 0, 512, true);
    /**
     * Разделяет документы на крупные части, которые могут поместиться за раз в контекст
     */
    private final TokenTextSplitter contextWindowSplitter = new TokenTextSplitter(100000, 100, 0, 50, true);

    @Tool(description = "Search private telegram chats or forums with topics")
    SearchChatsResponse searchPrivateTelegramChat(@ToolParam(description = "Full or partial name of telegram chat or forum name") String privateChatNamePart,
                                                  @ToolParam(description = "Full or partial name of telegram forum topic name", required = false) String topicNamePart) {
        log.info("Вызываю поиск приватного телеграм чата или форума: {}", privateChatNamePart);

        SearchPrivateChatRequest searchChatRequest = SearchPrivateChatRequest.builder()
            .privateChatNamePart(privateChatNamePart)
            .topicNamePart(topicNamePart)
            .build();

        CommonRequest<SearchPrivateChatRequest> commonRequest = CommonRequest.<SearchPrivateChatRequest>builder()
            .body(searchChatRequest)
            .build();

        CommonResponse<SearchChatsResponse> commonResponse = tgChatsCollectorClient.postSearchPrivateChat(commonRequest);

        return commonResponse.getBody();
    }

    @Tool(description = "Search public telegram channels")
    SearchChatsResponse searchPublicTelegramChannel(@ToolParam(description = "The unique name of channel, starts with @") String channelName) {
        log.info("Вызываю поиск публичного телеграм канала: {}", channelName);

        SearchPublicChannelByIdRequest searchChatRequest = SearchPublicChannelByIdRequest.builder()
            .publicChatName(channelName)
            .build();

        CommonRequest<SearchPublicChannelByIdRequest> commonRequest = CommonRequest.<SearchPublicChannelByIdRequest>builder()
            .body(searchChatRequest)
            .build();

        CommonResponse<SearchChatsResponse> commonResponse = tgChatsCollectorClient.postSearchPublicChannelById(commonRequest);

        return commonResponse.getBody();
    }

    // TODO добавить метод, который просто извлекает небольшую переписку


    @Tool(
        description = "Ищет ответ на вопрос в длинной переписке Telegram",
        returnDirect = true
    )
    public String answerFromTelegramWithRAG(
        @ToolParam(description = "Идентификатор чата")
        Long chatId,
        @ToolParam(required = false, description = "Идентификатор топика")
        Long topicId,
        @ToolParam(description = "Дата в формате ISO-8601, начиная с которой и до сегодняшнего дня нужно загрузить и проанализировать переписку в телеграм")
        LocalDate dateFrom,
        @ToolParam(required = false, description = "Вопрос пользователя для применения к переписке")
        String userQuestion) throws JsonProcessingException {

        // 1. Тянем ВСЮ переписку из Telegram API
        SearchChatHistoryResponse searchChatHistoryResponse = fetchSearchChatHistoryResponse(chatId, topicId, dateFrom);

        if (searchChatHistoryResponse == null) {
            return "Не удалось загрузить переписку из телеграм";
        }

        String fullChatText = objectMapper.writeValueAsString(searchChatHistoryResponse.getMessages());

        Document doc = new Document(fullChatText);
        var chunks = ragSplitter.apply(List.of(doc));
        // TODO
        /**
         * в настоящее время RAG store некорректно хранит состояние и работа с ним возможна, только при перезапуске приложения после каждого обращения
         */
        vectorStore.add(chunks);

        var request = SearchRequest.builder()
            .query(userQuestion)
            .topK(8)
            .build();

        var docs = vectorStore.similaritySearch(request);

        String context = docs.stream()
            .map(d -> d.getFormattedContent())
            .collect(Collectors.joining("\n---\n"));

        return ChatClient.create(chatModel)
            .prompt()
            .user("""
                    Вопрос пользователя: "%s"

                    Вот релевантные фрагменты переписки:

                    %s

                    Ответь, используя ТОЛЬКО этот контекст.
                    Если ответа нет — честно так и скажи.
                    """.formatted(userQuestion, context))
            .call()
            .content();
    }

//    @Tool(
//        description = "Ищет ответ на вопрос в длинной переписке Telegram, с полной загрузкой истории",
//        returnDirect = true
//    )
    public String answerFromTelegramWithoutRAG(
        @ToolParam(description = "Идентификатор чата")
        Long chatId,
        @ToolParam(required = false, description = "Идентификатор топика")
        Long topicId,
        @ToolParam(description = "Дата в формате ISO-8601, начиная с которой и до сегодняшнего дня нужно загрузить и проанализировать переписку в телеграм")
        LocalDate dateFrom,
        @ToolParam(required = false, description = "Вопрос пользователя для применения к переписке")
        String userQuestion) throws JsonProcessingException {

        // 1. Тянем ВСЮ переписку из Telegram API
        SearchChatHistoryResponse searchChatHistoryResponse = fetchSearchChatHistoryResponse(chatId, topicId, dateFrom);

        if (searchChatHistoryResponse == null) {
            return "Не удалось загрузить переписку из телеграм";
        }

        String fullChatText = objectMapper.writeValueAsString(searchChatHistoryResponse.getMessages());

        // 2. Оборачиваем в Document и режем на чанки
        Document doc = new Document(fullChatText, Map.of("chatId", chatId));
        List<Document> chunks = contextWindowSplitter.apply(List.of(doc));

        List<String> partialFindings = new ArrayList<>();

        for (Document chunk : chunks) {
            String chunkText = chunk.getFormattedContent();

            String partial = ChatClient.create(chatModel)
                .prompt()
                .user("""
                    У тебя есть часть переписки в Telegram.
                    Вопрос пользователя: "%s"

                    Вот фрагмент переписки:
                    ======================
                    %s
                    ======================

                    1. Если в этом фрагменте есть что-то, что помогает ответить на вопрос — выпиши это в виде списка пунктов (с датами/авторами, если есть).
                    2. Если ничего полезного нет — напиши только "NO_INFO".
                    """.formatted(userQuestion, chunkText))
                .call()
                .content();

            if (!"NO_INFO".equals(partial)) {
                partialFindings.add(partial);
            }
        }

        if (partialFindings.isEmpty()) {
            return "В переписке нет информации, которая отвечает на этот вопрос.";
        }

        // 3. Можно либо собрать результат руками, либо ещё раз обратиться к LLM
        String joined = String.join("\n\n", partialFindings);

        return ChatClient.create(chatModel)
            .prompt()
            .user("""
                Пользователь задал вопрос: "%s".

                Ниже собраны фрагменты из переписки, которые могут быть релевантны:

                %s

                На основе этих данных:
                1. Дай максимально точный ответ.
                2. Если есть несколько версий, аккуратно разрули противоречия.
                3. Если ответа всё равно нет, честно скажи об этом.
                """.formatted(userQuestion, joined))
            .call()
            .content();
    }

    @Nullable
    private SearchChatHistoryResponse fetchSearchChatHistoryResponse(Long chatId, Long topicId, LocalDate dateFrom) {
        log.info("Вызываю загрузку сообщений из телеграм чата: {}. Топик: {}. Начиная с даты: {}", chatId, topicId, dateFrom);

        try {
            SearchChatHistoryRequest searchChatHistoryRequest = SearchChatHistoryRequest.builder()
                .chatId(chatId)
                .topicId(topicId)
                .dateFrom(LocalDateTime.of(dateFrom, LocalTime.MIN))
                .build();

            CommonRequest<SearchChatHistoryRequest> commonRequest = CommonRequest.<SearchChatHistoryRequest>builder()
                .body(searchChatHistoryRequest)
                .build();

            CommonResponse<SearchChatHistoryResponse> commonResponse = tgChatsCollectorClient.postSearchChatHistory(commonRequest);

            return commonResponse.getBody();
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            return null;
        }
    }
}
