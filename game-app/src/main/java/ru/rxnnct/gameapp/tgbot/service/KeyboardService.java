package ru.rxnnct.gameapp.tgbot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.rxnnct.gameapp.core.service.AppUserService;

@Service
@RequiredArgsConstructor
public class KeyboardService {

    private final AppUserService appUserService;
    private final MessageSource messageSource;

    public ReplyKeyboardMarkup createMenu(Long tgId, Locale locale) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        if (appUserService.isAppUserRegistered(tgId)) {
            KeyboardRow row1 = new KeyboardRow();
            row1.add(messageSource.getMessage("bot.menu.pve", null, locale));
            row1.add(messageSource.getMessage("bot.menu.pvp", null, locale));

            KeyboardRow row2 = new KeyboardRow();
            row2.add(messageSource.getMessage("bot.menu.app_user_info", null, locale));
            row2.add(messageSource.getMessage("bot.menu.help", null, locale));

            keyboard.add(row1);
            keyboard.add(row2);
        } else {
            KeyboardRow row = new KeyboardRow();
            row.add(messageSource.getMessage("bot.menu.set_name", null, locale));
            row.add(messageSource.getMessage("bot.menu.help", null, locale));

            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
