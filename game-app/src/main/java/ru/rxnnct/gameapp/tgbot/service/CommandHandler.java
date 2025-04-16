package ru.rxnnct.gameapp.tgbot.service;

import java.util.Locale;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import ru.rxnnct.gameapp.core.entity.AppUser;
import ru.rxnnct.gameapp.core.service.AppUserService;
import ru.rxnnct.gameapp.game.service.PvpService;
import ru.rxnnct.gameapp.tgbot.config.properties.TelegramBotProperties;
import ru.rxnnct.gameapp.tgbot.service.response.BotResponse;
import ru.rxnnct.gameapp.tgbot.service.response.PhotoResponse;
import ru.rxnnct.gameapp.tgbot.service.response.TextResponse;

@Slf4j
@Component
@AllArgsConstructor
public class CommandHandler {

    private final AppUserService appUserService;
    private final MenuService menuService;
    private final PvpService pvpService;
    private final MessageSource messageSource;
    private final KeyboardService keyboardService;
    private final TelegramBotProperties botProperties;

    public BotResponse processMessage(String text, Long tgId, Locale locale) {
        return switch (text) {
            case "/start" -> handleStartCommand(tgId, locale);
            case "/help" -> createHelpResponse(locale);
            default -> handleUserState(text, tgId, locale);
        };
    }

    private BotResponse handleUserState(String text, Long tgId, Locale locale) {
        if (menuService.isRegistrationInProgress(tgId)) {
            return handleNicknameInput(text, tgId, locale);
        }
        return appUserService.findAppUserByTgId(tgId)
            .filter(AppUser::getIsRegistered)
            .map(user -> handleRegisteredUser(text, tgId, locale))
            .orElseGet(() -> handleUnregisteredUser(text, tgId, locale));
    }

    private BotResponse handleStartCommand(Long chatId, Locale locale) {
        return Optional.ofNullable(botProperties.startCommandCachedImageId())
            .filter(id -> !id.isBlank())
            .<BotResponse>map(id -> new PhotoResponse(id, getGreetingText(chatId, locale),
                keyboardService.createMenu(chatId, locale)))
            .orElseGet(() -> new TextResponse(getGreetingText(chatId, locale),
                keyboardService.createMenu(chatId, locale)));
    }

    private BotResponse createHelpResponse(Locale locale) {
        return new TextResponse(messageSource.getMessage("bot.help", null, locale), null);
    }

    private BotResponse handleUnregisteredUser(String text, Long tgId, Locale locale) {
        if (isCommand(text, "bot.menu.set_name", locale)) {
            menuService.setRegistrationInProgress(tgId, true);
            return new TextResponse(
                messageSource.getMessage("bot.app_user.enter_name", null, locale),
                null
            );
        }
        return handleCommonCommands(text, locale);
    }

    private BotResponse handleRegisteredUser(String text, Long tgId, Locale locale) {
        if (isCommand(text, "bot.menu.info", locale)) {
            return handleInfo(tgId, locale);
        } else if (isCommand(text, "bot.menu.pve", locale)) {
            return createPveMenu(locale);
        } else if (isCommand(text, "bot.menu.pvp", locale)) {
            return handlePvp(tgId, locale);
        }
        return handleCommonCommands(text, locale);
    }

    private BotResponse createPveMenu(Locale locale) {
        String message = messageSource.getMessage("bot.pve.menu.title", null, locale);
        return new TextResponse(message, keyboardService.createPveInlineMenu(locale));
    }

    private BotResponse handleInfo(Long tgId, Locale locale) {
        return appUserService.getInfo(tgId)
            .map(user -> new TextResponse(
                messageSource.getMessage("bot.app_user.info",
                    new Object[]{user.getName(), user.getBalance(), user.getCurrency()}, locale),
                null
            ))
            .orElse(new TextResponse(
                messageSource.getMessage("bot.app_user.app_user_not_found", null, locale),
                null
            ));
    }

    private BotResponse handlePvp(Long tgId, Locale locale) {
        String result = pvpService.examplePvpActivity(tgId);
        return new TextResponse(
            messageSource.getMessage("bot.pvp.result", new Object[]{result}, locale),
            null
        );
    }

    private BotResponse handleCommonCommands(String text, Locale locale) {
        if (isCommand(text, "bot.menu.help", locale) || "/help".equals(text)) {
            return new TextResponse(
                messageSource.getMessage("bot.help", null, locale),
                null
            );
        }
        return new TextResponse(
            messageSource.getMessage("bot.unknown_command", null, locale),
            null
        );
    }

    private BotResponse handleNicknameInput(String text, Long tgId, Locale locale) {
        if (!isValidNickname(text)) {
            return new TextResponse(
                messageSource.getMessage("bot.app_user.invalid_name", null, locale),
                null
            );
        }

        try {
            appUserService.createOrUpdateAppUser(text, tgId, true);
            menuService.setRegistrationInProgress(tgId, false);
            return new TextResponse(
                messageSource.getMessage("bot.app_user.name_set", new Object[]{text}, locale),
                keyboardService.createMenu(tgId, locale)
            );
        } catch (DataIntegrityViolationException e) {
            log.error("Name conflict: {}", text);
            return new TextResponse(
                messageSource.getMessage("bot.app_user.name_exists", new Object[]{text}, locale),
                null
            );
        }
    }

    private boolean isValidNickname(String text) {
        return text.length() >= 3 && !text.contains(" ") && text.matches("[a-zA-Z0-9]*");
    }

    private boolean isCommand(String text, String commandKey, Locale locale) {
        return messageSource.getMessage(commandKey, null, locale).equals(text);
    }

    public String getGreetingText(Long tgId, Locale locale) {
        return appUserService.findAppUserByTgId(tgId)
            .filter(AppUser::getIsRegistered)
            .map(user -> messageSource.getMessage(
                "bot.greeting_name",
                new Object[]{user.getName()},
                locale))
            .orElseGet(() -> messageSource.getMessage("bot.greeting", null, locale));
    }
}
