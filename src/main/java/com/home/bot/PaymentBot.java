package com.home.bot;

import com.home.bot.command.api.CommandHandler;
import com.home.bot.common.api.MessageSender;
import com.home.bot.session.SessionManager;
import com.home.bot.state.api.StateInputHandler;
import com.home.config.BotConfig;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

/**
 * PaymentBot is a Telegram bot that guides users through payment link creation via Stripe. It
 * handles both command messages (e.g. /start, /help) and stateful user input.
 */
public class PaymentBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    private SessionManager sessionManager;
    private List<CommandHandler> commandHandlers;
    private List<StateInputHandler> stateHandlers;
    private MessageSender messageSender;

    /**
     * Constructs a basic PaymentBot instance. Handlers and dependencies must be injected via
     * `withX()` methods.
     *
     * @param botConfig the configuration used to initialize the bot
     */
    public PaymentBot(BotConfig botConfig) {
        super(botConfig.token());
        this.botConfig = botConfig;
    }

    @Override
    public String getBotUsername() {
        return botConfig.username();
    }

    /**
     * Called by Telegram when an update (message) is received. Delegates to either a command handler
     * or a state handler, or sends fallback if unmatched.
     *
     * @param update the Telegram update
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        var message = update.getMessage();
        var chatId = message.getChatId();
        var text = message.getText().trim();

        // 1. Check if the message is a command like /start or /help
        for (var handler : commandHandlers) {
            if (handler.canHandle(text)) {
                handler.handle(chatId);
                return;
            }
        }

        // 2. Check session validity
        var state = sessionManager.getState(chatId);
        if (sessionManager.isExpired(chatId)) {
            sessionManager.clear(chatId);
            messageSender.send(chatId, "Your session has expired. Please send /start to begin again.");
            return;
        }

        // 3. Delegate input based on user's current state
        for (var handler : stateHandlers) {
            if (handler.canHandle(state)) {
                handler.handle(chatId, text);
                return;
            }
        }

        // 4. If input doesn't match anything, show fallback message
        messageSender.send(chatId, "Unexpected input. Please use /help.");
    }

    /**
     * Injects the message sender used to send responses to Telegram users.
     */
    public PaymentBot withMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
        return this;
    }

    /**
     * Injects the session manager that tracks user state and input.
     */
    public PaymentBot withSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        return this;
    }

    /**
     * Injects the list of command handlers that respond to commands like /start.
     */
    public PaymentBot withCommandHandlers(List<CommandHandler> commandHandlers) {
        this.commandHandlers = commandHandlers;
        return this;
    }

    /**
     * Injects the list of state input handlers that process user input based on state.
     */
    public PaymentBot withStateHandlers(List<StateInputHandler> stateHandlers) {
        this.stateHandlers = stateHandlers;
        return this;
    }
}
