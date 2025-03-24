package ru.rxnnct.gameapp.game.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rxnnct.gameapp.core.entity.Player;
import ru.rxnnct.gameapp.game.entity.GameCharacter;
import ru.rxnnct.gameapp.game.repository.GameCharacterExampleRepository;

@Service
@RequiredArgsConstructor
public class GameCharacterExampleService {

    private final GameCharacterExampleRepository gameCharacterExampleRepository;

    @Transactional
    public GameCharacter createCharacter(Player player) {
        GameCharacter newCharacter = new GameCharacter();
        newCharacter.setMaxHealth(100L);
        newCharacter.setStrength(20L);
        newCharacter.setCurrency(0L);
        newCharacter.setCreatedAt(LocalDateTime.now());
        newCharacter.setPlayer(player);

        gameCharacterExampleRepository.save(newCharacter);
        return newCharacter;
    }

}