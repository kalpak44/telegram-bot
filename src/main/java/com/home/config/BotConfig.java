package com.home.config;

import com.home.config.loader.Config;

/**
 * Configuration for the Telegram bot.
 *
 * @param token    the bot token used to authenticate with Telegram
 * @param username the bot's username
 */
public record BotConfig(String token, String username) {

    /**
     * Loads the bot configuration from environment variables or config.properties.
     *
     * @return a new BotConfig instance with loaded values
     */
    public static BotConfig load() {
        var token = Config.getRequired("bot.token");
        var username = Config.getRequired("bot.username");
        return new BotConfig(token, username);
    }
}
