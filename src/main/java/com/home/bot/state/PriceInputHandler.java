package com.home.bot.state;

import com.home.bot.common.api.MessageSender;
import com.home.bot.session.SessionManager;
import com.home.bot.state.api.StateInputHandler;
import com.home.model.ProductInfo;
import com.home.model.State;
import com.home.model.TimedProductInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Handles user input when the bot is expecting the price and currency. Validates the format, stores
 * it in session, and transitions to name input state.
 */
public class PriceInputHandler implements StateInputHandler {

    private static final Logger log = LoggerFactory.getLogger(PriceInputHandler.class);

    private final SessionManager sessionManager;
    private final MessageSender messageSender;

    /**
     * Constructs the handler for price input.
     *
     * @param sessionManager handles session state
     * @param messageSender  sends messages to users
     */
    public PriceInputHandler(SessionManager sessionManager, MessageSender messageSender) {
        this.sessionManager = sessionManager;
        this.messageSender = messageSender;
    }

    /**
     * Returns true if this handler can process the given state.
     *
     * @param state current user state
     * @return true if state is WAITING_FOR_PRICE
     */
    @Override
    public boolean canHandle(State state) {
        return state == State.WAITING_FOR_PRICE;
    }

    /**
     * Processes input assumed to be price and currency (e.g., "10.00 USD"). On success, updates
     * session and asks for product name. On error, notifies the user of format issues.
     *
     * @param chatId user's chat ID
     * @param text   user input text
     */
    @Override
    public void handle(Long chatId, String text) {
        var parts = text.trim().split(" ");
        if (parts.length != 2) {
            messageSender.send(
                    chatId,
                    "Invalid format. Please enter the price and currency in this format: 10.00 USD. Supported currencies: https://docs.stripe.com/currencies");
            return;
        }

        try {
            var price = Double.parseDouble(parts[0]);
            if (price <= 0) {
                messageSender.send(chatId, "Price must be a positive number.");
                return;
            }

            var priceInCents = (long) (price * 100);
            var currency = parts[1].toLowerCase();

            // Update session with price and currency, reset other fields
            var info = new ProductInfo(priceInCents, currency, null, 0);
            sessionManager.updateInfo(chatId, new TimedProductInfo(info, Instant.now()));
            sessionManager.updateState(chatId, State.WAITING_FOR_NAME);

            messageSender.send(chatId, "Enter product name:");
        } catch (NumberFormatException e) {
            log.warn("Invalid price input '{}': {}", text, e.getMessage());
            messageSender.send(chatId, "Invalid price format.");
        }
    }
}
