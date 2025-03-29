# Этап 1: Сборка telegram-bot-api
FROM ubuntu:22.04 AS tg-bot-api-builder

# Установка зависимостей
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    make git zlib1g-dev libssl-dev gperf cmake \
    clang-18 libc++-18-dev libc++abi-18-dev ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Клонирование и сборка
RUN git clone --recursive https://github.com/tdlib/telegram-bot-api.git /tg-bot-api
WORKDIR /tg-bot-api
RUN rm -rf build && \
    mkdir build && \
    cd build && \
    CXXFLAGS="-stdlib=libc++" \
    CC=/usr/bin/clang-18 \
    CXX=/usr/bin/clang++-18 \
    cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX:PATH=.. .. && \
    cmake --build . --target install

# Этап 2: Сборка Gradle-приложения
FROM gradle:8.7-jdk21-jammy AS gradle-builder

# Клонируем репозиторий с приложением
RUN git clone https://github.com/l1feforce/yt-dlp-kotlin-telegram-bot.git /app
WORKDIR /app

# Собираем приложение
RUN ./gradlew assemble

# Этап 3: Финальный образ
FROM eclipse-temurin:21-jre

# Установка runtime-зависимостей
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    libc++1-18 libc++abi1-18 ffmpeg curl jq && \
    rm -rf /var/lib/apt/lists/*

# Скачиваем последнюю версию yt-dlp
RUN LATEST_YTDLP_URL=$(curl -s https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest | \
    jq -r '.assets[] | select(.name | test("yt-dlp_linux")) | .browser_download_url') && \
    curl -L $LATEST_YTDLP_URL -o /usr/local/bin/yt-dlp && \
    chmod a+rx /usr/local/bin/yt-dlp

# Копируем артефакты
COPY --from=tg-bot-api-builder /tg-bot-api/bin/telegram-bot-api /usr/local/bin/
COPY --from=gradle-builder /app/build/distributions/ytdltgbot-*.tar /app/

# Распаковываем дистрибутив и настраиваем пути
RUN tar -xf /app/ytdltgbot-*.tar -C /app && \
    ln -s /app/ytdltgbot-*/bin/ytdltgbot /usr/local/bin/ytdltgbot && \
    chmod +x /usr/local/bin/ytdltgbot

# Скрипт для запуска
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]