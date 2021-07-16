package com.home.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class KalpakBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(KalpakBot.class);
    private final UrlBuilder urlBuilder;
    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    public KalpakBot(UrlBuilder urlBuilder) {
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
                    response.setText("Напиши ми ся името на продукта");
                    previousCommand = "new";
                } else if (previousCommand != null && previousCommand.equals("new") && text != null) {
                    if (text.isEmpty()) {
                        response.setText("Грешно име на продукта. Напишете ми го отново");
                    } else {
                        urlBuilder.setProductName(text);
                        response.setText("Добре. Продукта ви ще се казва \"" + text + "\"\n" +
                                "Колко ще струва той (в центове [min 100])?");
                        previousCommand = "name_created";
                    }
                } else if (previousCommand != null && previousCommand.equals("name_created") && text != null) {
                    try {
                        long price = Long.parseLong(text);
                        urlBuilder.setPrice(price);
                        previousCommand = "price_created";
                        response.setText("Желаема валута? Достъпни тук => https://stripe.com/docs/currencies#presentment-currencies");
                    } catch (Exception e) {
                        response.setText("Цената ви трябва да е някакво число. Опитай отново");
                    }
                } else if (previousCommand != null && previousCommand.equals("price_created") && text != null) {
                    if (text.isEmpty()) {
                        response.setText("Вкарай някаква цена. Виж достъпните https://stripe.com/docs/currencies#presentment-currencies");
                    } else {
                        urlBuilder.setCurrency(text);
                        response.setText("оки...." +
                                "Колко бройки ще искаме?");
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
                }else {
                    response.setText("Не разбрах какво имате предвид. Напиши \"нов продукт\" да започнем");
                    previousCommand = null;
                }

                if(previousCommand != null && previousCommand.equals("quantity_created")){
                    try {
                        response.setText(urlBuilder.build());
                    }catch (Exception e){
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
