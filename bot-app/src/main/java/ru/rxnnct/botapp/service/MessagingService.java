package ru.rxnnct.botapp.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final PlayerService playerService;
    private final MessageSource messageSource;

    public SendMessage receiveMessage(Update update, Locale locale) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var text = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();

            String responseMessage;
            if (text.startsWith("/set_name")) {
                String[] parts = text.split(" ", 2);
                if (parts.length < 2) {
                    responseMessage = messageSource.getMessage("bot.player.need_name", null,
                        locale);
                } else {
                    String playerName = parts[1].trim();
                    if (playerName.contains(" ")) {
                        responseMessage = messageSource.getMessage("bot.player.invalid_name", null,
                            locale);
                    } else {
                        try {
                            playerService.createOrUpdatePlayer(playerName, chatId);
                            responseMessage = messageSource.getMessage("bot.player.name_set",
                                new Object[]{playerName}, locale);
                        } catch (IllegalArgumentException e) {
                            responseMessage = e.getMessage();
                        }
                    }
                }
            } else {
                switch (text) {
                    case "/start" ->
                        responseMessage = messageSource.getMessage("bot.greeting", null, locale);
                    case "/player_info" -> {
                        var player = playerService.findPlayerByTgId(chatId);
                        responseMessage = player
                            .map(p -> messageSource.getMessage(
                                "bot.player.player_info",
                                new Object[]{p.getName()},
                                locale))
                            .orElseGet(() -> messageSource.getMessage(
                                "bot.player.player_not_found",
                                null,
                                locale));
                    }
                    default ->
                        responseMessage = messageSource.getMessage("bot.unknown_command", null,
                            locale);
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