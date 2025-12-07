package ru.panyukovnn.springaiagentsandbox.dto.searchchathistory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchChatHistoryRequest {

    @NotNull
    @Schema(description = "Идентификатор чата")
    private Long chatId;

    @Schema(description = "Идентификатор топика")
    private Long topicId;

    @NotNull
    @Schema(description = "Дата начала периода, в UTC")
    private LocalDateTime dateFrom;

    @Schema(description = "Принудительно перезагрузить историю из Telegram, игнорируя кэш в БД")
    private Boolean returnFromDb = false;
}
