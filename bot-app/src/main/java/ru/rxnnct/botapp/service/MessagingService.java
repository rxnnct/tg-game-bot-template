package ru.rxnnct.botapp.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class MessagingService {

    public SendMessage receiveMessage(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var text = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();

            String responseMessage;
            switch (text) {
                case "/start" -> responseMessage = "Hi! Please, enter your name";
                case "/set_name" -> responseMessage = String.format("New name: %s!", chatId);
                case "/player_info" -> responseMessage = String.format("ChatId: %s!", chatId);
                default -> responseMessage = "Unknown command";
            }

            var message = new SendMessage();
            message.setChatId(chatId);
            message.setText(responseMessage);

            return message;
        }

        return null;
    }
}