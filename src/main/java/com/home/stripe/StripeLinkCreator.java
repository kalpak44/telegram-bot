package com.home.stripe;

import com.home.config.StripeConfig;
import com.home.model.ProductInfo;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

/**
 * Creates Stripe payment links based on product information.
 */
public class StripeLinkCreator {
    private final StripeConfig stripeConfig;

    /**
     * Initializes Stripe with the provided configuration.
     *
     * @param stripeConfig contains API key and redirect URLs
     */
    public StripeLinkCreator(StripeConfig stripeConfig) {
        this.stripeConfig = stripeConfig;
        Stripe.apiKey = stripeConfig.secretKey(); // Init once
    }

    /**
     * Creates a Stripe Checkout Session and returns the payment link.
     *
     * @param info the product information to include in the payment
     * @return the URL to the Stripe Checkout session
     * @throws StripeException if Stripe API call fails
     */
    public String createStripeLink(ProductInfo info) throws StripeException {
        var productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder().setName(info.name()).build();

        var priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(info.currency())
                        .setUnitAmount(info.price())
                        .setProductData(productData)
                        .build();

        var lineItem =
                SessionCreateParams.LineItem.builder()
                        .setQuantity((long) info.quantity())
                        .setPriceData(priceData)
                        .build();

        var params =
                SessionCreateParams.builder()
                        .addLineItem(lineItem)
                        .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(stripeConfig.successUrl())
                        .setCancelUrl(stripeConfig.cancelUrl())
                        .build();

        var session = Session.create(params);
        return session.getUrl();
    }
}
