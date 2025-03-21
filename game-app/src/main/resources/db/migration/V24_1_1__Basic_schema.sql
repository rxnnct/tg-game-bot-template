create schema if not exists game_app;

create table game_app.t_player
(
    id            serial primary key,
    tg_id         int8 unique,
    name          varchar(25) not null unique,
    is_registered boolean,
    balance       int8,
    created_at    timestamptz
);

create table game_app.t_game_character
(
    id         serial primary key,
    max_health int8,
    strength   int8,
    currency   int8,
    created_at timestamptz,
    player_id  int references game_app.t_player (id) on delete cascade
);