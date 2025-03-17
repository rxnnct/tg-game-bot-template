package ru.rxnnct.gameapp.tgbot.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Service
@RequiredArgsConstructor
public class KeyboardService {

//    public ReplyKeyboardMarkup createMainMenuKeyboard(MenuState menuState) {
//        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
//        keyboardMarkup.setResizeKeyboard(true);
//        keyboardMarkup.setOneTimeKeyboard(false);
//
//        List<KeyboardRow> keyboard = new ArrayList<>();
//
//        KeyboardRow row1 = new KeyboardRow();
//        row1.add("/start");
//        row1.add("/help");
//
//        KeyboardRow row2 = new KeyboardRow();
//        if (menuState == MenuState.NAME_NOT_SET) {
//            row2.add("/set_name");
//        } else {
//            row2.add("/player_info");
//        }
//
//        keyboard.add(row1);
//        keyboard.add(row2);
//
//        keyboardMarkup.setKeyboard(keyboard);
//        return keyboardMarkup;
//    }
}
