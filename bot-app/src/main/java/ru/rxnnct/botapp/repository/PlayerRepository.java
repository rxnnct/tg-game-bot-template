package ru.rxnnct.botapp.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import ru.rxnnct.botapp.entity.Player;

public interface PlayerRepository extends CrudRepository<Player, Integer> {

    Optional<Player> findByTgId(Long tgId);

}
