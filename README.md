# Spring AI Agent Tutorial

Проект для демонстрации работы Spring AI с использованием LLM и вызовом через Spring Shell.

## Пререквизиты для запуска

- прописать DEEPSEEK_API_KEY
- поднять локальную embedding модель для RAG:
```yaml
services:
  ollama:
    image: ollama/ollama
    container_name: ollama
    ports:
      - "11432:11434"
    volumes:
      - ./ollama:/root/.ollama
    entrypoint: >
      /bin/sh -c "
        ollama serve &
        sleep 2 &&
        ollama pull nomic-embed-text &&
        wait
      "
    restart: unless-stopped
```

## Настройка

```bash
export =your-api-key-here
```

### 2. Сборка проекта

```bash
./gradlew clean build
```

### 3. Запуск приложения

```bash
./gradlew bootRun
```

После запуска откроется интерактивная Shell консоль.

## Доступные команды

### 1. ask - Простой запрос к AI

Отправить простой запрос к AI модели:

```bash
ask --message "Что такое Spring Framework?"
```

Короткая форма:

```bash
ask -m "Что такое Spring Framework?"
```

### 2. ask-custom - Запрос с кастомными параметрами

Отправить запрос с настройкой параметров генерации:

```bash
ask-custom --message "Напиши стихотворение про Java" --temperature 1.2 --max-tokens 500
```

Короткая форма:

```bash
ask-custom -m "Напиши стихотворение про Java" -t 1.2 -mt 500
```

Параметры:
- `temperature` (0.0-2.0) - креативность ответа (по умолчанию 0.7)
- `max-tokens` - максимальное количество токенов в ответе (по умолчанию 2000)

### 3. generate-code - Генерация кода

Сгенерировать код по описанию:

```bash
generate-code --description "Метод для сортировки списка строк" --language "Java"
```

Короткая форма:

```bash
generate-code -d "Метод для сортировки списка строк" -l "Java"
```

### 4. explain-code - Объяснение кода

Получить объяснение кода:

```bash
explain-code --code "public static void main(String[] args) { System.out.println(\"Hello\"); }"
```

Короткая форма:

```bash
explain-code -c "public static void main(String[] args) { System.out.println(\"Hello\"); }"
```

### Встроенные команды Shell

- `help` - показать список всех доступных команд
- `clear` - очистить экран
- `exit` или `quit` - выход из приложения

## Примеры использования

### Пример 1: Простой вопрос

```bash
shell:>ask -m "Объясни разницу между ArrayList и LinkedList в Java"
```

### Пример 2: Генерация кода

```bash
shell:>generate-code -d "Метод для проверки, является ли строка палиндромом" -l "Java"
```

### Пример 3: Креативная генерация с высокой температурой

```bash
shell:>ask-custom -m "Придумай название для стартапа в области AI" -t 1.5 -mt 100
```

