package ru.gusev.util

internal fun getEnvVariable(key: String): String? = System.getenv(key)