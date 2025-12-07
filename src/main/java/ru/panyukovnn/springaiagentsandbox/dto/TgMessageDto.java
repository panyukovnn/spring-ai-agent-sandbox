package ru.panyukovnn.springaiagentsandbox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TgMessageDto {

    private Long messageId;
    private Long senderId;
    private LocalDateTime dateTime;
    private String text;
    /**
     * Текст сообщения, на который отвечает текущее сообщение
     */
    private String replyToText;
    private Long replyToMessageId;
}
