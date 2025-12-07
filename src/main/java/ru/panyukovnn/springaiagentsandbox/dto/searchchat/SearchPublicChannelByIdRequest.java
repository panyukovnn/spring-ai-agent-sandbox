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
public class SearchPublicChannelByIdRequest {

    @NotEmpty(message = "Имя публичного канала не может быть пустым")
    @Schema(description = "Полное имя публичного чата/канала (начинается с знака @)")
    private String publicChatName;
}
