package ru.rxnnct.gameapp.game.repository;

import org.springframework.data.repository.CrudRepository;
import ru.rxnnct.gameapp.game.entity.PlayerRating;

public interface PlayerRatingRepository extends CrudRepository<PlayerRating, Long> {

}
