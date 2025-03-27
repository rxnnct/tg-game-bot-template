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
import ru.rxnnct.gameapp.game.service.PveService;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagingService {

    private final PlayerService playerService;
    private final KeyboardService keyboardService;
    private final MenuService menuService;
    private final PveService pveService;

    private final MessageSource messageSource;

    public SendMessage receiveMessage(Update update, Locale locale) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            log.warn("Update does not contain a valid text message: {}", update);
            return null;
        }

        var text = update.getMessage().getText();
        var tgId = update.getMessage().getChatId();

        Optional<Player> playerOpt = playerService.findPlayerByTgId(tgId);
        boolean isRegistered = playerOpt.map(Player::getIsRegistered).orElse(false);

        if (menuService.isRegistrationInProgress(tgId)) {
            return handleNicknameInput(text, tgId, locale);
        }

        if ("/start".equals(text)) {
            return handleStart(tgId, locale);
        }

        if (!isRegistered) {
            if (isCommand(text, "bot.menu.set_name", locale)) {
                menuService.setRegistrationInProgress(tgId, true);
                return handleRegistrationStart(tgId, locale);
            } else if (isCommand(text, "bot.menu.help", locale) || "/help".equals(text)) {
                return handleHelp(tgId, locale);
            } else {
                return handleUnknownCommand(tgId, locale);
            }
        }

        if (isCommand(text, "bot.menu.help", locale) || "/help".equals(text)) {
            return handleHelp(tgId, locale);
        } else if (isCommand(text, "bot.menu.player_info", locale)) {
            return handlePlayerInfo(tgId, locale);
        } else if (isCommand(text, "bot.menu.pve", locale)) {
            return handlePve(tgId, locale);
        } else {
            return handleUnknownCommand(tgId, locale);
        }
    }

    private SendMessage handleStart(Long tgId, Locale locale) {
        Optional<Player> playerOpt = playerService.findPlayerByTgId(tgId);
        boolean isRegistered = playerOpt.map(Player::getIsRegistered).orElse(false);

        String responseMessage;
        if (isRegistered) {
            responseMessage = messageSource.getMessage("bot.greeting_name",
                new Object[]{playerOpt.get().getName()}, locale);
        } else {
            responseMessage = messageSource.getMessage("bot.greeting", null, locale);
        }

        SendMessage sendMessage = buildSendMessage(tgId, responseMessage);
        sendMessage.setReplyMarkup(keyboardService.createMenu(tgId, locale));
        return sendMessage;
    }

    private SendMessage handleRegistrationStart(Long tgId, Locale locale) {
        String responseMessage = messageSource.getMessage("bot.player.enter_name", null, locale);
        SendMessage sendMessage = buildSendMessage(tgId, responseMessage);
        sendMessage.setReplyMarkup(null);
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
                menuService.setRegistrationInProgress(tgId, false);
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

    private SendMessage handleHelp(Long tgId, Locale locale) {
        String responseMessage = messageSource.getMessage("bot.help", null, locale);
        return buildSendMessage(tgId, responseMessage);
    }

    private SendMessage handlePlayerInfo(Long tgId, Locale locale) {
        var playerInfo = playerService.getPlayerInfo(tgId);
        String responseMessage = playerInfo
            .map(p -> messageSource.getMessage("bot.player.player_info",
                new Object[]{p.getName(), p.getBalance(), p.getCurrency()},
                locale))
            .orElseGet(() -> messageSource.getMessage("bot.player.player_not_found", null, locale));
        return buildSendMessage(tgId, responseMessage);
    }

    private SendMessage handlePve(Long tgId, Locale locale) {
        pveService.schedulePveActivity(tgId, locale);
        String responseMessage = messageSource.getMessage("bot.character.pve_start", null, locale);
        return buildSendMessage(tgId, responseMessage);
    }

    private SendMessage handleUnknownCommand(Long tgId, Locale locale) {
        String responseMessage = messageSource.getMessage("bot.unknown_command", null, locale);
        return buildSendMessage(tgId, responseMessage);
    }

    private SendMessage buildSendMessage(Long chatId, String responseMessage) {
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText(responseMessage);
        return message;
    }

    private boolean isCommand(String text, String commandKey, Locale locale) {
        return getLocalizedCommand(commandKey, locale).equals(text);
    }

    private String getLocalizedCommand(String commandKey, Locale locale) {
        return messageSource.getMessage(commandKey, null, locale);
    }
}