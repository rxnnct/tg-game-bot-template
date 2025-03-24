package ru.rxnnct.gameapp.game.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rxnnct.gameapp.game.entity.GameCharacter;

@Repository
public interface GameCharacterRepository extends JpaRepository<GameCharacter, Long> {

}