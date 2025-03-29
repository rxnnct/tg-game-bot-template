package ru.rxnnct.gameapp.game.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PvpService {

    @Transactional
    public String examplePvpActivity(Long tgId) {
        return "WIN!!!";
    }

}