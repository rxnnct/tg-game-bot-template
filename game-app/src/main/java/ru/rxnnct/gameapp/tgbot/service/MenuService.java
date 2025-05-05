package ru.rxnnct.gameapp.tgbot.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final StringRedisTemplate redisTemplate;

    private static final Duration MENU_STATE_TTL = Duration.ofHours(1);

    public void setRegistrationInProgress(Long tgId, boolean inProgress) {
        String key = "registration:" + tgId;
        if (inProgress) {
            redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(30));
        } else {
            redisTemplate.delete(key);
        }
    }

    public boolean isRegistrationInProgress(Long tgId) {
        String key = "registration:" + tgId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void setMenuState(Long tgId, String state) {
        String key = "menu_state:" + tgId;
        if (state != null) {
            redisTemplate.opsForValue().set(key, state, MENU_STATE_TTL);
        } else {
            redisTemplate.delete(key);
        }
    }

    public String getMenuState(Long tgId) {
        String key = "menu_state:" + tgId;
        return redisTemplate.opsForValue().get(key);
    }

}
