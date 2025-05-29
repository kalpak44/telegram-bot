package com.home.bot.state;

import com.home.bot.common.api.MessageSender;
import com.home.bot.session.SessionManager;
import com.home.bot.state.api.StateInputHandler;
import com.home.model.ProductInfo;
import com.home.model.State;
import com.home.stripe.StripeLinkCreator;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles input when the bot is waiting for the user to enter the product quantity. After receiving
 * a valid quantity, it creates a Stripe payment link and ends the session.
 */
public class QuantityInputHandler implements StateInputHandler {

    private static final Logger log = LoggerFactory.getLogger(QuantityInputHandler.class);

    private final SessionManager sessionManager;
    private final StripeLinkCreator stripeLinkCreator;
    private final MessageSender messageSender;

    /**
     * Constructs a handler for the quantity input step.
     *
     * @param sessionManager    session state tracker
     * @param stripeLinkCreator Stripe payment link generator
     * @param messageSender     message sender to reply to the user
     */
    public QuantityInputHandler(
            SessionManager sessionManager,
            StripeLinkCreator stripeLinkCreator,
            MessageSender messageSender) {
        this.sessionManager = sessionManager;
        this.stripeLinkCreator = stripeLinkCreator;
        this.messageSender = messageSender;
    }

    @Override
    public boolean canHandle(State state) {
        return state == State.WAITING_FOR_QUANTITY;
    }

    @Override
    public void handle(Long chatId, String text) {
        var timedInfo = sessionManager.getInfo(chatId);
        if (timedInfo == null) {
            messageSender.send(chatId, "Something went wrong. Please start again with /start.");
            sessionManager.clear(chatId);
            return;
        }

        var old = timedInfo.info();

        try {
            // Parse quantity input
            var quantity = Integer.parseInt(text);
            var completed = new ProductInfo(old.price(), old.currency(), old.name(), quantity);

            // Create Stripe link and send to user
            var url = stripeLinkCreator.createStripeLink(completed);
            messageSender.send(chatId, "Here is your payment link:\n" + url);

            // End session after successful completion
            sessionManager.clear(chatId);
        } catch (NumberFormatException e) {
            log.warn("Invalid quantity input '{}': {}", text, e.getMessage());
            messageSender.send(chatId, "Invalid quantity format.");
        } catch (StripeException e) {
            log.error("Stripe error: {}", e.getMessage());
            messageSender.send(chatId, "Failed to generate Stripe payment link. Please try again later.");
        }
    }
}
