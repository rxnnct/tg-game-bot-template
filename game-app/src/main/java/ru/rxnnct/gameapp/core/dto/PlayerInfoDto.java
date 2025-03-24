package ru.rxnnct.gameapp.core.dto;

import lombok.Data;

@Data
public class PlayerInfoDto {

    private String name;
    private Long balance;
    private Long currency;

}