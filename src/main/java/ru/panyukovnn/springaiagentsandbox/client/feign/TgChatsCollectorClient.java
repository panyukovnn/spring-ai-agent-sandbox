package ru.panyukovnn.springaiagentsandbox.client.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.panyukovnn.springaiagentsandbox.dto.common.CommonRequest;
import ru.panyukovnn.springaiagentsandbox.dto.common.CommonResponse;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchChatRequest;
import ru.panyukovnn.springaiagentsandbox.dto.searchchat.SearchChatsResponse;

@FeignClient(url = "${spring-ai-agent-sandbox.integration.tg-chats-collector.host}/tg-chats-collector/api/v1", name = "tgChatsCollectorClient")
public interface TgChatsCollectorClient {

    @PostMapping("/search-chat")
    CommonResponse<SearchChatsResponse> postSearchChat(@RequestBody CommonRequest<SearchChatRequest> request);
}
