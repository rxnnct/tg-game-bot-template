package ru.rxnnct.gameapp.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rxnnct.gameapp.core.entity.AppUser;
import ru.rxnnct.gameapp.core.exceptions.AppUserNotFoundException;
import ru.rxnnct.gameapp.core.exceptions.NoCharactersException;
import ru.rxnnct.gameapp.core.service.AppUserService;
import ru.rxnnct.gameapp.game.entity.GameCharacter;

@Service
@RequiredArgsConstructor
public class PvpService {

    private final GameCharacterService gameCharacterService;
    private final AppUserService appUserService;

    @Transactional
    public boolean switchIsPvpAvailable(Long tgId) {
        GameCharacter character = getFirstCharacterByTgId(tgId);
        return gameCharacterService.switchIsPvpAvailable(character.getId());
    }

    @Transactional
    public boolean getIsPvpAvailable(Long tgId) {
        GameCharacter character = getFirstCharacterByTgId(tgId);
        return character.getIsPvpAvailable();
    }

    @Transactional
    public String exampleFight(Long tgId) {
        return "WIN!!!";
    }

    private GameCharacter getFirstCharacterByTgId(Long tgId) {
        AppUser appUser = appUserService.findAppUserByTgId(tgId)
            .orElseThrow(() -> new AppUserNotFoundException(
                "User not found with tgId: %d".formatted(tgId)));

        if (appUser.getCharacters().isEmpty()) {
            throw new NoCharactersException("User has no characters, tgId: %d".formatted(tgId));
        }

        return appUser.getCharacters().getFirst();
    }
}