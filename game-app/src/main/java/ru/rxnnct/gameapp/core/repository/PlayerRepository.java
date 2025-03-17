package ru.rxnnct.gameapp.core.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import ru.rxnnct.gameapp.core.entity.Player;

public interface PlayerRepository extends CrudRepository<Player, Integer> {

    Optional<Player> findByTgId(Long tgId);

}
