package com.home.bot;

import com.home.config.BotConfig;
import com.home.model.ProductInfo;
import com.home.model.State;
import com.home.stripe.StripeLinkCreator;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class PaymentBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(PaymentBot.class);

    private final BotConfig botConfig;
    private final StripeLinkCreator stripeLinkCreator;

    private final Map<Long, State> userState = new HashMap<>();
    private final Map<Long, ProductInfo> userInput = new HashMap<>();

    public PaymentBot(BotConfig botConfig, StripeLinkCreator stripeLinkCreator) {
        super(botConfig.token());
        this.botConfig = botConfig;
        this.stripeLinkCreator = stripeLinkCreator;
    }

    @Override
    public String getBotUsername() {
        return botConfig.username();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        var message = update.getMessage();
        var text = message.getText().trim();
        var chatId = message.getChatId();

        if ("/help".equalsIgnoreCase(text)) {
            sendMessage(chatId, """
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
            userInput.put(chatId, new ProductInfo(0, null, null, 0));
            sendMessage(chatId, "Please enter the price and currency in this format: 10.00 USD. Supported currencies: https://docs.stripe.com/currencies");
            return;
        }

        var state = userState.get(chatId);
        var info = userInput.get(chatId);

        if (state == null || info == null) {
            sendMessage(chatId, "Send /start to begin.");
            return;
        }

        switch (state) {
            case WAITING_FOR_PRICE -> {
                var parts = text.split(" ");
                if (parts.length != 2) {
                    sendMessage(chatId, "Invalid format. Please enter the price and currency in this format: 10.00 USD. Supported currencies: https://docs.stripe.com/currencies");
                    return;
                }
                try {
                    info =
                            new ProductInfo(
                                    (long) (Double.parseDouble(parts[0]) * 100), parts[1].toLowerCase(), null, 0);
                    userInput.put(chatId, info);
                    userState.put(chatId, State.WAITING_FOR_NAME);
                    sendMessage(chatId, "Enter product name:");
                } catch (NumberFormatException e) {
                    log.warn("Invalid price input '{}': {}", text, e.getMessage());
                    sendMessage(chatId, "Invalid price format.");
                }
            }
            case WAITING_FOR_NAME -> {
                info = new ProductInfo(info.price(), info.currency(), text, 0);
                userInput.put(chatId, info);
                userState.put(chatId, State.WAITING_FOR_QUANTITY);
                sendMessage(chatId, "Enter quantity:");
            }
            case WAITING_FOR_QUANTITY -> {
                try {
                    var quantity = Integer.parseInt(text);
                    info = new ProductInfo(info.price(), info.currency(), info.name(), quantity);
                    userInput.put(chatId, info);

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

    private void sendMessage(Long chatId, String text) {
        try {
            execute(SendMessage.builder().chatId(chatId.toString()).text(text).build());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", chatId, e.getMessage());
        }
    }
}
