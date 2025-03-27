package ru.rxnnct.gameapp.game.service;

import java.time.Duration;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rxnnct.gameapp.core.entity.Player;
import ru.rxnnct.gameapp.core.exceptions.PlayerNotFoundException;
import ru.rxnnct.gameapp.core.service.PlayerService;
import ru.rxnnct.gameapp.core.exceptions.NoCharactersException;

@Service
@RequiredArgsConstructor
public class PveService {

    private final GameCharacterService gameCharacterService;
    private final PlayerService playerService;
    private final StringRedisTemplate redisTemplate;

    private static final String DELAYED_TASKS_KEY = "delayed_tasks";
    private static final int PVE_ACTIVITY_DURATION = 5;

    @Transactional
    public Long exploreDungeon(Long tgId) {
        Player player = playerService.findPlayerByTgId(tgId)
            .orElseThrow(() -> new PlayerNotFoundException("Player not found with tgId: " + tgId));

        if (player.getCharacters().isEmpty()) {
            throw new NoCharactersException("Player has no characters, tgId: " + tgId);
        }

        Long playerId = player.getCharacters().getFirst().getId();
        long income = (long) ((Math.random() * (12 - 8)) + 8);
        gameCharacterService.addCurrency(playerId, income);

        return income;
    }

    public void schedulePveActivity(Long tgId, Locale locale) {
        long executionTime = System.currentTimeMillis() + (PVE_ACTIVITY_DURATION * 1000);
        String task = String.format("pve_activity:%d:%s", tgId, locale.toLanguageTag());
        redisTemplate.opsForZSet().add(DELAYED_TASKS_KEY, task, executionTime);
    }

    public void setPveActivityInProgress(Long tgId, boolean inProgress, String pveActivityName) {
        String key = "%s:%d".formatted(pveActivityName, tgId);
        if (inProgress) {
            redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(PVE_ACTIVITY_DURATION));
        } else {
            redisTemplate.delete(key);
        }
    }

    public boolean isPveActivityInProgress(Long tgId, String pveActivityName) {
        String key = "%s:%d".formatted(pveActivityName, tgId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}