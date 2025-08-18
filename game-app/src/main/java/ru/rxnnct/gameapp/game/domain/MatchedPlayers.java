package ru.rxnnct.gameapp.game.domain;

import ru.rxnnct.gameapp.core.entity.AppUser;

public record MatchedPlayers(
    AppUser player1,
    AppUser player2
) {

}
