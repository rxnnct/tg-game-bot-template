package ru.rxnnct.gameapp.tgbot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rxnnct.gameapp.tgbot.config.properties.TelegramBotProperties;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramBotProperties properties;
    private final MessagingService messagingService;

    public TelegramBot(TelegramBotProperties properties, MessagingService messagingService) {
        super(properties.token());
        this.properties = properties;
        this.messagingService = messagingService;

        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/help", "[description]"));

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
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
                String languageCode = update.getMessage().getFrom().getLanguageCode();
                Locale locale = Locale.forLanguageTag(languageCode != null ? languageCode : "en");
                SendMessage sendMessage = messagingService.receiveMessage(update, locale);
                if (sendMessage != null) {
                    execute(sendMessage);
                }
            } else if (update.hasCallbackQuery()) {
                log.info("CallbackQuery received: {}", update.getCallbackQuery().getData());
            } else if (update.hasMyChatMember()) {
                handleMyChatMemberUpdate(update.getMyChatMember());
            } else {
                log.warn("Unsupported update type received: {}", update);
            }
        } catch (TelegramApiException e) {
            log.error("Error processing update: {}", update, e);
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

}