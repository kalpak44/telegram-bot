package com.home;

import com.home.bot.PaymentBot;
import com.home.bot.command.CancelCommandHandler;
import com.home.bot.command.HelpCommandHandler;
import com.home.bot.command.StartCommandHandler;
import com.home.bot.command.StatusCommandHandler;
import com.home.bot.common.MessageSenderImpl;
import com.home.bot.session.SessionManager;
import com.home.bot.state.NameInputHandler;
import com.home.bot.state.PriceInputHandler;
import com.home.bot.state.QuantityInputHandler;
import com.home.config.BotConfig;
import com.home.config.StripeConfig;
import com.home.stripe.StripeLinkCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

/**
 * Entry point for the Telegram PaymentBot application.
 * Loads configuration, builds the bot instance, and registers it with the Telegram API.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * Starts the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        log.info("Starting Telegram bot application...");

        // 1. Load configuration files for bot and Stripe
        var botConfig = BotConfig.load();
        var stripeConfig = StripeConfig.load();

        log.info("Config loaded: bot.username={}, stripe.successUrl={}, stripe.cancelUrl={}",
                botConfig.username(), stripeConfig.successUrl(), stripeConfig.cancelUrl());

        // 2. Create shared services
        var stripeLinkCreator = new StripeLinkCreator(stripeConfig);
        var telegramBot = buildPaymentBot(stripeLinkCreator, botConfig);

        // 3. Register bot with Telegram API
        try {
            var botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBot);
            log.info("Bot successfully registered.");
        } catch (Exception e) {
            log.error("Failed to register bot: {}", e.getMessage(), e);
        }
    }

    /**
     * Builds and wires the PaymentBot with handlers and dependencies.
     *
     * @param stripeLinkCreator helper to create Stripe payment links
     * @param botConfig         configuration for the bot (token, username)
     * @return fully configured PaymentBot
     */
    private static PaymentBot buildPaymentBot(StripeLinkCreator stripeLinkCreator, BotConfig botConfig) {
        // Instantiate the bot shell
        var paymentBot = new PaymentBot(botConfig);

        // Set up message sender (used by handlers)
        var messageSender = new MessageSenderImpl(paymentBot);

        // Set up session manager (manages user state)
        var sessionManager = new SessionManager();

        // Register command handlers (/start, /help, /cancel, /status)
        var commandHandlers = List.of(
                new HelpCommandHandler(messageSender),
                new StartCommandHandler(sessionManager, messageSender),
                new CancelCommandHandler(sessionManager, messageSender),
                new StatusCommandHandler(sessionManager, messageSender)
        );

        // Register state input handlers (price, name, quantity)
        var stateHandlers = List.of(
                new PriceInputHandler(sessionManager, messageSender),
                new NameInputHandler(sessionManager, messageSender),
                new QuantityInputHandler(sessionManager, stripeLinkCreator, messageSender)
        );

        // Inject dependencies into the bot instance
        return paymentBot
                .withMessageSender(messageSender)
                .withSessionManager(sessionManager)
                .withCommandHandlers(commandHandlers)
                .withStateHandlers(stateHandlers);
    }
}
