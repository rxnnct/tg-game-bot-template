package ru.rxnnct.gameapp.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rxnnct.gameapp.game.repository.GameCharacterRepository;

@Service
@RequiredArgsConstructor
public class GameCharacterService {

    private final GameCharacterRepository gameCharacterRepository;

}