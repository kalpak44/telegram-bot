package com.home.model;

/**
 * Represents product details for a payment.
 */
public record ProductInfo(long price, String currency, String name, int quantity) {
}
