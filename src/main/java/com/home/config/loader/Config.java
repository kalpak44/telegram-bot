package com.home.config.loader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

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

    public static String getRequired(String key) {
        var value = get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config: " + key);
        }
        return value;
    }
}
