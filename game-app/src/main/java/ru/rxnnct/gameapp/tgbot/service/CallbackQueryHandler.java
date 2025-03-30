package ru.rxnnct.gameapp.tgbot.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rxnnct.gameapp.game.service.PveService;

@Service
@Slf4j
@RequiredArgsConstructor
public class CallbackQueryHandler {

    private final MessageSource messageSource;
    private final PveService pveService;

    public void handleCallbackQuery(Update update, AbsSender sender) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        Long tgId = update.getCallbackQuery().getMessage().getChatId();
        String languageCode = update.getCallbackQuery().getFrom().getLanguageCode();
        Locale locale = Locale.forLanguageTag(languageCode != null ? languageCode : "en");

        SendMessage message = new SendMessage();
        message.setChatId(tgId.toString());

        switch (callbackData) {
            case "pve_example_1_start":
                if (!pveService.isExamplePveActivityInProgress(tgId)) {
                    pveService.scheduleExamplePveActivity(tgId, locale);
                    pveService.setExamplePveActivityInProgress(tgId, true);
                    message.setText(
                        messageSource.getMessage("bot.pve.example_1_start", null, locale));
                } else {
                    message.setText(
                        messageSource.getMessage("bot.pve.already_in_progress", null, locale));
                }
                break;
            case "pve_example_2_start":
                if (!pveService.isExamplePveActivityInProgress(tgId)) {
                    message.setText(
                        messageSource.getMessage("bot.pve.example_2.message", null, locale));
                } else {
                    message.setText(
                        messageSource.getMessage("bot.pve.already_in_progress", null, locale));
                }
                break;
            default:
                message.setText(messageSource.getMessage("bot.inline.unknown", null, locale));
        }

        sender.execute(message);
    }
}