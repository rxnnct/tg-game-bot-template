package ru.rxnnct.botapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final PlayerService playerService;

    public SendMessage receiveMessage(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var text = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();

            String responseMessage;
            if (text.startsWith("/set_name")) {
                String[] parts = text.split(" ", 2);
                if (parts.length < 2) {
                    responseMessage = "Please provide a name after the command, e.g., /set_name John";
                } else {
                    String playerName = parts[1].trim();
                    if (playerName.contains(" ")) {
                        responseMessage = "Name must be a single word, e.g., /set_name John";
                    } else {
                        try {
                            playerService.createOrUpdatePlayer(playerName, chatId);
                            responseMessage = String.format("New name: %s", playerName);
                        } catch (IllegalArgumentException e) {
                            responseMessage = e.getMessage();
                        }
                    }
                }
            } else {
                switch (text) {
                    case "/start" -> responseMessage = "Hi! Please, enter your name";
                    case "/player_info" -> {
                        var player = playerService.findPlayerByTgId(chatId);
                        responseMessage = player
                            .map(p -> String.format("Name: %s", p.getName()))
                            .orElse("Player not found.");
                    }
                    default -> responseMessage = "Unknown command";
                }
            }

            var message = new SendMessage();
            message.setChatId(chatId);
            message.setText(responseMessage);

            return message;
        }

        return null;
    }
}