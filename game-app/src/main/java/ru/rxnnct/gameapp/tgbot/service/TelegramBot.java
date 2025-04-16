package ru.rxnnct.gameapp.tgbot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rxnnct.gameapp.tgbot.config.properties.TelegramBotProperties;
import ru.rxnnct.gameapp.tgbot.service.response.BotResponse;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramBotProperties properties;
    private final CommandHandler commandHandler;
    private final CallbackQueryHandler callbackQueryHandler;
    private final MessageSource messageSource;

    public TelegramBot(TelegramBotProperties properties,
        CommandHandler commandHandler,
        CallbackQueryHandler callbackQueryHandler,
        MessageSource messageSource) {
        super(properties.token());
        this.properties = properties;
        this.commandHandler = commandHandler;
        this.callbackQueryHandler = callbackQueryHandler;
        this.messageSource = messageSource;

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

        BotResponse response = commandHandler.processMessage(text, chatId, locale);
        if (response != null) {
            response.send(this, chatId);
        }
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

    private Locale getLocaleFromUser(User user) {
        return Locale.forLanguageTag(
            Optional.ofNullable(user.getLanguageCode()).orElse("en"));
    }
}