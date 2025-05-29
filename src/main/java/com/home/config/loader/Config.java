package com.home.config.loader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Loads configuration values from environment variables or a properties file.
 * Values are resolved in this order: 1. Environment variable (converted from key) 2.
 * config.properties file on classpath
 */
public class Config {
    private static final Properties props = new Properties();

    static {
        try {
            var stream = Config.class.getClassLoader().getResourceAsStream("config.properties");
            if (stream != null) {
                props.load(stream);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load config.properties", e);
        }
    }

    /**
     * Gets the configuration value for the given key. Environment variable takes precedence over
     * properties file.
     *
     * @param key the config key
     * @return the config value, or null if not found
     */
    public static String get(String key) {
        // Convert key to environment-style format: dots and dashes replaced with underscores, then
        // uppercased
        var envKey = key.replace('.', '_').replace('-', '_').toUpperCase();
        var envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return props.getProperty(key);
    }

    /**
     * Gets a required configuration value.
     *
     * @param key the config key
     * @return the config value
     * @throws IllegalStateException if the key is missing or blank
     */
    public static String getRequired(String key) {
        var value = get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config: " + key);
        }
        return value;
    }
}
