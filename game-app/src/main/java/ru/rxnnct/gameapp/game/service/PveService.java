package ru.rxnnct.gameapp.game.service;

import lombok.RequiredArgsConstructor;
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
}