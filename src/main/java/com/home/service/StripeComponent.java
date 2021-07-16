package com.home.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StripeComponent {
    private static final Logger logger = LoggerFactory.getLogger(StripeComponent.class);
    @Value("${payments.stripe.key.secret}")
    private String API_SECET_KEY;
    @Value("${payments.stripe.success-url}")
    private String successUrl;
    @Value("${payments.stripe.cancel-url}")
    private String cancelUrl;

    public String createCheckOutPage(Long amount, String currency, Long quantity, String productName) throws StripeException {
        Stripe.apiKey = API_SECET_KEY;
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setAmount(amount)
                                        .setName(productName)
                                        .setQuantity(quantity)
                                        .setCurrency(currency)
                                        .build())
                        .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                        .setSuccessUrl(successUrl)
                        .setCancelUrl(cancelUrl)
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .build();

        return  Session.create(params).getUrl();
    }
}
