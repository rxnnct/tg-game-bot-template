package ru.rxnnct.gameapp.game.service;

import java.time.Duration;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rxnnct.gameapp.core.entity.AppUser;
import ru.rxnnct.gameapp.core.exceptions.AppUserNotFoundException;
import ru.rxnnct.gameapp.core.service.AppUserService;
import ru.rxnnct.gameapp.core.exceptions.NoCharactersException;

@Service
@RequiredArgsConstructor
public class PveService {

    private final GameCharacterService gameCharacterService;
    private final AppUserService appUserService;
    private final StringRedisTemplate redisTemplate;

    private static final String DELAYED_TASKS_KEY = "delayed_tasks";
    private static final String EXAMPLE_PVE_ACTIVITY_IS_IN_PROGRESS_KEY = "example_pve_activity_is_in_progress";
    private static final int EXAMPLE_PVE_ACTIVITY_DURATION = 20;

    @Transactional
    public Long examplePveActivity(Long tgId) {
        AppUser appUser = appUserService.findAppUserByTgId(tgId)
            .orElseThrow(() -> new AppUserNotFoundException(
                "User not found with tgId: %d".formatted(tgId)));

        if (appUser.getCharacters().isEmpty()) {
            throw new NoCharactersException("User has no characters, tgId: %d".formatted(tgId));
        }

        Long appUserId = appUser.getCharacters().getFirst().getId();
        long income = (long) ((Math.random() * (12 - 8)) + 8);
        gameCharacterService.addCurrency(appUserId, income);

        return income;
    }

    public void scheduleExamplePveActivity(Long tgId, Locale locale) {
        long executionTime = System.currentTimeMillis() + (EXAMPLE_PVE_ACTIVITY_DURATION * 1000);
        String task = String.format("example_pve_activity:%d:%s", tgId, locale.toLanguageTag());
        redisTemplate.opsForZSet().add(DELAYED_TASKS_KEY, task, executionTime);
    }

    public void setExamplePveActivityInProgress(Long tgId, boolean inProgress) {
        setPveActivityInProgress(tgId, inProgress, EXAMPLE_PVE_ACTIVITY_IS_IN_PROGRESS_KEY,
            EXAMPLE_PVE_ACTIVITY_DURATION);
    }

    public boolean isExamplePveActivityInProgress(Long tgId) {
        return isPveActivityInProgress(tgId, EXAMPLE_PVE_ACTIVITY_IS_IN_PROGRESS_KEY);
    }

    private void setPveActivityInProgress(Long tgId, boolean inProgress, String pveActivityName,
        int duration) {
        String key = "%s:%d".formatted(pveActivityName, tgId);
        if (inProgress) {
            redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(duration));
        } else {
            redisTemplate.delete(key);
        }
    }

    private boolean isPveActivityInProgress(Long tgId, String pveActivityName) {
        String key = "%s:%d".formatted(pveActivityName, tgId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}