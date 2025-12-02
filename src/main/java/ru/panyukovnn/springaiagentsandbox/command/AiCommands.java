package ru.panyukovnn.springaiagentsandbox.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.panyukovnn.springaiagentsandbox.tools.TgChatsCollectorTool;
import ru.panyukovnn.springaiagentsandbox.tools.YtSubtitlesTool;

/**
 * Shell команды для демонстрации работы Spring AI с DeepSeek
 */
@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class AiCommands {

    private final ChatModel chatModel;
    private final YtSubtitlesTool ytSubtitlesTool;
    private final TgChatsCollectorTool tgChatsCollectorTool;

    /**
     * Отправить простой запрос к AI модели
     *
     * @param message текст запроса
     * @return ответ от AI модели
     */
    @ShellMethod(key = "ask", value = "Отправить запрос к AI модели")
    public String ask(@ShellOption(value = {"-m", "--message"}, help = "Текст запроса") String message) {
        log.info("Отправка запроса к AI: {}", message);

        String response = chatModel.call(message);

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

        String response = ChatClient.create(chatModel)
            .prompt(message)
            .tools(ytSubtitlesTool)
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

        String response = ChatClient.create(chatModel)
            .prompt(message)
            .tools(tgChatsCollectorTool)
            .call()
            .content();

        log.info("Получен ответ от AI");

        return response;
    }

    /**
     * Отправить запрос с настройкой параметров
     *
     * @param message текст запроса
     * @param temperature температура генерации (0.0 - 2.0)
     * @param maxTokens максимальное количество токенов в ответе
     * @return ответ от AI модели
     */
    @ShellMethod(key = "ask-custom", value = "Отправить запрос с кастомными параметрами")
    public String askWithCustomParameters(
            @ShellOption(value = {"-m", "--message"}, help = "Текст запроса") String message,
            @ShellOption(value = {"-t", "--temperature"}, help = "Температура (0.0-2.0)", defaultValue = "0.7") Double temperature,
            @ShellOption(value = {"-mt", "--max-tokens"}, help = "Максимум токенов", defaultValue = "2000") Integer maxTokens
    ) {
        log.info("Отправка запроса с параметрами - temperature: {}, maxTokens: {}", temperature, maxTokens);

        var options = OpenAiChatOptions.builder()
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        var prompt = new Prompt(message, options);

        String response = chatModel.call(prompt).getResult().getOutput().getText();

        log.info("Получен ответ от AI");

        return response;
    }

    /**
     * Генерация кода по описанию
     *
     * @param description описание требуемого кода
     * @param language язык программирования
     * @return сгенерированный код
     */
    @ShellMethod(key = "generate-code", value = "Сгенерировать код по описанию")
    public String generateCode(
            @ShellOption(value = {"-d", "--description"}, help = "Описание кода") String description,
            @ShellOption(value = {"-l", "--language"}, help = "Язык программирования", defaultValue = "Java") String language
    ) {
        log.info("Генерация кода на языке {} по описанию: {}", language, description);

        String prompt = String.format(
                "Сгенерируй код на языке %s. Описание: %s. Верни только код без объяснений.",
                language,
                description
        );

        String response = chatModel.call(prompt);

        log.info("Код сгенерирован");

        return response;
    }

    /**
     * Объяснение кода
     *
     * @param code код для объяснения
     * @return объяснение кода
     */
    @ShellMethod(key = "explain-code", value = "Получить объяснение кода")
    public String explainCode(@ShellOption(value = {"-c", "--code"}, help = "Код для объяснения") String code) {
        log.info("Запрос объяснения кода");

        String prompt = String.format(
                "Объясни подробно, что делает этот код:\n\n%s",
                code
        );

        String response = chatModel.call(prompt);

        log.info("Объяснение получено");

        return response;
    }
}