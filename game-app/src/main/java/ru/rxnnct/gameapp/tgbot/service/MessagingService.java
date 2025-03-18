package ru.rxnnct.gameapp.tgbot.service;

import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.rxnnct.gameapp.core.entity.Player;
import ru.rxnnct.gameapp.core.service.PlayerService;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagingService {

    private final PlayerService playerService;
    private final MessageSource messageSource;
    private final KeyboardService keyboardService;

    public SendMessage receiveMessage(Update update, Locale locale) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            log.warn("Update does not contain a valid text message: {}", update);
            return null;
        }

        var text = update.getMessage().getText();
        var tgId = update.getMessage().getChatId();

        Optional<Player> playerOpt = playerService.findPlayerByTgId(tgId);
        boolean isRegistered = playerOpt.map(Player::getIsRegistered).orElse(false);

        String responseMessage;

        if ("/start".equals(text)) {
            responseMessage = handleStart(tgId, locale);
            SendMessage sendMessage = buildSendMessage(tgId, responseMessage);
            sendMessage.setReplyMarkup(keyboardService.createMenu(tgId, locale));
            return sendMessage;
        }

        if (!isRegistered) {
            return handleNicknameInput(text, tgId, locale);
        }

        if (isCommand(text, "bot.menu.help", locale) || "/help".equals(text)) {
            responseMessage = messageSource.getMessage("bot.help", null, locale);
        } else if (isCommand(text, "bot.menu.set_name", locale)) {
            responseMessage = messageSource.getMessage("bot.player.enter_name", null, locale);
        } else {
            responseMessage = handleRegisteredUserCommands(text, tgId, locale);
        }

        SendMessage sendMessage = buildSendMessage(tgId, responseMessage);
        sendMessage.setReplyMarkup(keyboardService.createMenu(tgId, locale));
        return sendMessage;
    }

    private SendMessage handleNicknameInput(String text, Long tgId, Locale locale) {
        String responseMessage;

        if (text.length() < 3 || text.contains(" ") || !text.matches("[a-zA-Z0-9]*")) {
            responseMessage = messageSource.getMessage("bot.player.invalid_name", null, locale);
        } else {
            try {
                playerService.createOrUpdatePlayer(text, tgId, true);
                responseMessage = messageSource.getMessage("bot.player.name_set",
                    new Object[]{text}, locale);
            } catch (DataIntegrityViolationException e) {
                log.error("Data integrity violation when setting name '{}': {}", text,
                    e.getMessage());
                responseMessage = messageSource.getMessage("bot.player.name_exists",
                    new Object[]{text}, locale);
            }
        }

        SendMessage sendMessage = buildSendMessage(tgId, responseMessage);
        sendMessage.setReplyMarkup(keyboardService.createMenu(tgId, locale));
        return sendMessage;
    }

    private String handleRegisteredUserCommands(String text, Long tgId, Locale locale) {
        if (isCommand(text, "bot.menu.player_info", locale)) {
            return handlePlayerInfo(tgId, locale);
        } else {
            return messageSource.getMessage("bot.unknown_command", null, locale);
        }
    }

    private boolean isCommand(String text, String commandKey, Locale locale) {
        return getLocalizedCommand(commandKey, locale).equals(text);
    }

    private String handleStart(Long chatId, Locale locale) {
        var player = playerService.findPlayerByTgId(chatId);
        return player.map(
                value -> messageSource.getMessage("bot.greeting_name", new Object[]{value.getName()},
                    locale))
            .orElseGet(() -> messageSource.getMessage("bot.player.need_name", null, locale));
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

    private String getLocalizedCommand(String commandKey, Locale locale) {
        return messageSource.getMessage(commandKey, null, locale);
    }
}