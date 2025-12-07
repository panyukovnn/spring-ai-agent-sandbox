package ru.panyukovnn.springaiagentsandbox.client.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.panyukovnn.springaiagentsandbox.dto.common.CommonRequest;
import ru.panyukovnn.springaiagentsandbox.dto.common.CommonResponse;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchChatsResponse;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchPrivateChatRequest;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchPublicChannelByIdRequest;
import ru.panyukovnn.springaiagentsandbox.dto.searchchathistory.SearchChatHistoryRequest;
import ru.panyukovnn.springaiagentsandbox.dto.searchchathistory.SearchChatHistoryResponse;

@FeignClient(url = "${spring-ai-agent-sandbox.integration.tg-chats-collector.host}/tg-chats-collector/api/v1", name = "tgChatsCollectorClient")
public interface TgChatsCollectorClient {

    @PostMapping("/search-private-chat")
    CommonResponse<SearchChatsResponse> postSearchPrivateChat(@RequestBody @Valid CommonRequest<SearchPrivateChatRequest> request);

    @PostMapping("/search-public-channel-by-id")
    CommonResponse<SearchChatsResponse> postSearchPublicChannelById(@RequestBody @Valid CommonRequest<SearchPublicChannelByIdRequest> request);

    @PostMapping("/search-chat-history")
    CommonResponse<SearchChatHistoryResponse> postSearchChatHistory(@RequestBody CommonRequest<SearchChatHistoryRequest> searchChatHistory);
}
