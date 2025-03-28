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
import ru.rxnnct.gameapp.core.dto.PlayerInfoDto;
import ru.rxnnct.gameapp.core.entity.Player;
import ru.rxnnct.gameapp.core.exceptions.NoCharactersException;
import ru.rxnnct.gameapp.core.exceptions.PlayerNotFoundException;
import ru.rxnnct.gameapp.core.repository.PlayerRepository;
import ru.rxnnct.gameapp.game.entity.GameCharacter;
import ru.rxnnct.gameapp.game.entity.PlayerRating;
import ru.rxnnct.gameapp.game.service.GameCharacterService;
import ru.rxnnct.gameapp.game.service.PlayerRatingService;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final GameCharacterService gameCharacterService;
    private final PlayerRatingService playerRatingService;

    @Transactional
    public void createOrUpdatePlayer(String name, Long tgId, boolean isRegistered) {
        try {
            Player newPlayer = new Player(
                null,
                tgId,
                name,
                isRegistered,
                0L,
                LocalDateTime.now(),
                null,
                null);
            playerRepository.save(newPlayer);
            createCharacter(newPlayer.getId());
            createPlayerRating(newPlayer.getId());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                "This name is already taken. Please choose another one.");
        }
    }

    public Optional<Player> findPlayerByTgId(long tgId) {
        return this.playerRepository.findByTgId(tgId);
    }

    public boolean isPlayerRegistered(long tgId) {
        return playerRepository.existsByTgIdAndIsRegisteredTrue(tgId);
    }

    @Transactional
    public Optional<PlayerInfoDto> getPlayerInfo(long tgId) {
        Player player = playerRepository.findByTgId(tgId)
            .orElseThrow(() -> new PlayerNotFoundException("Player not found with tgId: " + tgId));

        if (player.getCharacters() == null || player.getCharacters().isEmpty()) {
            throw new NoCharactersException("Player has no characters");
        }

        GameCharacter character = player.getCharacters().getFirst();

        PlayerInfoDto playerInfoDto = new PlayerInfoDto();
        playerInfoDto.setName(player.getName());
        playerInfoDto.setBalance(player.getBalance());
        playerInfoDto.setCurrency(character.getCurrency());

        return Optional.of(playerInfoDto);
    }

    @Transactional
    public void createCharacter(long id) {
        this.playerRepository.findById(id).ifPresentOrElse(player -> {
            GameCharacter newCharacter = gameCharacterService.createCharacter(player);

            List<GameCharacter> characters = player.getCharacters();
            if (characters == null) {
                characters = new ArrayList<>();
            }

            characters.add(newCharacter);

            player.setCharacters(characters);

            playerRepository.save(player);
        }, () -> {
            throw new NoSuchElementException("Player with ID " + id + " not found");
        });
    }

    @Transactional
    public void createPlayerRating(long id) {
        this.playerRepository.findById(id).ifPresentOrElse(player -> {
            PlayerRating newPlayerRating = playerRatingService.createPlayerRating(player);

            player.setPlayerRating(newPlayerRating);

            playerRepository.save(player);
        }, () -> {
            throw new NoSuchElementException("Player with ID " + id + " not found");
        });
    }

//    @Transactional
//    public void updatePlayer(Integer id, String name, Long tgId) {
//        this.playerRepository.findById(id).ifPresentOrElse(player -> {
//            player.setName(name);
//            player.setTgId(tgId);
//        }, () -> {
//            throw new NoSuchElementException("Player with ID " + id + " not found");
//        });
//    }
}
