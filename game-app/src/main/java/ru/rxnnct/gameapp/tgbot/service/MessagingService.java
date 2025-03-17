package ru.rxnnct.gameapp.tgbot.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagingService {

    private final PlayerService playerService;
    private final MessageSource messageSource;
    private final MenuService menuService;

//    private final KeyboardService keyboardService;

    public SendMessage receiveMessage(Update update, Locale locale) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            log.warn("Update does not contain a valid text message: {}", update);
            return null;
        }

        var text = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();

        String responseMessage;
        if (text.startsWith("/set_name")) {
            responseMessage = handleSetName(text, chatId, locale);
        } else if ("/help".equals(text)) {
            responseMessage = messageSource.getMessage("bot.help", null, locale);
        } else if ("/player_info".equals(text)) {
            responseMessage = handlePlayerInfo(chatId, locale);
        } else if ("/start".equals(text)) {
            responseMessage = handleStart(chatId, locale);
        } else {
            responseMessage = messageSource.getMessage("bot.unknown_command", null, locale);
        }

        SendMessage sendMessage = buildSendMessage(chatId, responseMessage);
//        sendMessage.setReplyMarkup(keyboardService.createMainMenuKeyboard(menuState));
        return sendMessage;
    }

    private String handleSetName(String text, Long chatId, Locale locale) {
        String[] parts = text.split(" ", 2);
        if (parts.length < 2) {
            return messageSource.getMessage("bot.player.need_name", null, locale);
        }

        String playerName = parts[1].trim();
        if (playerName.length() < 3 || playerName.contains(" ") || !playerName.matches(
            "[a-zA-Z0-9]*")) {
            return messageSource.getMessage("bot.player.invalid_name", null, locale);
        }

        try {
            playerService.createOrUpdatePlayer(playerName, chatId);
            return messageSource.getMessage("bot.player.name_set", new Object[]{playerName},
                locale);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when setting name '{}': {}", playerName,
                e.getMessage());
            return messageSource.getMessage("bot.player.name_exists", new Object[]{playerName},
                locale);
        }
    }

    private String handleStart(Long chatId, Locale locale) {
        var player = playerService.findPlayerByTgId(chatId);
        return player
            .map(p -> messageSource.getMessage("bot.greeting_name", new Object[]{p.getName()},
                locale))
            .orElseGet(() -> messageSource.getMessage("bot.greeting", null, locale));
    }

    private String handlePlayerInfo(Long chatId, Locale locale) {
        var player = playerService.findPlayerByTgId(chatId);
        return player
            .map(p -> messageSource.getMessage("bot.player.player_info", new Object[]{p.getName()},
                locale))
            .orElseGet(() -> messageSource.getMessage("bot.player.player_not_found", null, locale));
    }

    private SendMessage buildSendMessage(Long chatId, String responseMessage) {
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText(responseMessage);
        return message;
    }
}