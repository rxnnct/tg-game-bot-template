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
        Set<String> tasks = redisTemplate.opsForZSet().rangeByScore(DELAYED_TASKS_KEY, 0, now);

        if (tasks != null) {
            for (String task : tasks) {
                if (task.startsWith("pve_activity:")) {
                    String[] parts = task.split(":");
                    Long tgId = Long.valueOf(parts[1]);
                    Locale locale = Locale.forLanguageTag(parts[2]);

                    autoMessagingService.handlePveResult(tgId, locale);
                    redisTemplate.opsForZSet().remove(DELAYED_TASKS_KEY, task);
                }
            }
        }
    }
}