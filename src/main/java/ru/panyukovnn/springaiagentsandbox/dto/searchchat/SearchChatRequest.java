package ru.panyukovnn.springaiagentsandbox.dto.searchchat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchChatRequest {

    @Schema(description = "Полное имя публичного чата/канала (начинается с знака @)")
    private String publicChatName;

    @Schema(description = "Часть имени приватного чата")
    private String privateChatNamePart;

    @Schema(description = "Часть имени топика в форуме")
    private String topicNamePart;
}
