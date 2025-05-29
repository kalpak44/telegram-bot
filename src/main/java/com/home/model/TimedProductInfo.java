package com.home.model;

import java.time.Instant;

/**
 * Stores product info together with a timestamp.
 */
public record TimedProductInfo(ProductInfo info, Instant timestamp) {
}
