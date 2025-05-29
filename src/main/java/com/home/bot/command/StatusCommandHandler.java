package com.home.bot.command;

import com.home.bot.command.api.CommandHandler;
import com.home.bot.common.api.MessageSender;
import com.home.bot.session.SessionManager;

/**
 * Handles the /status command to display the user's current input progress.
 */
public class StatusCommandHandler implements CommandHandler {

    private final SessionManager sessionManager;
    private final MessageSender messageSender;

    /**
     * Constructs a new StatusCommandHandler.
     *
     * @param sessionManager the session manager to retrieve user state and input
     * @param messageSender  the message sender used to reply to the user
     */
    public StatusCommandHandler(SessionManager sessionManager, MessageSender messageSender) {
        this.sessionManager = sessionManager;
        this.messageSender = messageSender;
    }

    /**
     * Checks if this handler can process the given command.
     *
     * @param command the command text
     * @return true if this is a /status command
     */
    @Override
    public boolean canHandle(String command) {
        return "/status".equalsIgnoreCase(command);
    }

    /**
     * Handles the /status command by sending the current session input state back to the user.
     *
     * @param chatId the Telegram chat ID
     */
    @Override
    public void handle(Long chatId) {
        var timedInfo = sessionManager.getInfo(chatId);
        var state = sessionManager.getState(chatId);

        if (timedInfo == null || state == null || sessionManager.isExpired(chatId)) {
            messageSender.send(chatId, "No active session. Send /start to begin.");
            sessionManager.clear(chatId);
            return;
        }

        var info = timedInfo.info();

        messageSender.send(
                chatId,
                String.format(
                        """
                                📝 *Current Status*
                                State: `%s`
                                
                                Price: %s
                                Currency: %s
                                Product: %s
                                Quantity: %s
                                """,
                        state,
                        info.price() > 0 ? (info.price() / 100.0) : "(not set)",
                        info.currency() != null ? info.currency() : "(not set)",
                        info.name() != null ? info.name() : "(not set)",
                        info.quantity() > 0 ? info.quantity() : "(not set)"));
    }
}
