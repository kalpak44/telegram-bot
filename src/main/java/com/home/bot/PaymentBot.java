package com.home.bot;

import com.home.config.BotConfig;
import com.home.model.ProductInfo;
import com.home.model.State;
import com.home.model.TimedProductInfo;
import com.home.stripe.StripeLinkCreator;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PaymentBot is a Telegram bot that helps users generate Stripe payment links by guiding them
 * through a short flow of entering product details.
 *
 * <p>Commands: - /start: starts the process - /cancel: cancels the current session - /help: shows
 * usage instructions
 */
public class PaymentBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(PaymentBot.class);
    private static final Duration SESSION_TTL = Duration.ofHours(1);

    /**
     * Configuration details such as bot token and username.
     */
    private final BotConfig botConfig;

    /**
     * Stripe utility used to generate payment links.
     */
    private final StripeLinkCreator stripeLinkCreator;

    /**
     * Keeps track of current state for each user session.
     */
    private final Map<Long, State> userState = new ConcurrentHashMap<>();

    /**
     * Stores user input data along with timestamp for expiration handling.
     */
    private final Map<Long, TimedProductInfo> userInput = new ConcurrentHashMap<>();

    /**
     * Constructs a new PaymentBot instance.
     *
     * @param botConfig         the configuration for the bot (token, username, etc.)
     * @param stripeLinkCreator helper for creating Stripe payment links
     */
    public PaymentBot(BotConfig botConfig, StripeLinkCreator stripeLinkCreator) {
        super(botConfig.token());
        this.botConfig = botConfig;
        this.stripeLinkCreator = stripeLinkCreator;
    }

    /**
     * Returns the bot's username used on Telegram.
     *
     * @return bot username
     */
    @Override
    public String getBotUsername() {
        return botConfig.username();
    }

    /**
     * Processes incoming updates from Telegram.
     *
     * @param update incoming message or command
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        var message = update.getMessage();
        var text = message.getText().trim();
        var chatId = message.getChatId();

        if ("/help".equalsIgnoreCase(text)) {
            sendMessage(
                    chatId,
                    """
                            🤖 *PaymentBot Help*
                            
                            Available commands:
                            /start - Begin payment link creation
                            /cancel - Cancel current operation
                            /help - Show this help message
                            
                            Input format guidance:
                            - After /start, provide the price and currency (e.g. `10.00 USD`)
                              ↳ Supported currencies: https://docs.stripe.com/currencies
                            - Then, enter the product name
                            - Finally, provide the quantity
                            
                            The bot will return a Stripe payment link based on your input.
                            """);
            return;
        }

        if ("/cancel".equalsIgnoreCase(text)) {
            userState.remove(chatId);
            userInput.remove(chatId);
            sendMessage(chatId, "Cancelled.");
            return;
        }

        if ("/start".equalsIgnoreCase(text)) {
            userState.put(chatId, State.WAITING_FOR_PRICE);
            userInput.put(chatId, new TimedProductInfo(new ProductInfo(0, null, null, 0), Instant.now()));
            sendMessage(
                    chatId,
                    "Please enter the price and currency in this format: 10.00 USD. Supported currencies: https://docs.stripe.com/currencies");
            return;
        }

        var state = userState.get(chatId);
        var timedInfo = userInput.get(chatId);

        if (state == null
                || timedInfo == null
                || Duration.between(timedInfo.timestamp(), Instant.now()).compareTo(SESSION_TTL) > 0) {
            userState.remove(chatId);
            userInput.remove(chatId);
            sendMessage(chatId, "Your session has expired. Please send /start to begin again.");
            return;
        }

        var info = timedInfo.info();

        switch (state) {
            case WAITING_FOR_PRICE -> {
                var parts = text.split(" ");
                if (parts.length != 2) {
                    sendMessage(
                            chatId,
                            "Invalid format. Please enter the price and currency in this format: 10.00 USD. Supported currencies: https://docs.stripe.com/currencies");
                    return;
                }
                try {
                    var price = (long) (Double.parseDouble(parts[0]) * 100);
                    var currency = parts[1].toLowerCase();
                    info = new ProductInfo(price, currency, null, 0);
                    userInput.put(chatId, new TimedProductInfo(info, Instant.now()));
                    userState.put(chatId, State.WAITING_FOR_NAME);
                    sendMessage(chatId, "Enter product name:");
                } catch (NumberFormatException e) {
                    log.warn("Invalid price input '{}': {}", text, e.getMessage());
                    sendMessage(chatId, "Invalid price format.");
                }
            }
            case WAITING_FOR_NAME -> {
                info = new ProductInfo(info.price(), info.currency(), text, 0);
                userInput.put(chatId, new TimedProductInfo(info, Instant.now()));
                userState.put(chatId, State.WAITING_FOR_QUANTITY);
                sendMessage(chatId, "Enter quantity:");
            }
            case WAITING_FOR_QUANTITY -> {
                try {
                    int quantity = Integer.parseInt(text);
                    info = new ProductInfo(info.price(), info.currency(), info.name(), quantity);
                    userInput.put(chatId, new TimedProductInfo(info, Instant.now()));

                    var url = stripeLinkCreator.createStripeLink(info);
                    sendMessage(chatId, "Here is your payment link:\n" + url);

                    userState.remove(chatId);
                    userInput.remove(chatId);
                } catch (NumberFormatException e) {
                    log.warn("Invalid quantity input '{}': {}", text, e.getMessage());
                    sendMessage(chatId, "Invalid quantity format.");
                } catch (StripeException e) {
                    log.error("Stripe error: {}", e.getMessage());
                    sendMessage(chatId, "Failed to generate Stripe payment link. Please try again later.");
                }
            }
        }
    }

    /**
     * Sends a text message to a specific Telegram chat.
     *
     * @param chatId the ID of the chat
     * @param text   the message content to send
     */
    private void sendMessage(Long chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(chatId.toString()).text(text).build());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", chatId, e.getMessage());
        }
    }
}
