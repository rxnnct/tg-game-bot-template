package ru.rxnnct.gameapp.tgbot.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuService {

//    private final StringRedisTemplate redisTemplate;
//
//    public void setMenuState(Long tgId, String menuState) {
//        redisTemplate.opsForValue()
//            .set("menu_state:" + tgId, menuState, Duration.ofMinutes(30));
//    }
//
//    public String getMenuState(Long tgId) {
//        return redisTemplate.opsForValue().get("menu_state:" + tgId);
//    }
}
