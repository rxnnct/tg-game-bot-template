package ru.rxnnct.gameapp.game.service;

import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.rxnnct.gameapp.tgbot.service.AutoMessagingService;

@Service
@RequiredArgsConstructor
public class DelayedTaskService {

    private final StringRedisTemplate redisTemplate;
    private static final String DELAYED_TASKS_KEY = "delayed_tasks";
    private final AutoMessagingService autoMessagingService;

    @Scheduled(fixedRate = 2000)
    public void checkDelayedTasks() {
        long now = System.currentTimeMillis();
        Locale locale = Locale.getDefault();

        Set<String> tasks = redisTemplate.opsForZSet().rangeByScore(DELAYED_TASKS_KEY, 0, now);

        if (tasks != null) {
            for (String task : tasks) {
                if (task.startsWith("pve_activity:")) {
                    String playerId = task.split(":")[1];
                    processPveActivity(Long.valueOf(playerId), locale);
                    redisTemplate.opsForZSet().remove(DELAYED_TASKS_KEY, task);
                }
            }
        }
    }

    private void processPveActivity(Long tgId, Locale locale) {
        autoMessagingService.handlePveResult(tgId, locale);
    }
}