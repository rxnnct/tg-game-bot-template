package ru.rxnnct.gameapp.core.dto;

import lombok.Data;

@Data
public class AppUserInfoDto {

    private String name;
    private Long balance;
    private Long currency;

}