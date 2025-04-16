package ru.rxnnct.gameapp.tgbot.service;

import java.util.Locale;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.rxnnct.gameapp.core.entity.AppUser;
import ru.rxnnct.gameapp.core.service.AppUserService;
import ru.rxnnct.gameapp.game.service.PvpService;

@Slf4j
@Component
@AllArgsConstructor
public class CommandHandler {

    private final AppUserService appUserService;
    private final MenuService menuService;
    private final PvpService pvpService;
    private final MessageSource messageSource;
    private final KeyboardService keyboardService;

    public SendMessage processMessage(String text, Long tgId, Locale locale) {
        if ("/start".equals(text)) {
            return handleStartCommand(tgId, locale);
        }

        Optional<AppUser> appUserOpt = appUserService.findAppUserByTgId(tgId);
        boolean isRegistered = appUserOpt.map(AppUser::getIsRegistered).orElse(false);

        if (menuService.isRegistrationInProgress(tgId)) {
            return handleNicknameInput(text, tgId, locale);
        }

        if (!isRegistered) {
            return handleUnregisteredUser(text, tgId, locale);
        }

        return handleRegisteredUser(text, tgId, locale);
    }

    private SendMessage handleStartCommand(Long chatId, Locale locale) {
        String greetingText = getGreetingText(chatId, locale);
        ReplyKeyboard menuKeyboard = keyboardService.createMenu(chatId, locale);

        return createMessage(chatId, greetingText, menuKeyboard);
    }

    private SendMessage handleUnregisteredUser(String text, Long tgId, Locale locale) {
        if (isCommand(text, "bot.menu.set_name", locale)) {
            menuService.setRegistrationInProgress(tgId, true);
            return createMessage(tgId, "bot.app_user.enter_name", null, locale);
        }
        return handleCommonCommands(text, tgId, locale);
    }

    private SendMessage handleRegisteredUser(String text, Long tgId, Locale locale) {
        if (isCommand(text, "bot.menu.info", locale)) {
            return handleInfo(tgId, locale);
        } else if (isCommand(text, "bot.menu.pve", locale)) {
            return createPveMenu(tgId, locale);
        } else if (isCommand(text, "bot.menu.pvp", locale)) {
            return handlePvp(tgId, locale);
        }
        return handleCommonCommands(text, tgId, locale);
    }

    private SendMessage createPveMenu(Long tgId, Locale locale) {
        SendMessage message = new SendMessage(tgId.toString(),
            messageSource.getMessage("bot.pve.menu.title", null, locale));
        message.setReplyMarkup(keyboardService.createPveInlineMenu(locale));
        return message;
    }

    private SendMessage handleInfo(Long tgId, Locale locale) {
        return appUserService.getInfo(tgId)
            .map(user -> createMessage(tgId, "bot.app_user.info",
                new Object[]{user.getName(), user.getBalance(), user.getCurrency()}, locale))
            .orElse(createMessage(tgId, "bot.app_user.app_user_not_found", null, locale));
    }

    private SendMessage handlePvp(Long tgId, Locale locale) {
        String result = pvpService.examplePvpActivity(tgId);
        return createMessage(tgId, "bot.pvp.result", new Object[]{result}, locale);
    }

    private SendMessage handleCommonCommands(String text, Long tgId, Locale locale) {
        if (isCommand(text, "bot.menu.help", locale) || "/help".equals(text)) {
            return createMessage(tgId, "bot.help", null, locale);
        }
        return createMessage(tgId, "bot.unknown_command", null, locale);
    }

    private SendMessage handleNicknameInput(String text, Long tgId, Locale locale) {
        if (!isValidNickname(text)) {
            return createMessage(tgId, "bot.app_user.invalid_name", null, locale);
        }

        try {
            appUserService.createOrUpdateAppUser(text, tgId, true);
            menuService.setRegistrationInProgress(tgId, false);
            SendMessage message = createMessage(tgId, "bot.app_user.name_set", new Object[]{text},
                locale);
            message.setReplyMarkup(keyboardService.createMenu(tgId, locale));
            return message;
        } catch (DataIntegrityViolationException e) {
            log.error("Name conflict: {}", text);
            return createMessage(tgId, "bot.app_user.name_exists", new Object[]{text}, locale);
        }
    }

    private SendMessage createMessage(Long chatId, String messageText, ReplyKeyboard replyMarkup) {
        SendMessage message = new SendMessage(chatId.toString(), messageText);
        if (replyMarkup != null) {
            message.setReplyMarkup(replyMarkup);
        }
        return message;
    }

    private SendMessage createMessage(Long chatId, String messageKey, Object[] args,
        Locale locale) {
        return createMessage(chatId,
            messageSource.getMessage(messageKey, args, locale),
            null);
    }

    private boolean isValidNickname(String text) {
        return text.length() >= 3 && !text.contains(" ") && text.matches("[a-zA-Z0-9]*");
    }

    private boolean isCommand(String text, String commandKey, Locale locale) {
        return messageSource.getMessage(commandKey, null, locale).equals(text);
    }

    public String getGreetingText(Long chatId, Locale locale) {
        return appUserService.findAppUserByTgId(chatId)
            .filter(AppUser::getIsRegistered)
            .map(user -> messageSource.getMessage(
                "bot.greeting_name",
                new Object[]{user.getName()},
                locale))
            .orElseGet(() -> messageSource.getMessage("bot.greeting", null, locale));
    }
}
