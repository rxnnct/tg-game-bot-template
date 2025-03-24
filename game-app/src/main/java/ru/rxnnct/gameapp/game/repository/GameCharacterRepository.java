package ru.rxnnct.gameapp.game.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.rxnnct.gameapp.game.entity.GameCharacter;

@Repository
public interface GameCharacterRepository extends CrudRepository<GameCharacter, Long> {

}