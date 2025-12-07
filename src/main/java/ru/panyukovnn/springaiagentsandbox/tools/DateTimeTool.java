package ru.panyukovnn.springaiagentsandbox.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class DateTimeTool {

    @Tool(description = "Current dateTime")
    LocalDateTime currentDateTime() {
        log.info("Вызван инструмент определения текущей даты");

        return LocalDateTime.now();
    }
}
