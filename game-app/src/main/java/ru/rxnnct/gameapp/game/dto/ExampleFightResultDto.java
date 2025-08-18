package ru.rxnnct.gameapp.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExampleFightResultDto {

    private String playerCharacterName;
    private String enemyCharacterName;
    private String battleLog;
    private Boolean result;

}
