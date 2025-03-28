package ru.rxnnct.gameapp.game.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rxnnct.gameapp.core.entity.Player;
import ru.rxnnct.gameapp.game.entity.PlayerRating;
import ru.rxnnct.gameapp.game.repository.PlayerRatingRepository;

@Service
@RequiredArgsConstructor
public class PlayerRatingService {

    private final PlayerRatingRepository playerRatingRepository;

    @Transactional
    public PlayerRating createPlayerRating(Player player) {
        PlayerRating newPlayerRating = new PlayerRating();
        newPlayerRating.setPlayer(player);
        newPlayerRating.setMmr(0L);
        newPlayerRating.setGamesPlayed(0L);
        newPlayerRating.setWins(0L);
        newPlayerRating.setLosses(0L);
        newPlayerRating.setLastUpdated(LocalDateTime.now());

        playerRatingRepository.save(newPlayerRating);
        return newPlayerRating;
    }
}
