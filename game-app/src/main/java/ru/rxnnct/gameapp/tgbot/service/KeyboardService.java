package ru.rxnnct.gameapp.tgbot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.rxnnct.gameapp.core.service.AppUserService;
import ru.rxnnct.gameapp.tgbot.enums.MenuState;

@Service
@RequiredArgsConstructor
public class KeyboardService {

    private final AppUserService appUserService;
    private final MessageSource messageSource;

    public ReplyKeyboardMarkup createMainMenu(Long tgId, Locale locale, MenuState state) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        if (appUserService.isAppUserRegistered(tgId)) {
            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            switch (state) {
                case PVP_MENU:
                    row1.add(messageSource.getMessage("bot.pvp_menu.fight", null, locale));
                    row1.add(messageSource.getMessage("bot.pvp_menu.switch_pvp_ready", null, locale));

                    row2.add(messageSource.getMessage("bot.menu.to_main_menu", null, locale));

                    keyboard.add(row1);
                    keyboard.add(row2);

                    break;
                default: //MAIN_MENU
                    row1.add(messageSource.getMessage("bot.menu.pve", null, locale));
                    row1.add(messageSource.getMessage("bot.menu.pvp", null, locale));

                    row2.add(messageSource.getMessage("bot.menu.info", null, locale));
                    row2.add(messageSource.getMessage("bot.menu.help", null, locale));

                    keyboard.add(row1);
                    keyboard.add(row2);

                    break;
            }
        } else {
            KeyboardRow row = new KeyboardRow();
            row.add(messageSource.getMessage("bot.menu.set_name", null, locale));
            row.add(messageSource.getMessage("bot.menu.help", null, locale));

            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup createPveInlineMenu(Locale locale) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
            .text(messageSource.getMessage("bot.pve.button.example_1_start", null, locale))
            .callbackData("pve_example_1_start")
            .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
            .text(messageSource.getMessage("bot.pve.button.example_2_start", null, locale))
            .callbackData("pve_example_2_start")
            .build());

        rowsInline.add(row1);
        rowsInline.add(row2);
        markupInline.setKeyboard(rowsInline);

        return markupInline;
    }
}
