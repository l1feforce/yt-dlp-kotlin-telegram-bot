package org.gusev.domain

import java.net.URI

internal class ValidateUrlUseCase {
    fun isValidUrl(text: String): Boolean {
        return try {
            URI(text).toURL()
            true
        } catch (e: Exception) {
            false
        }
    }
}