package ru.rxnnct.gameapp.tgbot.service;

import java.util.Locale;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.rxnnct.gameapp.core.service.AppUserService;
import ru.rxnnct.gameapp.game.dto.ExampleFightResultDto;
import ru.rxnnct.gameapp.game.service.PvpService;
import ru.rxnnct.gameapp.tgbot.config.properties.TelegramBotProperties;
import ru.rxnnct.gameapp.tgbot.enums.MenuState;
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
            .map(user -> handleRegisteredUser(text, tgId, locale))
            .orElseGet(() -> handleUnregisteredUser(text, tgId, locale));
    }

    private BotResponse handleStartCommand(Long chatId, Locale locale) {
        return Optional.ofNullable(botProperties.startCommandCachedImageId())
            .filter(id -> !id.isBlank())
            .<BotResponse>map(id -> new PhotoResponse(id, getGreetingText(chatId, locale),
                keyboardService.createMainMenu(chatId, locale, MenuState.MAIN_MENU)))
            .orElseGet(() -> new TextResponse(getGreetingText(chatId, locale),
                keyboardService.createMainMenu(chatId, locale, MenuState.MAIN_MENU)));
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
            return handlePvpMenu(tgId, locale);
        } else if (isCommand(text, "bot.pvp_menu.fight", locale)) {
            return handleFight(tgId, locale);
        } else if (isCommand(text, "bot.pvp_menu.switch_pvp_ready", locale)) {
            return handleSwitchPvpReady(tgId, locale);
        } else if (isCommand(text, "bot.menu.to_main_menu", locale)) {
            return handleToMainMenuTransition(tgId, locale);
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

    private BotResponse buildPvpResponse(Long tgId, Locale locale, boolean isSwitched) {
        boolean isPvpReady = isSwitched
            ? pvpService.switchIsPvpAvailable(tgId)
            : pvpService.getIsPvpAvailable(tgId);

        String onText = messageSource.getMessage("bot.string.on", null, locale);
        String offText = messageSource.getMessage("bot.string.off", null, locale);

        String messageKey = isSwitched ? "bot.pvp.info_switched" : "bot.pvp.info";
        ReplyKeyboardMarkup keyboard =
            isSwitched ? null : keyboardService.createMainMenu(tgId, locale, MenuState.PVP_MENU);

        return new TextResponse(
            messageSource.getMessage(messageKey, new Object[]{isPvpReady ? onText : offText},
                locale),
            keyboard
        );
    }

    private BotResponse handlePvpMenu(Long tgId, Locale locale) {
        return buildPvpResponse(tgId, locale, false);
    }

    private BotResponse handleSwitchPvpReady(Long tgId, Locale locale) {
        return buildPvpResponse(tgId, locale, true);
    }

    private BotResponse handleFight(Long tgId, Locale locale) {
        ExampleFightResultDto result = pvpService.exampleFight(tgId);
        return new TextResponse(
            messageSource.getMessage("bot.pvp.result", new Object[]{result.getResult()}, locale),
            null
        );
    }

    private BotResponse handleToMainMenuTransition(Long tgId, Locale locale) {
        return new TextResponse(
            messageSource.getMessage("bot.menu.to_main_menu", null, locale),
            keyboardService.createMainMenu(tgId, locale, MenuState.MAIN_MENU)
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
            appUserService.createOrUpdateAppUser(text, tgId);
            menuService.setRegistrationInProgress(tgId, false);
            return new TextResponse(
                messageSource.getMessage("bot.app_user.name_set", new Object[]{text}, locale),
                keyboardService.createMainMenu(tgId, locale, MenuState.MAIN_MENU)
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
            .map(user -> messageSource.getMessage(
                "bot.greeting_name",
                new Object[]{user.getName()},
                locale))
            .orElseGet(() -> messageSource.getMessage("bot.greeting", null, locale));
    }
}
