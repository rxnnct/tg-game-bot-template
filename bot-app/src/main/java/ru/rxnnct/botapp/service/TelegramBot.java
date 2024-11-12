package ru.rxnnct.botapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rxnnct.botapp.config.properties.TelegramBotProperties;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramBotProperties properties;
    private final MessagingService messagingService;

    public TelegramBot(TelegramBotProperties properties, MessagingService messagingService) {
        super(properties.token());
        this.properties = properties;
        this.messagingService = messagingService;
    }

    @Override
    public String getBotUsername() {
        return properties.name();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            SendMessage sendMessage = messagingService.receiveMessage(update);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("PROBLEM IN: TelegramBot.onUpdateReceived");
        }
    }
}