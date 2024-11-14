package ru.rxnnct.botapp.service;

import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
    public Player createPlayer(String name, Long tgId) {
        return this.playerRepository.save(new Player(null, name, tgId));
    }

    @Override
    public Optional<Player> findPlayer(int playerId) {
        return this.playerRepository.findById(playerId);
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
