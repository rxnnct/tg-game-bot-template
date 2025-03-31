package ru.rxnnct.gameapp.game.repository;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.rxnnct.gameapp.game.entity.GameCharacter;

@Repository
public interface GameCharacterRepository extends CrudRepository<GameCharacter, UUID> {

}