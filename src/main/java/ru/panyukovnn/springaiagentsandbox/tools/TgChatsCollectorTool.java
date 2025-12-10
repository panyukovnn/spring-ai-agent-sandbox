package ru.panyukovnn.springaiagentsandbox.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import ru.panyukovnn.springaiagentsandbox.client.feign.TgChatsCollectorClient;
import ru.panyukovnn.springaiagentsandbox.dto.common.CommonRequest;
import ru.panyukovnn.springaiagentsandbox.dto.common.CommonResponse;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchChatsResponse;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchPrivateChatRequest;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchPublicChannelByIdRequest;
import ru.panyukovnn.springaiagentsandbox.dto.searchchathistory.SearchChatHistoryRequest;
import ru.panyukovnn.springaiagentsandbox.dto.searchchathistory.SearchChatHistoryResponse;
import ru.panyukovnn.springaiagentsandbox.workflows.MapReduceAnswerWorkflow;
import ru.panyukovnn.springaiagentsandbox.workflows.RagAnswerWorkflow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgChatsCollectorTool {

    private final ObjectMapper objectMapper;
    private final RagAnswerWorkflow ragAnswerWorkflow;
    private final TgChatsCollectorClient tgChatsCollectorClient;
    private final MapReduceAnswerWorkflow mapReduceAnswerWorkflow;

    // TODO добавить метод, который просто извлекает небольшую переписку

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

    @Tool(
        description = "Ищет ответ на вопрос в длинной переписке Telegram",
        returnDirect = true
    )
    public String answerFromTelegram(
        @ToolParam(description = "Идентификатор чата")
        Long chatId,
        @ToolParam(required = false, description = "Идентификатор топика")
        Long topicId,
        @ToolParam(description = "Дата в формате ISO-8601, начиная с которой и до сегодняшнего дня нужно загрузить и проанализировать переписку в телеграм")
        LocalDate dateFrom,
        @ToolParam(required = false, description = "Вопрос пользователя для применения к переписке")
        String userQuestion,
        @ToolParam(required = false, description = "Признак использования RAG индексирования материалов, указывается, только если явно упомянут пользователем")
        Boolean useRag) throws JsonProcessingException {

        SearchChatHistoryResponse searchChatHistoryResponse = fetchSearchChatHistoryResponse(chatId, topicId, dateFrom);

        if (searchChatHistoryResponse == null) {
            return "Не удалось загрузить переписку из телеграм";
        }

        String fullChatText = objectMapper.writeValueAsString(searchChatHistoryResponse.getMessages());

        if (Boolean.TRUE.equals(useRag)) {
            return ragAnswerWorkflow.answerWithRag(userQuestion, fullChatText)
                .orElse("В представленной переписке нет данных для ответа на ваш вопрос");
        }

        return mapReduceAnswerWorkflow.mapReduce(userQuestion, fullChatText)
            .orElse("В представленной переписке нет данных для ответа на ваш вопрос");
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
