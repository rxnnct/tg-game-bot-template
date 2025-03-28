create schema if not exists game_app;

create table game_app.t_app_user
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
    app_user_id  int references game_app.t_app_user (id) on delete cascade
);

CREATE TABLE game_app.t_player_rating
(
    id           serial primary key,
    app_user_id    int references game_app.t_app_user (id) on delete cascade,
    mmr          int8,
    games_played int8,
    wins         int8,
    losses       int8,
    last_updated timestamptz,
    unique (app_user_id)
);