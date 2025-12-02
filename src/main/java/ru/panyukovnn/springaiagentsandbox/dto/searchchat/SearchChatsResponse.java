package ru.panyukovnn.springaiagentsandbox.dto.searchchat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchChatsResponse {

    private List<ChatInfo> chats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatInfo {

        private Long id;
        private String title;
        private String type;
        private List<TopicInfo> topics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicInfo {

        private Long id;
        private String title;
    }
}
