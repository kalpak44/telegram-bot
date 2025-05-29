package com.home.bot.command;

import com.home.bot.command.api.CommandHandler;
import com.home.bot.common.api.MessageSender;
import com.home.bot.session.SessionManager;
import com.home.model.ProductInfo;
import com.home.model.State;
import com.home.model.TimedProductInfo;

import java.time.Instant;

/**
 * Handles the /start command, which begins a new payment link session by initializing user session
 * state and prompting for the first input (price and currency).
 */
public class StartCommandHandler implements CommandHandler {

    private final SessionManager sessionManager;
    private final MessageSender messageSender;

    /**
     * Constructs a StartCommandHandler with dependencies.
     *
     * @param sessionManager session manager to store user state
     * @param messageSender  message sender to communicate with the user
     */
    public StartCommandHandler(SessionManager sessionManager, MessageSender messageSender) {
        this.sessionManager = sessionManager;
        this.messageSender = messageSender;
    }

    /**
     * Determines if this handler is responsible for a given command.
     *
     * @param command the user input command
     * @return true if the command is "/start"
     */
    @Override
    public boolean canHandle(String command) {
        return "/start".equalsIgnoreCase(command);
    }

    /**
     * Handles the /start command by setting up the initial state and prompting for price.
     *
     * @param chatId the Telegram chat ID
     */
    @Override
    public void handle(Long chatId) {
        // Set user session state to expect price input
        sessionManager.updateState(chatId, State.WAITING_FOR_PRICE);

        // Initialize an empty product info with a timestamp
        var initialInfo = new ProductInfo(0, null, null, 0);
        sessionManager.updateInfo(chatId, new TimedProductInfo(initialInfo, Instant.now()));

        // Prompt user to enter price and currency
        messageSender.send(
                chatId,
                "Please enter the price and currency in this format: 10.00 USD. "
                        + "Supported currencies: https://docs.stripe.com/currencies");
    }
}
