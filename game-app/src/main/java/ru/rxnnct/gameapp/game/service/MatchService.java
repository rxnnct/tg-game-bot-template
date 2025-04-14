package ru.rxnnct.gameapp.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rxnnct.gameapp.game.repository.LastMatchBattleLogRepository;
import ru.rxnnct.gameapp.game.repository.MatchRepository;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
    private final LastMatchBattleLogRepository lastMatchBattleLogRepository;

}
