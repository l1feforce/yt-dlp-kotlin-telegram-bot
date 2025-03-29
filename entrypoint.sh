#!/bin/bash

# Запускаем telegram-bot-api в фоне
telegram-bot-api --api-id ${API_ID} --api-hash ${API_HASH} --local &

# Запускаем JVM-приложение
exec ytdltgbot