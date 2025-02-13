package ru.rxnnct.gameapp.tgbot.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import ru.rxnnct.gameapp.tgbot.entity.Player;

public interface PlayerRepository extends CrudRepository<Player, Integer> {

    Optional<Player> findByTgId(Long tgId);

}
