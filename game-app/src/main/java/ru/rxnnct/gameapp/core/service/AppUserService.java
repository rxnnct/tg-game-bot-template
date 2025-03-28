package ru.rxnnct.gameapp.core.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rxnnct.gameapp.core.dto.AppUserInfoDto;
import ru.rxnnct.gameapp.core.entity.AppUser;
import ru.rxnnct.gameapp.core.exceptions.NoCharactersException;
import ru.rxnnct.gameapp.core.exceptions.AppUserNotFoundException;
import ru.rxnnct.gameapp.core.repository.AppUserRepository;
import ru.rxnnct.gameapp.game.entity.GameCharacter;
import ru.rxnnct.gameapp.game.entity.PlayerRating;
import ru.rxnnct.gameapp.game.service.GameCharacterService;
import ru.rxnnct.gameapp.game.service.PlayerRatingService;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final GameCharacterService gameCharacterService;
    private final PlayerRatingService playerRatingService;

    @Transactional
    public void createOrUpdateAppUser(String name, Long tgId, boolean isRegistered) {
        try {
            AppUser newAppUser = new AppUser(
                null,
                tgId,
                name,
                isRegistered,
                0L,
                LocalDateTime.now(),
                null,
                null);
            appUserRepository.save(newAppUser);
            createCharacter(newAppUser.getId());
            createPlayerRating(newAppUser.getId());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                "This name is already taken. Please choose another one.");
        }
    }

    public Optional<AppUser> findAppUserByTgId(long tgId) {
        return this.appUserRepository.findByTgId(tgId);
    }

    public boolean isAppUserRegistered(long tgId) {
        return appUserRepository.existsByTgIdAndIsRegisteredTrue(tgId);
    }

    @Transactional
    public Optional<AppUserInfoDto> getAppUserInfo(long tgId) {
        AppUser appUser = appUserRepository.findByTgId(tgId)
            .orElseThrow(() -> new AppUserNotFoundException("User not found with tgId: " + tgId));

        if (appUser.getCharacters() == null || appUser.getCharacters().isEmpty()) {
            throw new NoCharactersException("User has no characters");
        }

        GameCharacter character = appUser.getCharacters().getFirst();

        AppUserInfoDto appUserInfoDto = new AppUserInfoDto();
        appUserInfoDto.setName(appUser.getName());
        appUserInfoDto.setBalance(appUser.getBalance());
        appUserInfoDto.setCurrency(character.getCurrency());

        return Optional.of(appUserInfoDto);
    }

    @Transactional
    public void createCharacter(long id) {
        this.appUserRepository.findById(id).ifPresentOrElse(appUser -> {
            GameCharacter newCharacter = gameCharacterService.createCharacter(appUser);

            List<GameCharacter> characters = appUser.getCharacters();
            if (characters == null) {
                characters = new ArrayList<>();
            }

            characters.add(newCharacter);

            appUser.setCharacters(characters);

            appUserRepository.save(appUser);
        }, () -> {
            throw new NoSuchElementException("User with ID " + id + " not found");
        });
    }

    @Transactional
    public void createPlayerRating(long id) {
        this.appUserRepository.findById(id).ifPresentOrElse(appUser -> {
            PlayerRating newPlayerRating = playerRatingService.createPlayerRating(appUser);

            appUser.setPlayerRating(newPlayerRating);

            appUserRepository.save(appUser);
        }, () -> {
            throw new NoSuchElementException("User with ID " + id + " not found");
        });
    }
}
