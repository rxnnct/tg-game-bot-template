package ru.rxnnct.gameapp.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rxnnct.gameapp.core.entity.AppUser;
import ru.rxnnct.gameapp.game.entity.PlayerRating;
import ru.rxnnct.gameapp.game.repository.PlayerRatingRepository;

@Service
@RequiredArgsConstructor
public class PlayerRatingService {

    private final PlayerRatingRepository playerRatingRepository;

    @Transactional
    public PlayerRating createPlayerRating(AppUser appUser) {
        PlayerRating newPlayerRating = new PlayerRating();
        newPlayerRating.setAppUser(appUser);
        newPlayerRating.setMmr(1000L);

        playerRatingRepository.save(newPlayerRating);
        return newPlayerRating;
    }
}
