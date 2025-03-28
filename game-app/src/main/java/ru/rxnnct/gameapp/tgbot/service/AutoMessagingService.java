package ru.rxnnct.gameapp.tgbot.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rxnnct.gameapp.core.exceptions.NoCharactersException;
import ru.rxnnct.gameapp.core.exceptions.AppUserNotFoundException;
import ru.rxnnct.gameapp.game.service.PveService;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutoMessagingService {

    private final TelegramBot telegramBot;
    private final PveService pveService;
    private final MessageSource messageSource;

    public void handlePveResult(Long tgId, Locale locale) {
        try {
            Long income = pveService.examplePveActivity(tgId);
            String responseMessage = messageSource.getMessage(
                "bot.character.pve_result",
                new Object[]{income},
                locale
            );
            telegramBot.execute(buildSendMessage(tgId, responseMessage));
        } catch (AppUserNotFoundException e) {
            String errorMessage = messageSource.getMessage(
                "bot.error.app_user_not_found",
                null,
                locale
            );
            try {
                telegramBot.execute(buildSendMessage(tgId, errorMessage));
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
        } catch (NoCharactersException e) {
            String errorMessage = messageSource.getMessage(
                "bot.error.no_characters",
                null,
                locale
            );
            try {
                telegramBot.execute(buildSendMessage(tgId, errorMessage));
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            String errorMessage = messageSource.getMessage(
                "bot.error.general",
                null,
                locale
            );
            try {
                telegramBot.execute(buildSendMessage(tgId, errorMessage));
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            pveService.setExamplePveActivityInProgress(tgId, false);
        }
    }

    private SendMessage buildSendMessage(Long chatId, String responseMessage) {
        var message = new SendMessage();
        message.setChatId(chatId);
        message.setText(responseMessage);
        return message;
    }

}