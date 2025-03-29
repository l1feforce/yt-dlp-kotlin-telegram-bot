package org.gusev.logger

interface Logger {
    fun d(tag: String, message: () -> String)
    fun w(tag: String, exception: Exception? = null, message: () -> String)
    fun e(tag: String, exception: Exception? = null, message: () -> String)
}

internal class DefaultLogger: Logger {
    override fun d(tag: String, message: () -> String) {
        println("debug [$tag]: ${message.invoke()}")
    }

    override fun w(tag: String, exception: Exception?, message: () -> String) {
        val exceptionMessage = "\n" + exception?.message.orEmpty()
        System.err.println("warn [$tag] ${message.invoke()}${exceptionMessage}")
    }

    override fun e(tag: String, exception: Exception?, message: () -> String) {
        val exceptionMessage = "\n" + exception?.message.orEmpty()
        System.err.println("error [$tag] ${message.invoke()}${exceptionMessage}")
    }
}