package ru.rxnnct.gameapp.tgbot.service.response;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rxnnct.gameapp.tgbot.service.TelegramBot;

public interface BotResponse {

    void send(TelegramBot bot, Long chatId) throws TelegramApiException;
}