package com.home.bot.common;

import com.home.bot.common.api.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Default implementation of {@link MessageSender} that uses the Telegram Bot API to send messages
 * to a specific chat.
 */
public class MessageSenderImpl implements MessageSender {

    private static final Logger log = LoggerFactory.getLogger(MessageSenderImpl.class);

    /**
     * The Telegram bot sender instance.
     */
    private final AbsSender absSender;

    /**
     * Constructs a new MessageSenderImpl.
     *
     * @param absSender the underlying Telegram sender (e.g., your bot)
     */
    public MessageSenderImpl(AbsSender absSender) {
        this.absSender = absSender;
    }

    /**
     * Sends a plain text message to the specified Telegram chat.
     *
     * @param chatId the ID of the chat to send the message to
     * @param text   the message content
     */
    @Override
    public void send(Long chatId, String text) {
        try {
            // Build and send the message
            var message = SendMessage.builder().chatId(chatId.toString()).text(text).build();

            absSender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", chatId, e.getMessage());
        }
    }
}
