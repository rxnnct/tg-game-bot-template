package ru.rxnnct.gameapp.game.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rxnnct.gameapp.core.entity.Player;
import ru.rxnnct.gameapp.game.entity.GameCharacter;
import ru.rxnnct.gameapp.game.exceptions.NoCharactersException;
import ru.rxnnct.gameapp.game.repository.GameCharacterRepository;

@Service
@RequiredArgsConstructor
public class GameCharacterService {

    private final GameCharacterRepository gameCharacterRepository;

    @Transactional
    public GameCharacter createCharacter(Player player) {
        GameCharacter newCharacter = new GameCharacter();
        newCharacter.setMaxHealth(100L);
        newCharacter.setStrength(20L);
        newCharacter.setCurrency(0L);
        newCharacter.setCreatedAt(LocalDateTime.now());
        newCharacter.setPlayer(player);

        gameCharacterRepository.save(newCharacter);
        return newCharacter;
    }

    @Transactional
    public void addCurrency(Long gameCharacterId, long currency) {
        GameCharacter gameCharacter = gameCharacterRepository.findById(gameCharacterId)
            .orElseThrow(() -> new NoCharactersException(
                "GameCharacter not found with id: " + gameCharacterId));

        long newCurrency = Math.addExact(gameCharacter.getCurrency(), currency);
        gameCharacter.setCurrency(newCurrency);

    }

}