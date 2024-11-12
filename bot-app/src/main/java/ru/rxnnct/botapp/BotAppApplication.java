package ru.rxnnct.botapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class BotAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotAppApplication.class, args);
    }

}