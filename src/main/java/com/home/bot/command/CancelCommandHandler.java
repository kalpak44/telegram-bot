package com.home.bot.command;

import com.home.bot.command.api.CommandHandler;
import com.home.bot.common.api.MessageSender;
import com.home.bot.session.SessionManager;

/**
 * Handles the /cancel command, which resets the user's session state.
 */
public class CancelCommandHandler implements CommandHandler {

    private final SessionManager sessionManager;
    private final MessageSender messageSender;

    /**
     * Constructs a CancelCommandHandler with the required dependencies.
     *
     * @param sessionManager the session manager to clear session data
     * @param messageSender  the message sender used to notify the user
     */
    public CancelCommandHandler(SessionManager sessionManager, MessageSender messageSender) {
        this.sessionManager = sessionManager;
        this.messageSender = messageSender;
    }

    /**
     * Checks if this handler is responsible for the given command.
     *
     * @param command the command string
     * @return true if the command is "/cancel", false otherwise
     */
    @Override
    public boolean canHandle(String command) {
        return "/cancel".equalsIgnoreCase(command);
    }

    /**
     * Handles the /cancel command by clearing the user session and sending a confirmation message.
     *
     * @param chatId the ID of the chat where the command was issued
     */
    @Override
    public void handle(Long chatId) {
        sessionManager.clear(chatId);
        messageSender.send(chatId, "Cancelled.");
    }
}
