package ru.rxnnct.botapp.service;

import java.util.Optional;
import ru.rxnnct.botapp.entity.Player;

public interface PlayerService {

    Player createPlayer(String name, Integer tgId);

    Optional<Player> findPlayer(int playerId);

    void updatePlayer(Integer id, String name, Integer tgId);

    void deletePlayer(Integer id);

}
