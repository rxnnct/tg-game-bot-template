package ru.rxnnct.gameapp.tgbot.service;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotResponse {

    void send(TelegramBot bot, Long chatId) throws TelegramApiException;
}