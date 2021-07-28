package com.home.service;

import com.stripe.exception.StripeException;
import org.springframework.stereotype.Component;

@Component
public class UrlBuilder {
    private final StripeComponent stripeComponent;

    private String productName;
    private Long quantity;
    private Long price;
    private String currency;

    public UrlBuilder(StripeComponent stripeComponent) {
        this.stripeComponent = stripeComponent;
    }

    public String getProductName() {
        return productName;
    }

    public UrlBuilder setProductName(String productName) {
        this.productName = productName;
        return this;
    }

    public long getQuantity() {
        return quantity;
    }

    public UrlBuilder setQuantity(long quantity) {
        this.quantity = quantity;
        return this;
    }

    public long getPrice() {
        return price;
    }

    public UrlBuilder setPrice(long price) {
        this.price = price;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public UrlBuilder setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public void clean(){
        productName = null;
        quantity = null;
        price = null;
        currency = null;
    }
    public String build(){
        if(productName == null){
            throw new RuntimeException("No product name was found");
        }else if(quantity == null){
            throw new RuntimeException("No quantity name was found");
        }else if(price == null){
            throw new RuntimeException("No price name was found");
        }else if(currency == null){
            throw new RuntimeException("No currency name was found");
        }
        try {
            return stripeComponent.createCheckOutPage(price, currency, quantity, productName);
        } catch (StripeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
