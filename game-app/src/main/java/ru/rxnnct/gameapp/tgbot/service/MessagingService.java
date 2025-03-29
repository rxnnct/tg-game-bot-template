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
import ru.rxnnct.gameapp.core.entity.AppUser;
import ru.rxnnct.gameapp.core.service.AppUserService;
import ru.rxnnct.gameapp.game.service.PveService;
import ru.rxnnct.gameapp.game.service.PvpService;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagingService {

    private final AppUserService appUserService;
    private final KeyboardService keyboardService;
    private final MenuService menuService;
    private final PveService pveService;
    private final PvpService pvpService;

    private final MessageSource messageSource;

    public SendMessage receiveMessage(Update update, Locale locale) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            log.warn("Update does not contain a valid text message: {}", update);
            return null;
        }

        var text = update.getMessage().getText();
        var tgId = update.getMessage().getChatId();

        Optional<AppUser> appUserOpt = appUserService.findAppUserByTgId(tgId);
        boolean isRegistered = appUserOpt.map(AppUser::getIsRegistered).orElse(false);

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
        } else if (isCommand(text, "bot.menu.info", locale)) {
            return handleInfo(tgId, locale);
        } else if (isCommand(text, "bot.menu.pve", locale)) {
            if (!pveService.isExamplePveActivityInProgress(tgId)) {
                return handlePve(tgId, locale);
            } else {
                return null;
            }
        } else if (isCommand(text, "bot.menu.pvp", locale)) {
            return handlePvP(tgId, locale);
        } else {
            return handleUnknownCommand(tgId, locale);
        }
    }

    private SendMessage handleStart(Long tgId, Locale locale) {
        Optional<AppUser> appUserOpt = appUserService.findAppUserByTgId(tgId);
        boolean isRegistered = appUserOpt.map(AppUser::getIsRegistered).orElse(false);

        String responseMessage;
        if (isRegistered) {
            responseMessage = messageSource.getMessage("bot.greeting_name",
                new Object[]{appUserOpt.get().getName()}, locale);
        } else {
            responseMessage = messageSource.getMessage("bot.greeting", null, locale);
        }

        SendMessage sendMessage = buildSendMessage(tgId, responseMessage);
        sendMessage.setReplyMarkup(keyboardService.createMenu(tgId, locale));
        return sendMessage;
    }

    private SendMessage handleRegistrationStart(Long tgId, Locale locale) {
        String responseMessage = messageSource.getMessage("bot.app_user.enter_name", null, locale);
        SendMessage sendMessage = buildSendMessage(tgId, responseMessage);
        sendMessage.setReplyMarkup(null);
        return sendMessage;
    }

    private SendMessage handleNicknameInput(String text, Long tgId, Locale locale) {
        String responseMessage;

        if (text.length() < 3 || text.contains(" ") || !text.matches("[a-zA-Z0-9]*")) {
            responseMessage = messageSource.getMessage("bot.app_user.invalid_name", null, locale);
        } else {
            try {
                appUserService.createOrUpdateAppUser(text, tgId, true);
                responseMessage = messageSource.getMessage("bot.app_user.name_set",
                    new Object[]{text}, locale);
                menuService.setRegistrationInProgress(tgId, false);
            } catch (DataIntegrityViolationException e) {
                log.error("Data integrity violation when setting name '{}': {}", text,
                    e.getMessage());
                responseMessage = messageSource.getMessage("bot.app_user.name_exists",
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

    private SendMessage handleInfo(Long tgId, Locale locale) {
        var info = appUserService.getInfo(tgId);
        String responseMessage = info
            .map(p -> messageSource.getMessage("bot.app_user.info",
                new Object[]{p.getName(), p.getBalance(), p.getCurrency()}, locale))
            .orElseGet(
                () -> messageSource.getMessage("bot.app_user.app_user_not_found", null, locale));
        return buildSendMessage(tgId, responseMessage);
    }

    private SendMessage handlePve(Long tgId, Locale locale) {
        pveService.scheduleExamplePveActivity(tgId, locale);
        pveService.setExamplePveActivityInProgress(tgId, true);
        String responseMessage = messageSource.getMessage("bot.character.pve_start", null, locale);
        return buildSendMessage(tgId, responseMessage);
    }

    private SendMessage handlePvP(Long tgId, Locale locale) {
        String pvpResult = pvpService.examplePvpActivity(tgId);
        String responseMessage = messageSource.getMessage("bot.character.pvp_result",
            new Object[]{pvpResult}, locale);
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