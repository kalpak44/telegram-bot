package com.home;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BotApplication {
    private static final Logger logger = LoggerFactory.getLogger(BotApplication.class);

    public static void main(String[] args) {
        logger.info("started");
        SpringApplication.run(BotApplication.class, args);
    }
}
