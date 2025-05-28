package com.home.config;

import com.home.config.loader.Config;

public record BotConfig(String token, String username) {

    public static BotConfig load() {
        var token = Config.getRequired("bot.token");
        var username = Config.getRequired("bot.username");
        return new BotConfig(token, username);
    }
}
