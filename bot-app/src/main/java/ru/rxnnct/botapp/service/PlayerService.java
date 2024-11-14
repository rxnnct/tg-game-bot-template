package ru.rxnnct.botapp.service;

import java.util.Optional;
import ru.rxnnct.botapp.entity.Player;

public interface PlayerService {

    Player createPlayer(String name, Long tgId);

    Optional<Player> findPlayer(int playerId);

    void updatePlayer(Integer id, String name, Long tgId);

    void deletePlayer(Integer id);

}
