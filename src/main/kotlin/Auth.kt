package ru.gusev

import com.github.kotlintelegrambot.extensions.filters.Filter

internal fun authFilter(authorizedUsers: Set<Long>) = Filter.Custom {
    this.from?.id in authorizedUsers
}