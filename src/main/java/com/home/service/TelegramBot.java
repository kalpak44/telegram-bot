package com.home.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final UrlBuilder urlBuilder;
    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    private List<String> currencies = Arrays.asList("USD", "AED", "ALL", "AMD", "ANG", "AUD", "AWG", "AZN", "BAM", "BBD", "BDT", "BGN", "BIF", "BMD", "BND", "BSD", "BWP", "BZD", "CAD", "CDF", "CHF", "CNY", "CZK", "DKK", "DOP", "DZD", "EGP", "ETB", "EUR", "FJD", "GBP", "GEL", "GIP", "GMD", "GYD", "HKD", "HRK", "HTG", "HUF", "IDR", "ILS", "ISK", "JMD", "JPY", "KES", "KGS", "KHR", "KMF", "KRW", "KYD", "KZT", "LBP", "LKR", "LRD", "LSL", "MAD", "MDL", "MGA", "MKD", "MMK", "MNT", "MOP", "MRO", "MVR", "MWK", "MXN", "MYR", "MZN", "NAD", "NGN", "NOK", "NPR", "NZD", "PGK", "PHP", "PKR", "PLN", "QAR", "RON", "RSD", "RUB", "RWF", "SAR", "SBD", "SCR", "SEK", "SGD", "SLL", "SOS", "SZL", "THB", "TJS", "TOP", "TRY", "TTD", "TWD", "TZS", "UAH", "UGX", "UZS", "VND", "VUV", "WST", "XAF", "XCD", "YER", "ZAR", "ZMW");

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
                                "Цена?");
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
                        response.setText("валута?");
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
                    if (text.isEmpty() || !currencies.contains(text)) {
                        response.setText("(@_@) Не въведена валута. Опитай отново. Разрешени са: " + currencies.toString());
                    } else {
                        urlBuilder.setCurrency(text);
                        response.setText("оки.... брой?");
                        previousCommand = "currency_created";
                    }
                } else if (previousCommand != null && previousCommand.equals("currency_created") && text != null) {
                    try {
                        long q = Long.parseLong(text);
                        urlBuilder.setQuantity(q);
                        previousCommand = "quantity_created";
                        response.setText("Готови сме.\n");
                    } catch (Exception e) {
                        response.setText("(@_@) Бройката ви трябва да е число. Опитай отново");
                    }
                } else {
                    response.setText("「(°ヘ°) Напиши \"new\" да започнем ;)");
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