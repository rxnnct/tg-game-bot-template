package ru.rxnnct.botapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.rxnnct.botapp.service.TelegramBot;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TelegramBotConfiguration {

    private final TelegramBot telegramBot;

    @EventListener(ContextRefreshedEvent.class)
    public void initialize() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            log.error("Error during bot initialization", e);
        }
    }
}