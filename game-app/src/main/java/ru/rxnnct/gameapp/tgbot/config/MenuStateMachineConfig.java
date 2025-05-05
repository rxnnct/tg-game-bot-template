package ru.rxnnct.gameapp.tgbot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory
public class MenuStateMachineConfig extends StateMachineConfigurerAdapter<String, String> {

    @Override
    public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
        states
            .withStates()
            .initial("MAIN_MENU")
            .state("PVP_MENU");
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
        transitions
            .withExternal()
            .source("MAIN_MENU").target("PVP_MENU").event("TO_PVP_MENU")
            .and()
            .withExternal()
            .source("PVP").target("MAIN_MENU").event("TO_MAIN_MENU");
    }
}
