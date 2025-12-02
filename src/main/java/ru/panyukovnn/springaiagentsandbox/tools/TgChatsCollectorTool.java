package ru.panyukovnn.springaiagentsandbox.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import ru.panyukovnn.springaiagentsandbox.client.feign.TgChatsCollectorClient;
import ru.panyukovnn.springaiagentsandbox.dto.common.CommonRequest;
import ru.panyukovnn.springaiagentsandbox.dto.common.CommonResponse;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchChatRequest;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchChatsResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgChatsCollectorTool {

    private final TgChatsCollectorClient tgChatsCollectorClient;

    @Tool(description = "Search private telegram chats by name part")
    SearchChatsResponse getYoutubeVideoSubtitles(String privateChatNamePart) {
        log.info("Вызываю поиск приватного чата в tg-chats-collector: {}", privateChatNamePart);

        SearchChatRequest searchChatRequest = SearchChatRequest.builder()
            .privateChatNamePart(privateChatNamePart)
            .build();

        CommonRequest<SearchChatRequest> commonRequest = CommonRequest.<SearchChatRequest>builder()
            .body(searchChatRequest)
            .build();

        CommonResponse<SearchChatsResponse> commonResponse = tgChatsCollectorClient.postSearchChat(commonRequest);

        return commonResponse.getBody();
    }
}
