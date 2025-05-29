package com.home.bot.common.api;

/**
 * Functional interface for sending plain text messages to a Telegram chat. Implementations handle
 * the actual delivery mechanism using Telegram Bot API.
 */
@FunctionalInterface
public interface MessageSender {

    /**
     * Sends a text message to the specified Telegram chat.
     *
     * @param chatId the unique identifier of the target chat
     * @param text   the message content to send
     */
    void send(Long chatId, String text);
}
