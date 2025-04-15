package ru.rxnnct.gameapp.tgbot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rxnnct.gameapp.core.entity.AppUser;
import ru.rxnnct.gameapp.core.service.AppUserService;
import ru.rxnnct.gameapp.game.service.PvpService;
import ru.rxnnct.gameapp.tgbot.config.properties.TelegramBotProperties;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramBotProperties properties;
    private final AppUserService appUserService;
    private final KeyboardService keyboardService;
    private final MenuService menuService;
    private final PvpService pvpService;
    private final MessageSource messageSource;
    private final CallbackQueryHandler callbackQueryHandler;

    public TelegramBot(TelegramBotProperties properties,
        AppUserService appUserService,
        KeyboardService keyboardService,
        MenuService menuService,
        PvpService pvpService,
        MessageSource messageSource,
        CallbackQueryHandler callbackQueryHandler) {
        super(properties.token());
        this.properties = properties;
        this.appUserService = appUserService;
        this.keyboardService = keyboardService;
        this.menuService = menuService;
        this.pvpService = pvpService;
        this.messageSource = messageSource;
        this.callbackQueryHandler = callbackQueryHandler;

        initializeCommands();
    }

    private void initializeCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/help",
            messageSource.getMessage("bot.menu.help", null, Locale.getDefault())));

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot commands: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return properties.name();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update);
            } else if (update.hasCallbackQuery()) {
                callbackQueryHandler.handleCallbackQuery(update, this);
            } else if (update.hasMyChatMember()) {
                handleMyChatMemberUpdate(update.getMyChatMember());
            } else {
                log.warn("Unsupported update type received: {}", update);
            }
        } catch (TelegramApiException e) {
            log.error("Error processing update: {}", update, e);
        }
    }

    private void handleMessage(Update update) throws TelegramApiException {
        Message message = update.getMessage();
        String text = message.getText();
        Long chatId = message.getChatId();
        Locale locale = getLocaleFromUser(message.getFrom());

        SendMessage response = processMessage(text, chatId, locale);
        if (response != null) {
            execute(response);
        }
    }

    private void handleStartCommand(Long chatId, Locale locale) {
        Optional<AppUser> appUserOpt = appUserService.findAppUserByTgId(chatId);

        String caption = appUserOpt
            .filter(AppUser::getIsRegistered)
            .map(user -> messageSource.getMessage(
                "bot.greeting_name",
                new Object[]{user.getName()},
                locale))
            .orElseGet(() -> messageSource.getMessage("bot.greeting", null, locale));

        SendPhoto sendPhoto = SendPhoto.builder()
            .chatId(chatId.toString())
            .photo(new InputFile(properties.startCommandCachedImageId()))
            .caption(caption)
            .replyMarkup(keyboardService.createMenu(chatId, locale))
            .build();

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            log.error("Failed to send start photo: {}", e.getMessage());
        }
    }

    private SendMessage processMessage(String text, Long tgId, Locale locale) {
        if ("/start".equals(text)) {
            handleStartCommand(tgId, locale);
            return null;
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

    private SendMessage handleUnregisteredUser(String text, Long tgId, Locale locale) {
        if (isCommand(text, "bot.menu.set_name", locale)) {
            menuService.setRegistrationInProgress(tgId, true);
            return createMessage(tgId, "bot.app_user.enter_name", null, null);
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

    private SendMessage handleCommonCommands(String text, Long tgId, Locale locale) {
        if (isCommand(text, "bot.menu.help", locale) || "/help".equals(text)) {
            return createMessage(tgId, "bot.help", null, null);
        }
        return createMessage(tgId, "bot.unknown_command", null, null);
    }

    private SendMessage handleNicknameInput(String text, Long tgId, Locale locale) {
        if (!isValidNickname(text)) {
            return createMessage(tgId, "bot.app_user.invalid_name", null, null);
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

    private SendMessage handleInfo(Long tgId, Locale locale) {
        return appUserService.getInfo(tgId)
            .map(user -> createMessage(tgId, "bot.app_user.info",
                new Object[]{user.getName(), user.getBalance(), user.getCurrency()}, locale))
            .orElse(createMessage(tgId, "bot.app_user.app_user_not_found", null, locale));
    }

    private SendMessage createPveMenu(Long tgId, Locale locale) {
        SendMessage message = new SendMessage(tgId.toString(),
            messageSource.getMessage("bot.pve.menu.title", null, locale));
        message.setReplyMarkup(keyboardService.createPveInlineMenu(locale));
        return message;
    }

    private SendMessage handlePvp(Long tgId, Locale locale) {
        String result = pvpService.examplePvpActivity(tgId);
        return createMessage(tgId, "bot.pvp.result", new Object[]{result}, locale);
    }

    private SendMessage createMessage(Long chatId, String messageKey, Object[] args,
        Locale locale) {
        return new SendMessage(chatId.toString(),
            messageSource.getMessage(messageKey, args, locale));
    }

    private boolean isValidNickname(String text) {
        return text.length() >= 3 && !text.contains(" ") && text.matches("[a-zA-Z0-9]*");
    }

    private boolean isCommand(String text, String commandKey, Locale locale) {
        return messageSource.getMessage(commandKey, null, locale).equals(text);
    }

    private Locale getLocaleFromUser(User user) {
        return Locale.forLanguageTag(
            Optional.ofNullable(user.getLanguageCode()).orElse("en"));
    }

    private void handleMyChatMemberUpdate(ChatMemberUpdated chatMemberUpdated) {
        Chat chat = chatMemberUpdated.getChat();
        User user = chatMemberUpdated.getFrom();
        ChatMember oldChatMember = chatMemberUpdated.getOldChatMember();
        ChatMember newChatMember = chatMemberUpdated.getNewChatMember();

        log.info("Chat member status changed for chat {} (user: {}): {} -> {}",
            chat.getId(), user.getUserName(),
            oldChatMember.getStatus(), newChatMember.getStatus());

        if (newChatMember.getStatus().equals("kicked")) {
            log.info("Bot was removed from chat {}", chat.getId());
        } else if (newChatMember.getStatus().equals("member")) {
            log.info("Bot was added to chat {}", chat.getId());
        }
    }

}