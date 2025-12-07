package ru.panyukovnn.springaiagentsandbox.dto.searchchathistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.panyukovnn.springaiagentsandbox.dto.TgMessageDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchChatHistoryResponse {

    private Long chatId;
    private String chatTitle;
    private String chatPublicName;
    private String topicName;
    private Long topicId;
    private Integer totalCount;
    private List<TgMessageDto> messages;
}
