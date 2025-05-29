package com.home.config;

import com.home.config.loader.Config;

/**
 * Configuration for Stripe payments.
 *
 * @param secretKey  the Stripe secret API key
 * @param successUrl the URL to redirect to after a successful payment
 * @param cancelUrl  the URL to redirect to if payment is cancelled
 */
public record StripeConfig(String secretKey, String successUrl, String cancelUrl) {

    /**
     * Loads Stripe configuration from environment variables or config.properties.
     *
     * @return a new StripeConfig instance with loaded values
     */
    public static StripeConfig load() {
        var secretKey = Config.getRequired("payments.stripe.key.secret");
        var successUrl = Config.getRequired("payments.stripe.success-url");
        var cancelUrl = Config.getRequired("payments.stripe.cancel-url");
        return new StripeConfig(secretKey, successUrl, cancelUrl);
    }
}
