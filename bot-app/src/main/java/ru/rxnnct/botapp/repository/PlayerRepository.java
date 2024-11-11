package ru.rxnnct.botapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.rxnnct.botapp.entity.Player;

public interface PlayerRepository extends CrudRepository<Player, Integer> {

}
