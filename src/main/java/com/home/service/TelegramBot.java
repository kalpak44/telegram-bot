package com.home.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final UrlBuilder urlBuilder;
    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    public TelegramBot(UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }


    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    private String previousCommand;

    // TODO: 30.07.2021 rework me
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            SendMessage response = new SendMessage();
            Long chatId = message.getChatId();
            response.setChatId(String.valueOf(chatId));
            String text = message.getText();
            try {
                if (text != null && (text.contains("нов") || text.contains("new") || text.contains("nov"))) {
                    urlBuilder.clean();
                    response.setText("името на продукта");
                    previousCommand = "new";
                } else if (previousCommand != null && previousCommand.equals("new") && text != null) {
                    if (text.isEmpty()) {
                        response.setText("Грешно име");
                    } else {
                        urlBuilder.setProductName(text);
                        response.setText("Добре. Продукта \"" + text + "\"\n" +
                                "Колко ще струва той?");
                        previousCommand = "name_created";
                    }
                } else if (previousCommand != null && previousCommand.equals("name_created") && text != null) {
                    try {
                        long price;
                        if (!text.contains(".")) {
                            price = Long.parseLong(text) * 100;
                        } else {
                            price = new BigDecimal(text).multiply(new BigDecimal("100")).setScale(0, RoundingMode.UP).longValue();
                        }
                        urlBuilder.setPrice(price);
                        previousCommand = "price_created";
                        //https://stripe.com/docs/currencies#presentment-currencies
                        response.setText("Желаема валута?");
                        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                        List<KeyboardRow> keyboard = new ArrayList<>();
                        KeyboardRow row = new KeyboardRow();
                        row.add("USD");
                        row.add("EUR");
                        row.add("GBP");
                        keyboard.add(row);
                        keyboardMarkup.setKeyboard(keyboard);
                        response.setReplyMarkup(keyboardMarkup);
                    } catch (Exception e) {
                        response.setText("Цената ви трябва да е число по голямо от 0");
                    }
                } else if (previousCommand != null && previousCommand.equals("price_created") && text != null) {
                    if (text.isEmpty()) {
                        response.setText("Вкарай някаквавалута");
                    } else {
                        urlBuilder.setCurrency(text);
                        response.setText("оки...." +
                                "брой?");
                        previousCommand = "currency_created";
                    }
                } else if (previousCommand != null && previousCommand.equals("currency_created") && text != null) {
                    try {
                        long q = Long.parseLong(text);
                        urlBuilder.setQuantity(q);
                        previousCommand = "quantity_created";
                        response.setText("Готови сме.\n");
                    } catch (Exception e) {
                        response.setText("Бройката ви трябва да е число. Опитай отново");
                    }
                } else {
                    response.setText("Не разбрах какво имате предвид. Напиши \"нов продукт\" да започнем");
                    previousCommand = null;
                }

                if (previousCommand != null && previousCommand.equals("quantity_created")) {
                    try {
                        response.setText(urlBuilder.build());
                    } catch (Exception e) {
                        response.setText(e.getMessage());
                    }
                    previousCommand = null;
                }
                execute(response);
                logger.info("Sent message \"{}\" to {}", text, chatId);
            } catch (Exception e) {
                response.setText(e.getMessage());
                try {
                    execute(response);
                } catch (TelegramApiException ex) {
                    logger.error("Failed to send message \"{}\" to {} due to error: {}", text, chatId, ex.getMessage());
                }
            }
        }
    }
}
