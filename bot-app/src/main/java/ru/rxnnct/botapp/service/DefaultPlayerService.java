package ru.rxnnct.botapp.service;

import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rxnnct.botapp.entity.Player;
import ru.rxnnct.botapp.repository.PlayerRepository;

@Service
@RequiredArgsConstructor
public class DefaultPlayerService implements PlayerService {

    private final PlayerRepository playerRepository;

    @Override
    @Transactional
    public Player createOrUpdatePlayer(String name, Long tgId) {
        try {
            return playerRepository.findByTgId(tgId)
                .map(player -> {
                    player.setName(name);
                    return playerRepository.save(player);
                })
                .orElseGet(() -> {
                    Player newPlayer = new Player(null, name, tgId);
                    return playerRepository.save(newPlayer);
                });
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                "This name is already taken. Please choose another one.");
        }
    }

    @Override
    public Optional<Player> findPlayerByTgId(long tgId) {
        return this.playerRepository.findByTgId(tgId);
    }

    @Override
    @Transactional
    public void updatePlayer(Integer id, String name, Long tgId) {
        this.playerRepository.findById(id).ifPresentOrElse(player -> {
            player.setName(name);
            player.setTgId(tgId);
        }, () -> {
            throw new NoSuchElementException("Player with ID " + id + " not found");
        });
    }

    @Override
    public void deletePlayer(Integer id) {
        this.playerRepository.deleteById(id);
    }
}
