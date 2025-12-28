package ru.panyukovnn.springaiagentsandbox.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.panyukovnn.springaiagentsandbox.tools.DateTimeTool;
import ru.panyukovnn.springaiagentsandbox.tools.TavilyWebSearchTool;
import ru.panyukovnn.springaiagentsandbox.tools.TgChatsCollectorTool;
import ru.panyukovnn.springaiagentsandbox.tools.YtSubtitlesTool;

/**
 * Shell команды для демонстрации работы Spring AI с DeepSeek
 */
@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class AiCommands {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final DateTimeTool dateTimeTool;
    private final YtSubtitlesTool ytSubtitlesTool;
    private final TgChatsCollectorTool tgChatsCollectorTool;
    private final TavilyWebSearchTool tavilyWebSearchTool;

    /**
     * Отправить простой запрос к AI модели
     *
     * @param message текст запроса
     * @return ответ от AI модели
     */
    @ShellMethod(key = "ask", value = "Отправить запрос к AI модели")
    public String ask(@ShellOption(value = {"-m", "--message"}, help = "Текст запроса") String message) {
        log.info("Отправка запроса к AI: {}", message);

        String response = chatClient
            .prompt(message)
            .call()
            .content();

        log.info("Получен ответ от AI");

        return response;
    }

    /**
     * Отправить простой запрос к AI модели с добавлением инструмента для загрузки субтитров из Youtube
     *
     * @param message текст запроса
     * @return ответ от AI модели
     */
    @ShellMethod(key = "askYt", value = "Спросить модель с добавлением инструмента по загрузке Youtube субтитров из видео")
    public String askYt(@ShellOption(value = {"-m", "--message"}, help = "Текст запроса") String message) {
        log.info("Отправка запроса к AI: {}", message);

        String response = chatClient
            .prompt(message)
            .tools(ytSubtitlesTool)
            .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .call()
            .content();

        log.info("Получен ответ от AI");

        return response;
    }

    /**
     * Отправить простой запрос к AI модели с добавлением инструмента для поиска чата телеграм
     *
     * @param message текст запроса
     * @return ответ от AI модели
     */
    @ShellMethod(key = "askTg", value = "Спросить модель с добавлением инструментов для работы с клиентом телеграм")
    public String askTg(@ShellOption(value = {"-m", "--message"}, help = "Текст запроса") String message) {
        log.info("Отправка запроса к AI: {}", message);

        ChatResponse chatResponse = chatClient
            .prompt(message)
            .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .tools(tgChatsCollectorTool, dateTimeTool)
            .call()
            .chatResponse();

        log.info("Получен ответ от AI. Токены: {}", chatResponse.getMetadata().getUsage().getTotalTokens());

        return chatResponse.getResult().getOutput().getText();
    }

    @ShellMethod(key = "searchWeb", value = "Спросить с поиском в интернете")
    public String searchWeb(@ShellOption(value = {"-m", "--message"}, help = "Промт") String message) {
        log.info("Отправка запроса к AI: {}", message);

        ChatResponse chatResponse = chatClient
            .prompt(message)
            .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .tools(tavilyWebSearchTool)
            .call()
            .chatResponse();

        log.info("Получен ответ от AI. Токены: {}", chatResponse.getMetadata().getUsage().getTotalTokens());

        return chatResponse.getResult().getOutput().getText();
    }
}