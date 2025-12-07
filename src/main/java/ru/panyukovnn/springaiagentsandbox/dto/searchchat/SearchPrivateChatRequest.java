package ru.panyukovnn.springaiagentsandbox.dto.searchchat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchPrivateChatRequest {

    @NotEmpty(message = "Имя приватного чата для поиска не может быть пустым")
    @Schema(description = "Часть имени приватного чата")
    private String privateChatNamePart;

    @Schema(description = "Часть имени топика в форуме")
    private String topicNamePart;
}
