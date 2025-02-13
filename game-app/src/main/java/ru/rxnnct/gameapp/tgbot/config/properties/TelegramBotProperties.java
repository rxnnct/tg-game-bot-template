package ru.rxnnct.gameapp.tgbot.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public record TelegramBotProperties(String name, String token) {

}