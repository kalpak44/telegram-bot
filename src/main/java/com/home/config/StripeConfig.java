package com.home.config;

import com.home.config.loader.Config;

public record StripeConfig(
        String publicKey, String secretKey, String successUrl, String cancelUrl) {

    public static StripeConfig load() {
        var publicKey = Config.getRequired("payments.stripe.key.public");
        var secretKey = Config.getRequired("payments.stripe.key.secret");
        var successUrl = Config.getRequired("payments.stripe.success-url");
        var cancelUrl = Config.getRequired("payments.stripe.cancel-url");

        return new StripeConfig(publicKey, secretKey, successUrl, cancelUrl);
    }
}
