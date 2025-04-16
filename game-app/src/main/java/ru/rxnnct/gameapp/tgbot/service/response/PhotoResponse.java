package ru.rxnnct.gameapp.tgbot.service.response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.rxnnct.gameapp.tgbot.service.TelegramBot;

@RequiredArgsConstructor
@Slf4j
public class PhotoResponse implements BotResponse {

    private final String fileId;
    private final String caption;
    private final ReplyKeyboard keyboard;

    @Override
    public void send(TelegramBot bot, Long chatId) throws TelegramApiException {
        try {
            bot.execute(SendPhoto.builder()
                .chatId(chatId.toString())
                .photo(new InputFile(fileId))
                .caption(caption)
                .replyMarkup(keyboard)
                .build());
        } catch (TelegramApiRequestException e) {
            if (e.getApiResponse().contains("wrong remote file identifier")) {
                log.error("Invalid file_id for image: {}", fileId);
                new TextResponse(caption, keyboard).send(bot, chatId);
            } else {
                throw e;
            }
        }
    }
}
