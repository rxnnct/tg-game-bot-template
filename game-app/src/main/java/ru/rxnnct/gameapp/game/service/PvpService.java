package ru.rxnnct.gameapp.game.service;

import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rxnnct.gameapp.core.entity.AppUser;
import ru.rxnnct.gameapp.core.exceptions.AppUserNotFoundException;
import ru.rxnnct.gameapp.core.exceptions.NoCharactersException;
import ru.rxnnct.gameapp.core.service.AppUserService;
import ru.rxnnct.gameapp.game.dto.ExampleFightResultDto;
import ru.rxnnct.gameapp.game.entity.GameCharacter;
import ru.rxnnct.gameapp.game.domain.MatchedPlayers;

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
    public ExampleFightResultDto exampleFight(Long tgId) {
        MatchedPlayers matchedPlayers = makeMatch(tgId);

        //todo: exampleFight + PvP on/off check
        Random random = new Random();
        ExampleFightResultDto result = new ExampleFightResultDto(
            "stub",
            "stub",
            "stub",
            random.nextBoolean());

        return result;
    }

    private MatchedPlayers makeMatch(Long tgId) {

        MatchedPlayers matchedPlayers = new MatchedPlayers(null, null);

        //todo: makeMatch + PvP on/off check
        //MatchedPlayers matchedPlayers = new MatchedPlayers(null, null);

        return matchedPlayers;
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