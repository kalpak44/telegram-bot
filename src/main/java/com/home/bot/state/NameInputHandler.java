package com.home.bot.state;

import com.home.bot.common.api.MessageSender;
import com.home.bot.session.SessionManager;
import com.home.bot.state.api.StateInputHandler;
import com.home.model.ProductInfo;
import com.home.model.State;
import com.home.model.TimedProductInfo;

import java.time.Instant;

/**
 * Handles user input when the bot expects the product name.
 * Once the name is received, it transitions the session to wait for quantity.
 */
public class NameInputHandler implements StateInputHandler {

    private final SessionManager sessionManager;
    private final MessageSender messageSender;

    /**
     * Constructs a handler for processing product name input.
     *
     * @param sessionManager manages session state and product info
     * @param messageSender  sends responses to the user
     */
    public NameInputHandler(SessionManager sessionManager, MessageSender messageSender) {
        this.sessionManager = sessionManager;
        this.messageSender = messageSender;
    }

    /**
     * Checks if this handler is responsible for the current state.
     *
     * @param state current session state
     * @return true if state is WAITING_FOR_NAME
     */
    @Override
    public boolean canHandle(State state) {
        return state == State.WAITING_FOR_NAME;
    }

    /**
     * Handles the user input assumed to be a product name.
     * Updates the session with the product name and prompts for quantity.
     *
     * @param chatId Telegram chat ID of the user
     * @param text   product name provided by the user
     */
    @Override
    public void handle(Long chatId, String text) {
        var timedInfo = sessionManager.getInfo(chatId);

        // Defensive check in case of missing session state
        if (timedInfo == null) {
            messageSender.send(chatId, "Something went wrong. Please start again with /start.");
            sessionManager.clear(chatId);
            return;
        }

        // Create updated ProductInfo with name included
        ProductInfo previous = timedInfo.info();
        ProductInfo updated = new ProductInfo(previous.price(), previous.currency(), text, 0);

        // Store the new info and advance session state
        sessionManager.updateInfo(chatId, new TimedProductInfo(updated, Instant.now()));
        sessionManager.updateState(chatId, State.WAITING_FOR_QUANTITY);

        messageSender.send(chatId, "Enter quantity:");
    }
}
