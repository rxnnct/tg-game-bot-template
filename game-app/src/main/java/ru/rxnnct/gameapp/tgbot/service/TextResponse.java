package ru.rxnnct.gameapp.tgbot.service;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RequiredArgsConstructor
public class TextResponse implements BotResponse {

    private final String text;
    private final ReplyKeyboard keyboard;

    @Override
    public void send(TelegramBot bot, Long chatId) throws TelegramApiException {
        bot.execute(SendMessage.builder()
            .chatId(chatId.toString())
            .text(text)
            .replyMarkup(keyboard)
            .build());
    }
}
