package com.home.bot.command.api;

/**
 * Represents a handler for a specific bot command (e.g., /start, /help). Implementations of this
 * interface define how to check and respond to a command.
 */
public interface CommandHandler {

    /**
     * Determines whether this handler can process the given command text.
     *
     * @param command the command input string (e.g. "/start")
     * @return true if the handler can process the command, false otherwise
     */
    boolean canHandle(String command);

    /**
     * Executes the command logic for the specified chat.
     *
     * @param chatId the unique identifier of the chat where the command was invoked
     */
    void handle(Long chatId);
}
