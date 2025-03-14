package ru.rxnnct.gameapp.tgbot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
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
        commands.add(new BotCommand("/start", "[description]"));
        commands.add(new BotCommand("/set_name", "[description]"));
        commands.add(new BotCommand("/player_info", "[description]"));
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
            String languageCode = update.getMessage().getFrom().getLanguageCode();
            Locale locale = Locale.forLanguageTag(languageCode != null ? languageCode : "en");
            SendMessage sendMessage = messagingService.receiveMessage(update, locale);
            if (sendMessage != null) {
                execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            log.error("PROBLEM IN: TelegramBot.onUpdateReceived", e);
        }
    }

}