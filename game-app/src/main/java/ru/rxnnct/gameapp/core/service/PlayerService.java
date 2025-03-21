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
import ru.rxnnct.gameapp.core.entity.Player;
import ru.rxnnct.gameapp.core.repository.PlayerRepository;
import ru.rxnnct.gameapp.game.entity.GameCharacter;
import ru.rxnnct.gameapp.game.service.GameCharacterService;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final GameCharacterService gameCharacterService;

    @Transactional
    public void createOrUpdatePlayer(String name, Long tgId, boolean isRegistered) {
        try {
            playerRepository.findByTgId(tgId)
                .map(player -> {
                    player.setName(name);
                    player.setIsRegistered(isRegistered);
                    return playerRepository.save(player);
                })
                .orElseGet(() -> {
                    Player newPlayer = new Player(
                        null,
                        tgId,
                        name,
                        isRegistered,
                        0L,
                        LocalDateTime.now(),
                        null);
                    return playerRepository.save(newPlayer);
                });
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
    public void createCharacter(Long tgId) {
        this.playerRepository.findByTgId(tgId).ifPresentOrElse(player -> {
            var newCharacter = gameCharacterService.createCharacter(player);

            List<GameCharacter> characters = player.getCharacters();
            if (characters == null) {
                characters = new ArrayList<>();
            }

            characters.add(newCharacter);

            player.setCharacters(characters);

            playerRepository.save(player);
        }, () -> {
            throw new NoSuchElementException("Player with ID " + tgId + " not found");
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
