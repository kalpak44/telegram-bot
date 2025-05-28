package com.home;

import com.home.bot.PaymentBot;
import com.home.config.BotConfig;
import com.home.config.StripeConfig;
import com.home.stripe.StripeLinkCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    log.info("Starting Telegram bot application...");
    var botConfig = BotConfig.load();
    var stripeConfig = StripeConfig.load();

    log.info(
        "Config loaded: bot.username={}, stripe.publicKey=****, stripe.secretKey=****, stripe.successUrl={}, stripe.cancelUrl={}",
        botConfig.username(),
        stripeConfig.successUrl(),
        stripeConfig.cancelUrl());

    var stripeLinkCreator = new StripeLinkCreator(stripeConfig);
    var telegramBot = new PaymentBot(botConfig, stripeLinkCreator);

    try {
      var botsApi = new TelegramBotsApi(DefaultBotSession.class);
      botsApi.registerBot(telegramBot);
      log.info("Bot successfully registered.");
    } catch (Exception e) {
      log.error("Failed to register bot: {}", e.getMessage(), e);
    }
  }
}
