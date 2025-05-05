package ru.rxnnct.gameapp.game.service;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rxnnct.gameapp.core.entity.AppUser;
import ru.rxnnct.gameapp.game.entity.GameCharacter;
import ru.rxnnct.gameapp.core.exceptions.NoCharactersException;
import ru.rxnnct.gameapp.game.repository.GameCharacterRepository;

@Service
@RequiredArgsConstructor
public class GameCharacterService {

    private final GameCharacterRepository gameCharacterRepository;

    @Transactional
    public GameCharacter createCharacter(AppUser appUser) {
        GameCharacter newCharacter = new GameCharacter();
        newCharacter.setMaxHealth(100L);
        newCharacter.setStrength(20L);
        newCharacter.setCurrency(0L);
        //newCharacter.setIsPvpAvailable(false);
        newCharacter.setCreatedAt(LocalDateTime.now());
        newCharacter.setAppUser(appUser);

        gameCharacterRepository.save(newCharacter);
        return newCharacter;
    }

    @Transactional
    public void addCurrency(UUID gameCharacterId, long currency) {
        GameCharacter gameCharacter = gameCharacterRepository.findById(gameCharacterId)
            .orElseThrow(() -> new NoCharactersException(
                "GameCharacter not found with id: " + gameCharacterId));

        long newCurrency = Math.addExact(gameCharacter.getCurrency(), currency);
        gameCharacter.setCurrency(newCurrency);

    }
}